# ── Stage 1: Build ───────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom first — layer cache means deps only re-download when pom changes
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copy source and produce the fat JAR
COPY src ./src
RUN mvn clean package -DskipTests -q

# ── Stage 2: Runtime (lean image) ────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Run as non-root for security
RUN addgroup -S vedu && adduser -S vedu -G vedu
USER vedu

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
