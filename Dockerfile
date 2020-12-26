FROM openjdk:8-jdk-alpine
EXPOSE 8080
ARG JAR_FILE=/home/runner/work/party-part-service/party-part-service/target/party-part-service-1.0.0.jar
ADD ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]