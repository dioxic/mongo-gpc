import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.10"
    kotlin("plugin.serialization") version "1.4.10"
    id("com.github.ben-manes.versions") version "0.33.0"
}

group = "uk.dioxic.mongo"
version = "1.0-SNAPSHOT"

repositories {
    jcenter()
    mavenCentral()
}

val log4Version = "2.13.3"
val junitVersion = "5.7.0"
val assertjVersion = "3.17.2"
val mongoVersion = "4.1.1"
val coroutinesVersion = "1.3.9"
val kluentVersion = "1.63"
val jacksonVersion = "2.11.1"
val opencsvVersion = "5.2"
val kmongoVersion = "4.1.3"

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.mongodb:mongodb-driver-sync:$mongoVersion")
    implementation(platform("org.apache.logging.log4j:log4j-bom:$log4Version"))
    implementation("org.apache.logging.log4j:log4j-core")
    implementation("org.apache.logging.log4j:log4j-api")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl")
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:0.11.1")
    implementation("com.vhl.blackmo:kotlin-grass-jvm:0.4.1")
    implementation("org.litote.kmongo:kmongo-serialization:$kmongoVersion")
    implementation("com.github.ajalt.clikt:clikt:3.0.1")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testImplementation("org.amshove.kluent:kluent:$kluentVersion")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
