FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY . .

RUN ./mvnw -DskipTests clean package

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "target/mycash-0.0.1-SNAPSHOT.jar"]