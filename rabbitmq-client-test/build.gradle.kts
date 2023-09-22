plugins {
    id("java")
}

group = "gg.bibleguessr"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")

    // RabbitMQ Implementation
    implementation("com.rabbitmq:amqp-client:5.18.0")

    // Jackson (for JSON)
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")

}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}