FROM openjdk:11-jre-slim
EXPOSE 8080
ARG JAR_FILE=target/party-part-service-1.0.0.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","-Dspring.profiles.active=prod","/app.jar"]