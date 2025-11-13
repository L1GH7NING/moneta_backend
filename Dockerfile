# Stage 1: Build the application using the official Maven image for JDK 21
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

# Set the working directory
WORKDIR /app

# Copy the Maven project definition files
COPY pom.xml .
COPY src ./src

# Build the application, creating the executable JAR
RUN mvn clean install -DskipTests

# Stage 2: Create the final, lightweight runtime image using the official Eclipse Temurin JRE 21
FROM eclipse-temurin:21-jre-alpine

# Set the working directory
WORKDIR /app

# Copy the executable JAR from the 'build' stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port your Spring Boot app runs on (default is 8080)
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]