plugins {
    id("orchestrator.java-conventions")
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.karate)
    testRuntimeOnly(libs.junit.launcher)
}

tasks.test {
    useJUnitPlatform { excludeTags("e2e") }
}

tasks.register<Test>("e2eTest") {
    description = "Run Karate scenarios against the configured gateway and mock server"
    group = "verification"
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    useJUnitPlatform { includeTags("e2e") }
    systemProperty("karate.output.dir", layout.buildDirectory.dir("karate-reports").get().asFile.absolutePath)
    outputs.dir(layout.buildDirectory.dir("karate-reports"))
    // A remote service can change without any local source changing.
    outputs.upToDateWhen { false }
    listOf("GATEWAY_URL", "ORG", "SERVICE", "API").forEach { key ->
        providers.environmentVariable(key).orNull?.let { environment(key, it) }
    }
}
