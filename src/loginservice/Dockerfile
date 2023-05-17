# Use the official OpenJDK image as the base image
FROM openjdk:11-jre-slim

# Set the working directory
WORKDIR /app

# Copy the fat JAR built with sbt-assembly into the container
COPY ./target/scala-2.13/microservices-demo-assembly-0.1.0-SNAPSHOT.jar /app/microservices-demo.jar

# Expose the port that the application will run on
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "/app/microservices-demo.jar"]
