FROM eclipse-temurin:17-jdk-focal

WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw -s .mvn/settings.xml dependency:go-offline

COPY src ./src

RUN ./mvnw -s .mvn/settings.xml package -DskipTests

EXPOSE 8761

CMD ["java", "-jar", "target/discovery-service-0.0.1-SNAPSHOT.jar"]
