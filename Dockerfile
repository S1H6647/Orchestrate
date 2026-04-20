FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace

# Copy Maven wrapper and project metadata first for better layer caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw -B -q -DskipTests dependency:go-offline

# Copy source and build fat jar
COPY src ./src
RUN ./mvnw -B -q -DskipTests package


FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Run as non-root user
RUN addgroup -S spring && adduser -S spring -G spring

# Copy built application jar
COPY --from=build /workspace/target/*.jar /app/app.jar

USER spring:spring
EXPOSE 8081

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-XX:+UseG1GC", "-jar", "/app/app.jar"]
