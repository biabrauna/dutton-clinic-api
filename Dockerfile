# ── Stage 1: build ────────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Baixa dependências antes do código-fonte (melhor aproveitamento do cache de camadas)
COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn package -DskipTests -q

# ── Stage 2: runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Usuário não-root por segurança
RUN addgroup -S clinica && adduser -S clinica -G clinica
USER clinica

COPY --from=builder /app/target/clinica-api-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]
