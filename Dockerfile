# Estágio 1: Builder
# Utilizamos uma imagem oficial do Maven com JDK 21 para compilar o código
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Primeiro, copiamos apenas o pom.xml e baixamos as dependências.
# Isso garante o cache das camadas do Docker se o pom.xml não for alterado.
COPY pom.xml .
RUN mvn dependency:go-offline

# Depois copiamos o código-fonte e compilamos
COPY src ./src
RUN mvn clean package -DskipTests

# Estágio 2: Runtime
# Utilizamos uma imagem enxuta apenas com o JRE 21 para rodar o app
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiamos o JAR gerado no estágio anterior
COPY --from=builder /app/target/ecommerce.api-0.0.1-SNAPSHOT.jar app.jar

# Expõe a porta configurada no application.properties
EXPOSE 8080

# Comando para iniciar o servidor Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]
