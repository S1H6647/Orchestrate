FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw -B -q -DskipTests dependency:go-offline
COPY src ./src
RUN ./mvnw -B -q -DskipTests package -am && \
    rm -f target/*-plain.jar

FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
COPY --from=build /workspace/target/*.jar app.jar
USER spring:spring
EXPOSE 8081
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+UseG1GC", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dspring.jmx.enabled=false", \
    "-jar", "app.jar"]