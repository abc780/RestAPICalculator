#Calculator

FROM openjdk:23-jdk

COPY target/calculator-0.0.1-SNAPSHOT.jar calculator.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "calculator.jar"]
