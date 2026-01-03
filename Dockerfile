# -------- BUILD STAGE --------
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn package -DskipTests


# -------- RUNTIME STAGE --------
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN useradd -m appuser
USER appuser

COPY --from=build /app/target/E-Commerce-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080
 
# Allow configuring CORS origins at container runtime via env var
# Example: APP_CORS_ALLOWED_ORIGINS=https://client-hub-portal.vercel.app
ENV APP_CORS_ALLOWED_ORIGINS="https://client-hub-portal.vercel.app"

ENTRYPOINT ["java", "-jar", "app.jar"]
