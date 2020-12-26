FROM openjdk:8-jdk-alpine
EXPOSE 8080
ARG JAR_FILE=target/party-part-service-1.0.0.jar
COPY --from=build ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]