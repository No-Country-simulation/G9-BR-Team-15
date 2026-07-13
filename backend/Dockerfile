# --- Etapa 1: Build do projeto ---
FROM maven:3.9.9-eclipse-temurin-17-alpine AS builder
WORKDIR /build

# Copia apenas o arquivo de configuração para cachear as dependências
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o código-fonte e compila gerando o arquivo .jar do artefato "api"
COPY src ./src
RUN mvn clean package -DskipTests

# --- Etapa 2: Execução da API ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Cria um usuário sem privilégios de root por segurança
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copia o arquivo .jar gerado na Etapa 1
COPY --from=builder /build/target/api-0.0.1-SNAPSHOT.jar app.jar

# Porta padrão exposta pelo contêiner
EXPOSE 8080

# Comando que inicia o contêiner rodando o Spring Boot no Java 17
ENTRYPOINT ["java", "-jar", "app.jar"]
