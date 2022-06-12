FROM openjdk:11.0.11
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "-Duser.timezone=Asia/Ho_Chi_Minh", "/app.jar"]