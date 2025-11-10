# Etapa 1: compilar o projeto com Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copia o pom.xml e baixa dependências
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia o código fonte e compila
COPY src appAdoteJa/src
RUN mvn clean package -DskipTests

# Etapa 2: imagem final, mais leve
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Copia o .jar da fase anterior
COPY --from=build /app/target/*.jar app.jar

# Expõe a porta padrão
EXPOSE 8080

# Comando de inicialização
ENTRYPOINT ["java", "-jar", "app.jar"]
