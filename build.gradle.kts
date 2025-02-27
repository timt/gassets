plugins {
    kotlin("jvm") version "2.1.0"
    id("com.bmuschko.docker-remote-api") version "9.4.0"
}

group = "io.shaka"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core:3.1.0")
    implementation("io.ktor:ktor-server-netty:3.1.0")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

// Ensure dependencies are packaged correctly
tasks.register<Jar>("fatJar") {
    archiveBaseName.set("gassets")
    archiveFileName.set("gassets.jar") // Ensure the expected filename
    destinationDirectory.set(file("$buildDir/libs")) // Save in build/libs/

    manifest {
        attributes["Main-Class"] = "io.shaka.MainKt"
    }
    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }
    })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// Task to build the Docker image
tasks.register("dockerBuild", com.bmuschko.gradle.docker.tasks.image.DockerBuildImage::class) {
    dependsOn("fatJar")
    inputDir.set(file("."))  // Set the build context to the root of the project
    images.add("gassets:latest")
}

// Task to push the Docker image to a registry
tasks.register("dockerPush", com.bmuschko.gradle.docker.tasks.image.DockerPushImage::class) {
    dependsOn("dockerBuild")
    images.add("your-dockerhub-username/gassets:latest")
    registryCredentials {
        username.set(System.getenv("DOCKER_HUB_USERNAME"))
        password.set(System.getenv("DOCKER_HUB_PASSWORD"))
    }
}