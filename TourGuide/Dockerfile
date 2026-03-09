FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
COPY libs ./libs
COPY src ./src

RUN mvn package -DskipTests -B

FROM eclipse-temurin:17-jre

WORKDIR /opt/springboot

COPY --from=build /app/target/tourguide-0.0.1-SNAPSHOT.jar tourguide-0.0.1-SNAPSHOT.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/opt/springboot/tourguide-0.0.1-SNAPSHOT.jar"]