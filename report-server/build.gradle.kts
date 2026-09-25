plugins {
    id("orchestrator.java-conventions")
    alias(libs.plugins.spring.boot.report)
    alias(libs.plugins.dependency.management)
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
description = "report-server"

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.spring.web)
    implementation(libs.spring.thymeleaf)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.jsr310)
    implementation(libs.snakeyaml)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    testImplementation(libs.spring.test)
    testRuntimeOnly(libs.junit.launcher)
}


tasks.register<JavaExec>("publishKarate") {
    group = "reporting"
    description = "Publish Karate reports into REPORT_DATA_DIR without starting the web server"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.example.reportserver.ReportServerApplication")
    args("publish-karate")
}

tasks.register<JavaExec>("publishCats") {
    group = "reporting"
    description = "Publish CATS reports into REPORT_DATA_DIR without starting the web server"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.example.reportserver.ReportServerApplication")
    args("publish-cats")
}
