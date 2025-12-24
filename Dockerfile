# Use Eclipse Temurin Java 21 as base image
FROM eclipse-temurin:21-jre

# Set working directory
WORKDIR /app

# Add a non-root user for security
RUN useradd -m appuser
USER appuser

# Copy the built jar from the target directory
COPY --chown=appuser:appuser target/E-Commerce-1.0-SNAPSHOT.jar app.jar

# Expose the port your app runs on
EXPOSE 8080


# Healthcheck for the application
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the jar file, loading secrets if present
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.location=file:/etc/secrets/application.properties"]

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]
