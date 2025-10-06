# =============================================================================
# MULTI-STAGE DOCKERFILE - Spring Boot Application
# =============================================================================

# Stage 1: Build da aplicação com Gradle
FROM gradle:9.1-jdk21-alpine AS builder

WORKDIR /app

# Copiar arquivos de configuração do Gradle
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# Download de dependências (cache layer)
RUN gradle dependencies --no-daemon || return 0

# Copiar código fonte
COPY src ./src

# Build da aplicação (sem rodar testes para acelerar - rode testes no CI/CD)
RUN gradle clean bootJar -x test --no-daemon

# =============================================================================
# Stage 2: Imagem de produção otimizada
# =============================================================================
FROM eclipse-temurin:25-jre-alpine

# Instalar dependências necessárias
RUN apk add --no-cache \
    curl \
    tzdata \
    && cp /usr/share/zoneinfo/America/Sao_Paulo /etc/localtime \
    && echo "America/Sao_Paulo" > /etc/timezone \
    && apk del tzdata

# Criar usuário não-root para segurança
RUN addgroup -S spring && adduser -S spring -G spring

# Diretório de trabalho
WORKDIR /app

# Copiar JAR do build stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Alterar ownership para usuário não-root
RUN chown -R spring:spring /app

# Trocar para usuário não-root
USER spring:spring

# Expor porta da aplicação
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/api/actuator/health || exit 1

# JVM Options para produção
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:InitialRAMPercentage=50.0 \
               -XX:+HeapDumpOnOutOfMemoryError \
               -XX:HeapDumpPath=/tmp/heapdump.hprof \
               -Djava.security.egd=file:/dev/./urandom"

# Executar aplicação
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
