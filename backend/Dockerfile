# Stage 1: Build the JAR
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Build the final image
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/exercise4you-1.1.0.jar app.jar
EXPOSE 80
ENTRYPOINT ["java", "-jar", "app.jar"]