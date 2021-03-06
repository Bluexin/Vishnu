import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformJvmPlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.noarg.gradle.NoArgGradleSubplugin

plugins {
    `maven-publish`
    kotlin("jvm")
    kotlin("plugin.noarg")
}

group = "be.bluexin"
version = "1.0-SNAPSHOT"

java {
    withSourcesJar()
}

repositories {
    mavenCentral()
}

subprojects {
    apply<MavenPublishPlugin>()
    apply<KotlinPlatformJvmPlugin>()
    apply<NoArgGradleSubplugin>()

    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenCentral()
    }

    java {
        withSourcesJar()
    }

    dependencies {
        implementation(kotlin("stdlib-jdk8"))
        implementation(coroutine("jdk8"))
        api("org.jetbrains:annotations:13.0")

        // Testing
        testImplementation("org.junit.jupiter", "junit-jupiter-api", version("junit"))
        testImplementation("org.junit.jupiter", "junit-jupiter-params", version("junit"))
        testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", version("junit"))
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            javaParameters = true
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

    publishing {
        publications.create<MavenPublication>("publication") {
            from(components["java"])
            this.artifactId = "${rootProject.name}-${base.archivesBaseName}".toLowerCase()
        }

        repositories {
            val mavenPassword = if (hasProp("local")) null else prop("sbxMavenPassword")
            maven {
                url = uri(if (mavenPassword != null) "sftp://maven.sandboxpowered.org:22/sbxmvn/" else "file://$buildDir/repo")
                if (mavenPassword != null) {
                    credentials(PasswordCredentials::class.java) {
                        username = prop("sbxMavenUser")
                        password = mavenPassword
                    }
                }
            }

        }
    }
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}

fun Project.version(name: String) = extra.properties["${name}_version"] as? String

fun Project.coroutine(module: String): Any =
    "org.jetbrains.kotlinx:kotlinx-coroutines-$module:${version("coroutines")}"
