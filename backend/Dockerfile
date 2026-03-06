FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY backend .

RUN chmod +x mvnw
RUN ./mvnw package -DskipTests

EXPOSE 8080

CMD ["java","-jar","/app/target/backend-0.0.1-SNAPSHOT.jar"]