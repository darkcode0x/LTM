# Multi-stage build for VideoConverter
# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
# Copy sources
COPY src ./src
RUN mvn -q -DskipTests clean package

# Runtime stage with Tomcat + FFmpeg
FROM tomcat:10.1-jdk17-temurin

# Install ffmpeg (for Linux based container)
RUN apt-get update \
    && apt-get install -y --no-install-recommends ffmpeg \
    && rm -rf /var/lib/apt/lists/*

# Remove default ROOT and deploy our WAR as ROOT
RUN rm -rf /usr/local/tomcat/webapps/ROOT
COPY --from=build /app/target/video-converter-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war

# Create uploads directories (avatars, videos)
RUN mkdir -p /usr/local/tomcat/webapps/uploads/avatars \
    && mkdir -p /usr/local/tomcat/webapps/uploads/videos

# Environment variables for DB connection (match your code expectations)
ENV DB_HOST=db \
    DB_PORT=3306 \
    DB_NAME=videodb \
    DB_USER=videouser \
    DB_PASSWORD=videopass

EXPOSE 8080

# Healthcheck script (simple curl on root)
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/ || exit 1

# Run Tomcat
CMD ["catalina.sh", "run"]
