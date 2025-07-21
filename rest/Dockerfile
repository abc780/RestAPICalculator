#Rest

FROM openjdk:23-jdk

COPY target/rest-0.0.1-SNAPSHOT.jar rest.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "rest.jar"]
