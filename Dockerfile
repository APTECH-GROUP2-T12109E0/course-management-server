# Use an official maven/Java image as the base image
FROM maven:3.8.4-openjdk-17-slim AS build

# Set the working directory in the container
WORKDIR /app

# Copy the Maven project file to the container
COPY pom.xml .

# Download the project dependencies
RUN mvn dependency:go-offline -B

# Copy the source code to the container
COPY src ./src

# Build the project
RUN mvn package -DskipTests

# Use a lightweight Java image as the base image for runtime
FROM openjdk:17-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the built JAR file from the build image to the runtime image
COPY --from=build /app/target/course-management-server-0.0.1-SNAPSHOT.jar .
COPY src/main/resources/application.properties.disc /app/application.properties

# Expose the port that the application will listen on
EXPOSE 8080

# Set the entry point for the container
ENTRYPOINT ["java", "-jar", "course-management-server-0.0.1-SNAPSHOT.jar"]
