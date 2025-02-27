# Use a base image with Kotlin & JDK
FROM openjdk:21-jdk

# Set working directory inside the container
WORKDIR /app

# Copy the built application JAR file
COPY build/libs/gassets.jar /app/gassets.jar

# Expose the application port (adjust if needed)
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "/app/gassets.jar"]