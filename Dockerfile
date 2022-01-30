FROM openjdk:11
COPY ./target/*.jar cart-service.jar 
ENTRYPOINT ["java","-jar","cart-service.jar"]