val mavenUser: String by project
val mavenPassword: String by project
val jacksonKotlinVersion: String by project
val hibernateTypesVersion: String by project

plugins {
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
    kotlin("kapt")
    `maven-publish`
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.springframework.data:spring-data-jpa")
    implementation("org.hibernate:hibernate-core")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonKotlinVersion")
    implementation("com.vladmihalcea:hibernate-types-52:$hibernateTypesVersion")

    // Projec specific
    implementation("com.github.METADIUM:verifiable-credential-java:0.1.8")

    kapt("org.hibernate:hibernate-jpamodelgen")
}

publishing {
    repositories {
        maven {
            name = "maven"
            url = uri("https://artifacts.vostokservices.com/repository/${if (project.version.toString().endsWith("-SNAPSHOT")) "maven-snapshots" else "maven-releases"}")
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
