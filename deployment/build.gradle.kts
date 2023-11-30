import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "gg.bibleguessr.deployment"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val junitJupiterVersion = "5.9.1"

dependencies {

    // Service Wrapper
    implementation(project(":service-wrapper"))
    implementation(project(":backend-utils"))
    implementation(project(":bible-service"))
    implementation(project(":guess-counter"))

    // JSON configuration
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.1")

    // Logging provider
    implementation("ch.qos.logback:logback-classic:1.4.13")

    // Annotations
    implementation("org.jetbrains:annotations:24.0.1")

    testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")

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