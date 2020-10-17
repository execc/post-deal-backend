import groovy.lang.Closure
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

val junitVersion: String by project
val mockitoVersion: String by project
val oauth2Security: String by project
val jacksonKotlinVersion: String by project
val postgresVersion: String by project
val guavaVersion: String by project

val vstIdentityApiVersion: String by project
val vstOauth2ApiVersion: String by project
val vstSecurityCommonsVersion: String by project
val vstCommonsVersion: String by project
val vstFilesApiVersion: String by project
val weBootStarterVersion: String by project
val swaggerVersion: String by project
val flywayVersion: String by project

plugins {
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
    id("org.springframework.boot")
    id("com.palantir.git-version")
    id("com.palantir.docker")
    id("com.gorylenko.gradle-git-properties")
}

dependencies {
    implementation(project(":deals-webapp:deals-webapp-api"))

    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.cloud:spring-cloud-security")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-ribbon")
    implementation("org.springframework.security.oauth.boot:spring-security-oauth2-autoconfigure:$oauth2Security")
    implementation("io.springfox:springfox-swagger2:$swaggerVersion")
    implementation("io.springfox:springfox-bean-validators:$swaggerVersion")
    implementation("io.springfox:springfox-swagger-ui:$swaggerVersion")
    implementation("org.postgresql:postgresql:$postgresVersion")
    implementation("com.google.guava:guava:$guavaVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonKotlinVersion")
    implementation("org.flywaydb:flyway-core:$flywayVersion")
    implementation("io.micrometer:micrometer-registry-prometheus")

    // VST API LIBS
    implementation("com.wavesplatform.vst:vst-identity-api:$vstIdentityApiVersion")
    implementation("com.wavesplatform.vst:vst-oauth2-api:$vstOauth2ApiVersion")
    implementation("com.wavesplatform.vst:vst-security-commons:$vstSecurityCommonsVersion")
    implementation("com.wavesplatform.vst:vst-files-api:$vstFilesApiVersion")
    implementation("com.wavesplatform.we:vst-node-client-starter:$vstCommonsVersion")
    implementation("com.wavesplatform.we:vst-contract-client:$vstCommonsVersion")
    implementation("com.wavesplatform.we:vst-tx-observer-starter:$vstCommonsVersion")
    implementation("com.wavesplatform.we:we-boot-starter:$weBootStarterVersion")

    implementation("com.github.METADIUM:verifiable-credential-java:0.1.8")

    implementation(project(":deals-contract:deals-contract-app"))

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("org.mockito:mockito-core:$mockitoVersion")
    testImplementation("org.mockito:mockito-inline:$mockitoVersion")
    testImplementation("org.mockito:mockito-junit-jupiter:$mockitoVersion")
    testImplementation("org.testcontainers:postgresql")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.3.1")
}
tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}

val gitVersion = (project.extensions.extraProperties.get("gitVersion") as? Closure<*>)?.call()

configure<com.gorylenko.GitPropertiesPluginExtension> {
    dateFormat = "yyyy-MM-dd'T'HH:mmZ"
    keys = listOf(
        "git.branch",
        "git.commit.id",
        "git.commit.id.abbrev",
        "git.commit.time",
        "git.tags",
        "git.closest.tag.name",
        "git.closest.tag.commit.count",
        "git.total.commit.count"
    )
}

fun getDate(): String {
    val current = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")

    return formatter.format(current)
}

val bootJar: BootJar by tasks

tasks {
    docker {
        name = "registry.wavesenterprise.com/deals/${project.name}"
        tags(if (version.toString().endsWith("-SNAPSHOT")) {
            "$version-${getDate()}-$gitVersion"
        } else {
            "$version-$gitVersion"
        })
        files(bootJar.get().archiveFile)
        noCache(true)

        dependsOn(bootJar.get())
    }
}
