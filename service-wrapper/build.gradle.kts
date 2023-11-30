import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
  `java-library`
}

group = "gg.bibleguessr"
version = "1.0.0-SNAPSHOT"

repositories {
  mavenCentral()
}

val vertxVersion = "4.4.5"
val junitJupiterVersion = "5.9.1"

dependencies {

  implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))
  implementation("io.vertx:vertx-web-validation")
  implementation("io.vertx:vertx-web")
  testImplementation("io.vertx:vertx-junit5")
  testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")

  // Bibleguessr Backend Utils
  implementation(project(":backend-utils"))

  // JSON configuration
  implementation("com.fasterxml.jackson.core:jackson-databind:2.15.1")

  // Logging provider
  implementation("org.slf4j:slf4j-api:2.0.7")

  // RabbitMQ
  implementation("com.rabbitmq:amqp-client:5.18.0")

}

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging {
    events = setOf(PASSED, SKIPPED, FAILED)
  }
}
