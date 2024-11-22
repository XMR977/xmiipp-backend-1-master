# Docker 镜像构建
# Use an OpenJDK base image for running Java
FROM openjdk:17-alpine
# Copy local code to the container image.
WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build a release artifact.
RUN mvn package -DskipTests

# Expose the port your Spring Boot app listens on (e.g., 8080)
EXPOSE 8101

# Run the web service on container startup.
CMD ["java","-jar","/app/target/xmiipp-backend-0.0.1-SNAPSHOT.jar","--spring.profiles.active=prod"]