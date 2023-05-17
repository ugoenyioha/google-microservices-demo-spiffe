libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % "10.2.7",
  "com.typesafe.akka" %% "akka-stream" % "2.6.18",
  "com.nulab-inc" %% "scala-oauth2-provider" % "0.22.0",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.2.7"
)

assembly / mainClass := Some("loginservice")

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}
