FROM amazoncorretto:17-al2-full
LABEL authors="Abriel"
WORKDIR /app
COPY target/auth-service-0.0.1-SNAPSHOT.jar auth-service.jar
CMD ["java","-jar","auth-service.jar"]
