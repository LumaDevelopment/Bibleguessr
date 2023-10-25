import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    `java-library`
}

group = "gg.bibleguessr"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val junitJupiterVersion = "5.9.1"

dependencies {
    testImplementation(platform("org.junit:junit-bom:$junitJupiterVersion"))
    testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")

    // JSON configuration
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.1")

    // Logging provider
    implementation("org.slf4j:slf4j-api:2.0.7")

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
