plugins {
    id("orchestrator.java-conventions")
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.dependency.management)
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
description = "gateway"

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
    flatDir {
        dirs("libs")
    }
}

dependencies {
    implementation(libs.spring.web)
    implementation(libs.json.path)
    implementation(fileTree("libs") { include("*.jar") })

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.spring.configuration.processor)

    testImplementation(libs.spring.test)
    testRuntimeOnly(libs.junit.launcher)
}
