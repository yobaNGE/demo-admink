FROM eclipse-temurin:17-jdk-alpine AS build
ENTRYPOINT ["java", "-jar", "app.jar"]
EXPOSE 8080
COPY --from=build /app/target/*.jar app.jar
WORKDIR /app
FROM eclipse-temurin:17-jre-alpine

RUN ./mvnw clean package -DskipTests
RUN chmod +x mvnw
COPY src src
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
WORKDIR /app

