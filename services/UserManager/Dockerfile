FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy Maven settings and wrapper
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# Set Maven options for better network handling and caching
ENV MAVEN_OPTS="-Dmaven.wagon.http.pool=false -Dmaven.wagon.http.retryHandler.count=5 -Dmaven.wagon.httpconnectionManager.ttlSeconds=240 -Dmaven.wagon.http.retryHandler.requestSentEnabled=true"

# Create .m2 directory and set permissions
RUN mkdir -p /root/.m2 \
    && chmod 777 /root/.m2

# Download dependencies first with retry mechanism
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B dependency:go-offline \
    -Dmaven.wagon.http.retryHandler.count=5 \
    -Dmaven.wagon.http.pool=false \
    -Dmaven.wagon.rto=60000

# Copy source and build with retry mechanism
COPY src ./src/
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B clean package \
    -DskipTests \
    -Dmaven.wagon.http.retryHandler.count=5 \
    -Dmaven.wagon.http.pool=false \
    -Dmaven.wagon.rto=60000

# Verify JAR exists
RUN ls -la target/

# Runtime stage
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the JAR file
COPY --from=builder /app/target/*.jar ./app.jar

# Set the port according to the configuration memory
EXPOSE 9090

# Set JVM options for better container performance
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0 -Djava.security.egd=file:/dev/./urandom"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
