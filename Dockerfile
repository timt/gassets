# Use a base image with Kotlin & JDK
FROM bellsoft/liberica-openjdk-alpine:21

# Set working directory inside the container
WORKDIR /app

# Copy the built application JAR file
COPY build/libs/gassets.jar /app/gassets.jar

# Expose the application port (adjust if needed)
EXPOSE 8000

# Run the application
CMD ["java", "-jar", "/app/gassets.jar"]