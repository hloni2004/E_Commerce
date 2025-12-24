# Use Eclipse Temurin Java 21 as base image
FROM eclipse-temurin:21-jre

# Set working directory
WORKDIR /app

# Copy the built jar from the target directory
COPY target/E-Commerce-1.0-SNAPSHOT.jar app.jar

# Expose the port your app runs on
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]
