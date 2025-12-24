# -------- BUILD STAGE --------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN ./mvnw package -DskipTests || mvn package -DskipTests


# -------- RUNTIME STAGE --------
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN useradd -m appuser
USER appuser

COPY --from=build /app/target/E-Commerce-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
