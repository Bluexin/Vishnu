rootProject.name = "Vishnu"

includeBuild("kaeron") {
    dependencySubstitution {
        substitute(module("be.bluexin:kaeron")).with(project(":"))
    }
}

include(":server")

val kotlin_version: String by settings


pluginManagement {
    resolutionStrategy {
        eachPlugin {
            when (requested.id.namespace) {
                "org.jetbrains.kotlin" -> useVersion(kotlin_version)
                "org.jetbrains.kotlin.plugin" -> useVersion(kotlin_version)
            }
        }
    }
}
