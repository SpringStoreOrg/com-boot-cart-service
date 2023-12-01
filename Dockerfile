FROM eclipse-temurin:17-jdk-alpine
COPY ./target/*.jar cart-service.jar 
ENTRYPOINT ["java","-jar","cart-service.jar"]