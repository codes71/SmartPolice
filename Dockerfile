# Use a base image with Kotlin and Java already installed
FROM openjdk:11-jre-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the specific files and directories you want to include
COPY .gradle/ .idea/ app/ build/ gradle/ libfacesdk/ Login/ .gitignore build.gradle Dockerfile gradle.properties gradlew gradlew.bat local.properties privacy README.md settings.gradle ./

# Compile the Kotlin code
RUN kotlinc schema.kt -include-runtime -d schema.jar

# Define the command to run the application
CMD ["java", "-jar", "schema.jar"]
