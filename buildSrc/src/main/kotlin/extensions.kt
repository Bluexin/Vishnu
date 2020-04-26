import org.gradle.api.Project

fun Project.hasProp(name: String): Boolean = name in project.properties

fun Project.prop(name: String): String? = project.properties[name] as? String

fun Project.version(name: String) = project.properties["${name}_version"] as? String

fun Project.coroutine(module: String): Any =
    "org.jetbrains.kotlinx:kotlinx-coroutines-$module:${version("coroutines")}"

fun micronaut(module: String) = "io.micronaut:micronaut-$module"
fun micronautConfig(module: String) = "io.micronaut.configuration:micronaut-$module"