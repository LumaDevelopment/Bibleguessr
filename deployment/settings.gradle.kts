rootProject.name = "deployment"

include(":service-wrapper")
project(":service-wrapper").projectDir = File("../service-wrapper")

include(":backend-utils")
project(":backend-utils").projectDir = File("../backend-utils")

include(":bible-service")
project(":bible-service").projectDir = File("../bible-service")

include(":guess-counter")
project(":guess-counter").projectDir = File("../guess-counter")