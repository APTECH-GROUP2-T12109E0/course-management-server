# Use an official maven/Java image as the base image
FROM maven:3.8.4-openjdk-17 AS build

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

# Use a Java image as the base image for runtime
FROM openjdk:17-jdk

# Set the working directory in the container
WORKDIR /app

# Create the necessary directories
RUN mkdir -p /app/assets/videos
RUN mkdir -p /app/assets/captions
RUN mkdir -p /app/assets/images/course

# Copy the built JAR file from the build image to the runtime image
COPY --from=build /app/target/course-management-server-0.0.1-SNAPSHOT.jar .
COPY src/main/resources/application.properties_prod.disc /app/application.properties
COPY src/main/resources/application_prod.yml /app/application.yml
COPY src/main/resources/jasperreports_extension.properties /app/jasperreports_extension.properties
COPY src/main/resources/reports /app/reports
COPY src/main/resources/fonts /app/fonts

# Expose the port that the application will listen on
EXPOSE 8080

# Set the entry point for the container
ENTRYPOINT ["java", "-jar", "course-management-server-0.0.1-SNAPSHOT.jar"]
