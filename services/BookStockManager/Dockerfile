FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy Maven settings and wrapper
COPY ../../.mvn/settings.xml /root/.m2/
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# Set Maven options for better network handling
ENV MAVEN_OPTS="-Dmaven.wagon.http.pool=false -Dmaven.wagon.http.retryHandler.count=3 -Dmaven.wagon.httpconnectionManager.ttlSeconds=120"

# Download dependencies first
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B dependency:go-offline -Dmaven.wagon.http.retryHandler.count=3

# Copy source and build
COPY src ./src/
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B clean package -DskipTests -Dmaven.wagon.http.retryHandler.count=3

# Verify JAR exists
RUN ls -la target/

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the JAR file
COPY --from=builder /app/target/*.jar ./app.jar

# Set the port according to the configuration memory
EXPOSE 9093

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
