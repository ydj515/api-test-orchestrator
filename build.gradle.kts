import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    alias(libs.plugins.spotless)
    base
    jacoco
    id("orchestrator.java-conventions") apply false
}

repositories { mavenCentral() }

val modules = listOf("gateway", "mock-rest-api-server", "report-server", "karate-tests")

tasks.named("check") {
    dependsOn(modules.map { ":$it:check" })
}

tasks.named("assemble") {
    dependsOn(modules.map { ":$it:assemble" })
}

val verifyModuleGraph by tasks.registering(com.example.buildlogic.ModuleGraphCheck::class) {
    group = "verification"
    description = "Compare the architecture document with Gradle project dependencies"
    architectureDocument.set(layout.projectDirectory.file("docs/architecture.md"))
}

gradle.projectsEvaluated {
    val graph = subprojects.map { module ->
        val dependencies = module.configurations.flatMap { configuration ->
            configuration.dependencies.withType<ProjectDependency>().map { it.path }
        }.distinct().sorted()
        "${module.path} -> $dependencies"
    }
    verifyModuleGraph.configure { actualGraph.set(graph) }
}

tasks.named("check") {
    dependsOn(verifyModuleGraph, gradle.includedBuild("build-logic").task(":check"))
}

jacoco { toolVersion = libs.versions.jacoco.get() }

val coverageModules = listOf("gateway", "mock-rest-api-server", "report-server")
val jacocoRootReport by tasks.registering(JacocoReport::class) {
    group = "verification"
    description = "Aggregate production coverage from the three Spring modules"
    dependsOn(coverageModules.map { ":$it:jacocoTestReport" })
    executionData.from(coverageModules.map { layout.projectDirectory.file("$it/build/jacoco/test.exec") })
    classDirectories.from(coverageModules.map { layout.projectDirectory.dir("$it/build/classes/java/main") })
    sourceDirectories.from(coverageModules.map { layout.projectDirectory.dir("$it/src/main/java") })
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}
tasks.named("check") { dependsOn(jacocoRootReport) }

// Check changed/new Java files from this fixed adoption commit; CI fetches its history.
spotless {
    ratchetFrom("f4b2177b3e51ef05f8257458904a96114e21cffb")
    java {
        target("**/src/**/*.java")
        targetExclude("**/build/**", "**/.gradle/**", "**/node_modules/**")
        googleJavaFormat(libs.versions.googleJavaFormat.get()).aosp()
        trimTrailingWhitespace()
        endWithNewline()
    }
}

tasks.named("check") {
    dependsOn("spotlessCheck")
}
