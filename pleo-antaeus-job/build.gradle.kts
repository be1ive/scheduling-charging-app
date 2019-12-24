plugins {
    kotlin("jvm")
}

kotlinProject()

dependencies {
    implementation(project(":pleo-antaeus-core"))
    compile(project(":pleo-antaeus-models"))
    runtime("org.postgresql:postgresql:42.2.6")
}