FROM openjdk:8-jdk-alpine
EXPOSE 8080
RUN ls .
RUN ls ../
RUN ls /
WORKDIR /home/runner/work/party-part-service/party-part-service/target
ARG JAR_FILE=party-part-service-1.0.0.jar
ADD ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]