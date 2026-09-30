# VouPassar backend — TASK 3.1
# Multi-stage: build com layout do repo (backend/ + database/),
# runtime mínimo na VPS. Nunca compilar na VPS (docs/architecture.md §9).
# Build context: raiz do repo (ver docker-compose.yml).
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build
COPY backend/pom.xml ./backend/pom.xml
RUN mvn -f backend/pom.xml -q dependency:go-offline || true
COPY backend/src ./backend/src
COPY database/migrations ./database/migrations
RUN mvn -f backend/pom.xml -q -DskipTests package \
 && cp backend/target/voupassar-backend-*.jar /app.jar

FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S app && adduser -S app -G app \
 && apk add --no-cache wget
USER app
WORKDIR /app
COPY --from=builder /app.jar ./app.jar
EXPOSE 8080
ENV JAVA_OPTS="-Xms256m -Xmx512m" \
    SPRING_PROFILES_ACTIVE="prod"
HEALTHCHECK --interval=20s --timeout=5s --start-period=40s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
