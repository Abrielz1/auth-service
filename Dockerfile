FROM openjdk:17-jdk-slim
LABEL authors="Abriel"
WORKDIR /app
COPY target/auth-service-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["top", "-b"]