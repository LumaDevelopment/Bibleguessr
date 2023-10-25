import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    java
    application
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "gg.bibleguessr"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val vertxVersion = "4.4.5"
val junitJupiterVersion = "5.9.1"

application {
    mainClass.set("gg.bibleguessr.api_gateway.APIGateway")
}

dependencies {

    // Vert.x & Testing
    implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))
    implementation("io.vertx:vertx-web-validation")
    implementation("io.vertx:vertx-web")
    testImplementation("io.vertx:vertx-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")

    // Bibleguessr Service Wrapper and Backend Utils
    implementation(project(":backend-utils"))
    implementation(project(":service-wrapper"))

    // JSON configuration
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.1")

    // Logging provider
    implementation("org.slf4j:slf4j-simple")

    // RabbitMQ
    implementation("com.rabbitmq:amqp-client:5.18.0")

    // Making HTTP requests
    // https://mvnrepository.com/artifact/com.squareup.okhttp3/okhttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<ShadowJar> {
    archiveClassifier.set("fat")
    mergeServiceFiles()
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
    }
}
