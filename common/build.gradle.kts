plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("be.bluexin:kaeron")

    implementation("it.unimi.dsi", "fastutil", version("fastutil"))

    implementation("net.onedaybeard.artemis", "artemis-odb", version("artemis"))
    implementation("net.onedaybeard.artemis", "artemis-odb-serializer-kryo", version("artemis"))
    implementation("net.onedaybeard.artemis", "artemis-odb-serializer-json", version("artemis"))

    implementation("com.github.javafaker", "javafaker", version("javafaker"))
    implementation("com.fasterxml.jackson.module", "jackson-module-kotlin", version("jackson"))

    // Logging
    implementation("org.slf4j", "slf4j-api", version("slf4j"))
    implementation("io.github.microutils", "kotlin-logging", version("klog"))
    runtimeOnly("ch.qos.logback", "logback-classic", version("logback"))
}