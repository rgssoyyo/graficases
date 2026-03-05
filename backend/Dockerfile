FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . .

RUN chmod +x mvnw && ./mvnw package -DskipTests

EXPOSE 8080

CMD ["java","-jar","target/*.jar"]