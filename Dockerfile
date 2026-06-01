# Stage 1: Build the frontend (Vite/React)
FROM node:20 AS frontend-build
WORKDIR /app/frontend

# Copy frontend source
COPY React/package*.json ./
RUN npm install
COPY React/ ./

# Build the frontend - Output will be in /app/frontend/dist
RUN npm run build

# Stage 2: Build the backend (Spring Boot / Maven)
FROM maven:3.9.6-eclipse-temurin-17 AS backend-build
WORKDIR /app/backend

# Copy backend source
COPY Spring-Boot/pom.xml .
COPY Spring-Boot/src ./src

# Copy the built frontend into Spring Boot's static folder
# This allows Spring Boot to serve the frontend files
COPY --from=frontend-build /app/frontend/dist ./src/main/resources/static

# Build the backend jar (skipping tests for faster build)
RUN mvn clean package -DskipTests

# Stage 3: Create the final runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built jar from backend-build stage
COPY --from=backend-build /app/backend/target/QuizApplication-0.0.1-SNAPSHOT.jar app.jar

# Expose the standard Spring Boot port
EXPOSE 8080

# Set default profile to prod (can be overridden at runtime)
ENV SPRING_PROFILES_ACTIVE=prod

# Run the application using java -jar and the profiles concept
ENTRYPOINT ["java", "-jar", "app.jar"]
