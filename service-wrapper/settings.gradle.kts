rootProject.name = "service_wrapper"

// Import our backend utilities
include(":backend-utils")
project(":backend-utils").projectDir = File("../backend-utils")
