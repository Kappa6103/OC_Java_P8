FROM eclipse-temurin:17-jre

WORKDIR /opt/springboot

COPY ./target/tourguide-0.0.1-SNAPSHOT.jar tourguide-0.0.1-SNAPSHOT.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/opt/springboot/tourguide-0.0.1-SNAPSHOT.jar"]