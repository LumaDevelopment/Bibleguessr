rootProject.name = "bible-service"

// Include the service-wrapper project from
// the parent directory
include(":service-wrapper")
project(":service-wrapper").projectDir = File("../service-wrapper")