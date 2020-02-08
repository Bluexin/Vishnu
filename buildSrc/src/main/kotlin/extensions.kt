import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project
import org.gradle.api.tasks.Exec

fun Project.hasProp(name: String): Boolean = name in project.properties

fun Project.prop(name: String): String? = project.properties[name] as? String

fun Project.version(name: String) = project.properties["${name}_version"] as? String

fun Project.coroutine(module: String): Any =
    "org.jetbrains.kotlinx:kotlinx-coroutines-$module:${version("coroutines")}"
