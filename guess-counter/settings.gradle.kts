rootProject.name = "guess-counter"

// Include the service-wrapper and
// backend-utils projects from
// the parent directory
include(":service-wrapper")
project(":service-wrapper").projectDir = File("../service-wrapper")

include(":backend-utils")
project(":backend-utils").projectDir = File("../backend-utils")