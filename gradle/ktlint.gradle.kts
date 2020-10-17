val ktlint by configurations.creating

repositories {
    jcenter()
}

dependencies.add("ktlint", "com.pinterest:ktlint:0.35.0")

val ktlintCheck by tasks.creating(JavaExec::class) {
    group = "verification"
    description = "Check Kotlin code style."
    classpath = ktlint
    main = "com.pinterest.ktlint.Main"
    args = mutableListOf("**/*.gradle.kts", "**/*.kt")
}

tasks["check"].dependsOn(ktlintCheck)

tasks.register<JavaExec>("ktlintFormat") {
    group = "formatting"
    description = "Fix Kotlin code style deviations."
    classpath = ktlint
    main = "com.pinterest.ktlint.Main"
    args = mutableListOf("-F")
}
