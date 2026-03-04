plugins {
    // Upgrade AGP to 8.5.1+ for 16 KB page size support
    id("com.android.application").version("8.5.1").apply(false)
    id("com.android.library").version("8.5.1").apply(false)
    kotlin("android").version("2.1.20").apply(false)
    kotlin("multiplatform").version("2.1.20").apply(false)
    kotlin("plugin.serialization").version("2.1.20").apply(false)
    id("org.jetbrains.kotlin.plugin.compose").version("2.1.20").apply(false)
    id("com.codingfeline.buildkonfig").version("0.17.1").apply(false)
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
