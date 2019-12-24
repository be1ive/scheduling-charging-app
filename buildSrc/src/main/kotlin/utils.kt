
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin

const val junitVersion = "5.3.2"

/**
 * Configures the current project as a Kotlin project by adding the Kotlin `stdlib` as a dependency.
 */
fun Project.kotlinProject() {
    dependencies {
        // Kotlin libs
        "implementation"(kotlin("stdlib-jdk8"))
        "implementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3")

        // Logging
        "implementation"("org.slf4j:slf4j-simple:1.7.25")
        "implementation"("io.github.microutils:kotlin-logging:1.6.22")

        // Mockk
        "testImplementation"("io.mockk:mockk:1.9")

        // JUnit 5
        "testImplementation"("org.junit.jupiter:junit-jupiter-api:$junitVersion")
        "testImplementation"("org.junit.jupiter:junit-jupiter-params:$junitVersion")
        "testImplementation"("io.mockk:mockk:1.9.3")
        "runtime"("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    }
}

/**
 * Configures data layer libs needed for interacting with the DB
 */
fun Project.dataLibs() {
    dependencies {
        "implementation"("org.jetbrains.exposed:exposed-core:0.19.3")
        "implementation"("org.jetbrains.exposed:exposed-dao:0.19.3")
        "implementation"("org.jetbrains.exposed:exposed-jdbc:0.19.3")
        "implementation"("org.jetbrains.exposed:exposed-java-time:0.19.3")
        "implementation"("org.xerial:sqlite-jdbc:3.25.2")
    }
}