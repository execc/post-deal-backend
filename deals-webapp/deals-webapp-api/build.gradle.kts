val vstIdentityApiVersion: String by project

plugins {
    kotlin("plugin.spring")
}

dependencies {
    implementation(kotlin("stdlib"))

    api(project(":deals-domain"))
    implementation("com.wavesplatform.vst:vst-identity-api:$vstIdentityApiVersion")
    implementation("org.springframework.boot:spring-boot-starter-web")
}
