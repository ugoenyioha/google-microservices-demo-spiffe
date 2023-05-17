import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.nulabinc.oauth2.{AuthorizationRequest, OAuth2Provider}
import spray.json.DefaultJsonProtocol._
import spray.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object MicroservicesDemoApp {

  // Replace with your Azure AD configuration
  val azureAdConfig = OAuth2Provider.Config(
    clientId = "your_client_id",
    clientSecret = "your_client_secret",
    site = "https://login.microsoftonline.com",
    tokenUrl = "/your_tenant_id/oauth2/v2.0/token",
    authorizationUrl = "/your_tenant_id/oauth2/v2.0/authorize"
  )

  implicit val system = ActorSystem("loginservice")
  implicit val materializer = ActorMaterializer()

  def main(args: Array[String]): Unit = {
    val route: Route = concat(
      path("login") {
        get {
          val authorizationRequest = AuthorizationRequest(
            clientId = azureAdConfig.clientId,
            responseType = "code",
            redirectUri = "https://msdemo.usableapps.io/callback",
            scope = "openid profile email"
          )

          val authorizationUrl = azureAdConfig.site + azureAdConfig.authorizationUrl + authorizationRequest.toQueryString
          redirect(authorizationUrl, StatusCodes.Found)
        }
      },
      path("callback") {
        get {
          parameter("code") { code =>
            val accessTokenRequest = OAuth2Provider.AccessTokenRequest(
              clientId = azureAdConfig.clientId,
              clientSecret = azureAdConfig.clientSecret,
              code = code,
              grantType = "authorization_code",
              redirectUri = "https://msdemo.usableapps.io/callback"
            )

            val tokenUrl = azureAdConfig.site + azureAdConfig.tokenUrl
            val tokenFuture = OAuth2Provider.getAccessToken(tokenUrl, accessTokenRequest)

            onComplete(tokenFuture) {
              case Success(tokenResponse) =>
                val accessToken = tokenResponse.fields("access_token").convertTo[String]
                val idToken = tokenResponse.fields("id_token").convertTo[String]
                // Use the access token and ID token as needed
                complete(s"Logged in. Access token: $accessToken\nID token: $idToken")

              case Failure(exception) =>
                complete(StatusCodes.BadRequest, s"Failed to get access token: ${exception.getMessage}")
            }
          }
        }
      },
      path("protected") {
        // Add your authentication and authorization logic here
        complete("You have accessed a protected resource!")
      }
    )

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

    println("Server online at http://localhost:8080/\nPress RETURN to stop...")
    scala.io.StdIn.readLine()

    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}
