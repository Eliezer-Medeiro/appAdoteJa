# Etapa 1: build do app
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY appAdoteJa/pom.xml .
COPY appAdoteJa/src ./src
RUN mvn clean package -DskipTests

# Etapa 2: rodar o app
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
