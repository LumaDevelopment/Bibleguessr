import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    `java-library`
}

group = "gg.bibleguessr.guess_counter"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val junitJupiterVersion = "5.9.1"

dependencies {

    // Service Wrapper
    implementation(project(":service-wrapper"))
    implementation(project(":backend-utils"))

    // JSON configuration
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.1")

    // Logging provider
    implementation("org.slf4j:slf4j-api:2.0.7")

    // Annotations
    implementation("org.jetbrains:annotations:24.0.1")

    testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")

}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
    }
}