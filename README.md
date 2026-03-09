# TourGuide
TourGuide is a Spring Boot API that is part of the OpenClassrooms Java Development course.

It is a mock API doing various business logics and mocking external API calls with long response times.
The challenge was to implement CompletableFuture and ExecutorService to deal with those long response times in an async manner.
The application is containerised with Docker, and a CI pipeline builds and validates the code on every push or pull request.

# Technologies used
* Core
  *  **java 17**: The project uses the Long-Term Support version of Java. Eclipse Temurin EOL Oct 2027.
  *  **Spring Boot 3.1.1**: Java framework that simplifies the development of Spring applications by providing auto-configuration and a built-in embedded Tomcat server.
  *  **Maven**: Dependency management and build tool.
* Testing
  *  JUnit 5 & Spring Boot Starter Test: Standard testing framework for unit and integration tests.

# Installation & Run
**Prerequisites**
*  docker

**Installation**
*  download source code
*  decompresse .zip file
*  cd into decompressed source folder

*  build the image
```bash
docker build -t tourguide-app .
```
*  start the container
```bash 
docker run -d -p 8080:8080 --name tourguide tourguide-app
```
**Access**

The JSON payload is viewable on port 8080 with a web browser or Postman.
See the controller class for available paths and functionalities.

**Stop**
```bash
docker stop tourguide
```
