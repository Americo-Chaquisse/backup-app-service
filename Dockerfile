FROM amazoncorretto:8u252

ARG JAR_FILE=target/backup-app-server.jar

WORKDIR /usr/local/runme

COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]

EXPOSE 8080