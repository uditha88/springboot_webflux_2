# docker build -t uditha/spring_webflux_2:latest .
# docker  push uditha/spring_webflux_2:latest
# docker run -p 8081:8081 uditha/spring_webflux_2:latest (this is the basic command that works without compose)

# ---------- Build Stage ----------
# FROM maven:3.9.6-eclipse-temurin-17 AS builder (this is too heavy)

FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# ---------- Run Stage ----------
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8081
# the above is just documentation. no use

ENTRYPOINT ["java", "-jar", "app.jar"]