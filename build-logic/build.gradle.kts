plugins {
    `java-gradle-plugin`
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

gradlePlugin {
    plugins {
        register("javaConventions") {
            id = "orchestrator.java-conventions"
            implementationClass = "com.example.buildlogic.JavaConventionsPlugin"
        }
    }
}

val verification by sourceSets.creating {
    compileClasspath += sourceSets.main.get().output + sourceSets.main.get().compileClasspath
    runtimeClasspath += sourceSets.main.get().output + sourceSets.main.get().runtimeClasspath
}

val verifyArchitectureRules by tasks.registering(JavaExec::class) {
    group = "verification"
    description = "Exercise allowed and forbidden package dependency fixtures"
    classpath = verification.runtimeClasspath
    mainClass.set("com.example.buildlogic.ArchitectureRulesTest")
}

tasks.check {
    dependsOn(verifyArchitectureRules)
}

dependencies {
    implementation(libs.spotbugs.plugin)
}

sourceSets.main {
    java.srcDir("../config/architecture/src/test/java/architecture/policy")
}
