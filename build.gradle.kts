import io.gitlab.arturbosch.detekt.Detekt
import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion: String by project
val springBootVersion: String by project
val springCloudVersion: String by project
val mavenUser: String by project
val mavenPassword: String by project
val weBootStarterVersion: String by project
val testContainers: String by project
val jacksonKotlinVersion: String by project

plugins {
    val springBootVersion = "2.2.1.RELEASE"
    val kotlinVersion = "1.3.70"
    val gradleDependencyManagementVersion = "1.0.8.RELEASE"
    val detektVersion = "1.1.0"
    val gitPropertiesVersion = "2.2.2"
    val vstContractDockerPluginVersion = "2.1.6-SNAPSHOT"

    kotlin("jvm") version kotlinVersion apply false
    kotlin("plugin.spring") version kotlinVersion apply false
    kotlin("plugin.jpa") version kotlinVersion apply false
    `maven-publish`
    id("org.springframework.boot") version springBootVersion apply false
    id("io.spring.dependency-management") version gradleDependencyManagementVersion apply false
    id("io.gitlab.arturbosch.detekt") version detektVersion apply false
    id("com.palantir.git-version") version "0.12.2" apply false
    id("com.gorylenko.gradle-git-properties") version gitPropertiesVersion apply false

    id("jacoco")
    id("com.wavesplatform.vst.contract-docker") version vstContractDockerPluginVersion apply false
}

allprojects {
    group = "net.wavesenterprise.app"
    version = "1.0.0-SNAPSHOT"

    repositories {
        mavenLocal()
        mavenCentral()
        maven { url = uri("https://repo.spring.io/libs-release") }
        maven {
            name = "maven-snapshots"
            url = uri("https://artifacts.wavesenterprise.com/repository/maven-snapshots/")
            mavenContent {
                snapshotsOnly()
            }
            credentials {
                username = mavenUser
                password = mavenPassword
            }
        }

        maven {
            name = "maven-releases"
            url = uri("https://artifacts.wavesenterprise.com/repository/maven-releases/")
            mavenContent {
                releasesOnly()
            }
            credentials {
                username = mavenUser
                password = mavenPassword
            }
        }
        repositories {
            maven {
                url = uri ("https://jitpack.io") 
            }
        }
    }
}

subprojects {
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "kotlin")
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "jacoco")

    apply(from = "$rootDir/gradle/ktlint.gradle.kts")

    val jacocoCoverageFile = "$buildDir/reports/jacoco/jacoco-coverage.xml"

    tasks.withType<JacocoReport> {
        reports {
            xml.apply {
                isEnabled = true
                destination = File(jacocoCoverageFile)
            }
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            events = setOf(
                TestLogEvent.FAILED,
                TestLogEvent.PASSED,
                TestLogEvent.SKIPPED
            )
            exceptionFormat = TestExceptionFormat.FULL
            showExceptions = true
            showCauses = true
            showStackTraces = true
        }
        finalizedBy("jacocoTestReport")
    }

    val detektConfigFilePath = "$rootDir/gradle/detekt-config.yml"

    tasks.withType<Detekt> {
        exclude("resources/")
        exclude("build/")
        config = files(detektConfigFilePath)
    }

    the<DependencyManagementExtension>().apply {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:$springBootVersion")
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
            mavenBom("org.testcontainers:testcontainers-bom:$testContainers")
            mavenBom("org.springframework.boot:spring-boot-dependencies:$springBootVersion") {
                bomProperty("kotlin.version", "1.3.72")
            }
            mavenBom("com.fasterxml.jackson:jackson-bom:$jacksonKotlinVersion")
        }
    }

    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = JavaVersion.VERSION_1_8.toString()
        }
    }

    jacoco {
        toolVersion = "0.8.6"
        reportsDir = file("$buildDir/jacocoReports")
    }
}