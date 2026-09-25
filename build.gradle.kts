plugins {
    base
    id("orchestrator.java-conventions") apply false
}

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
