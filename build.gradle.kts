import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformJvmPlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.noarg.gradle.NoArgGradleSubplugin

plugins {
    kotlin("jvm")
    kotlin("plugin.noarg")
}

group = "be.bluexin"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

subprojects {
    apply<KotlinPlatformJvmPlugin>()
    apply<NoArgGradleSubplugin>()

    dependencies {
        implementation(kotlin("stdlib-jdk8"))
        implementation(coroutine("jdk8"))

        // Testing
        testImplementation("org.junit.jupiter", "junit-jupiter-api", version("junit"))
        testImplementation("org.junit.jupiter", "junit-jupiter-params", version("junit"))
        testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", version("junit"))
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            @Suppress("SuspiciousCollectionReassignment")
            freeCompilerArgs += listOf("-Xuse-experimental=kotlin.Experimental", "-XXLanguage:+InlineClasses")
        }
    }

    tasks.test {
        useJUnitPlatform()
    }

    tasks.withType<AbstractArchiveTask> {
        archiveBaseName.convention(provider { project.name.toLowerCase() })
    }

    noArg {
        annotation("be.bluexin.vishnu.Noarg")
    }
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}

fun Project.version(name: String) = extra.properties["${name}_version"] as? String

fun Project.coroutine(module: String): Any =
    "org.jetbrains.kotlinx:kotlinx-coroutines-$module:${version("coroutines")}"
