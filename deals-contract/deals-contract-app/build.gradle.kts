import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import org.springframework.boot.gradle.tasks.bundling.BootJar

val vstCommonsVersion: String by project
val mavenUser: String by project
val mavenPassword: String by project

plugins {
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("com.palantir.git-version")
    id("com.wavesplatform.vst.contract-docker")
    `maven-publish`
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.wavesplatform.we:vst-contract-grpc:$vstCommonsVersion")
    api(project(":deals-contract:deals-contract-api"))
    api(project(":deals-domain"))
    implementation("org.springframework:spring-aspects")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

val gitVersion = (project.extensions.extraProperties.get("gitVersion") as? groovy.lang.Closure<*>)?.call()

fun getDate(): String {
    val current = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")

    return formatter.format(current)
}
val dockerName = "registry.wavesenterprise.com/deals-sc/deals-contract-app"
val bootJar: BootJar by tasks
docker {
    name = dockerName
    tags(if (version.toString().endsWith("-SNAPSHOT")) {
        "$version-${getDate()}-$gitVersion"
    } else {
        "$version-$gitVersion"
    })
    files(bootJar.outputs)
}

bootJar.apply {
    archiveClassifier.set("application")
}

val jar: Jar by tasks

jar.apply {
    enabled = true
}

fun String.runCommand(workingDir: File = file("./")): String {
    val parts = this.split("\\s".toRegex())
    val proc = ProcessBuilder(*parts.toTypedArray())
            .directory(workingDir)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

    proc.waitFor(1, TimeUnit.MINUTES)
    return proc.inputStream.bufferedReader().readText().trim()
}

val replaceImageId = tasks.create("replaceImageId") {
    doLast {
        var imageId = ""
        try {
            imageId = "docker inspect $dockerName --format=\"{{.ID}}\"".runCommand()
            println("Contract $dockerName image ID: $imageId")
        } catch (e: Exception) {
            println("Unable to find image ID for contract: $dockerName")
        }

        if (imageId.isNotBlank()) {
            val hash = imageId.substring(8, imageId.length - 1)
            val path = "${project.rootProject.projectDir.absolutePath}/deals-webapp/deals-webapp-app/src/main/resources/application.yml"
            val content = File(path).readText()
            val regex = "imageHash:.*".toRegex()
            val updatedContent = content.replace(regex, "imageHash: $hash")
            File(path).writeText(updatedContent)
        }
    }
}

tasks["dockerTag"].finalizedBy(replaceImageId)
replaceImageId.dependsOn(tasks["dockerTag"])

publishing {
    repositories {
        maven {
            name = "maven"
            url = uri("https://artifacts.wavesenterprise.com/repository/${if (project.version.toString().endsWith("-SNAPSHOT")) "maven-snapshots" else "maven-releases"}")
            credentials {
                username = mavenUser
                password = mavenPassword
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
