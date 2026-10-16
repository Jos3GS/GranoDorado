plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    js {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared"))

            implementation(libs.compose.ui)
        }
    }
}

// Alias de conveniencia para compatibilidad con configuraciones de ejecución del IDE
tasks.register("wasmJsBrowserDevelopmentRun") {
    dependsOn("jsBrowserDevelopmentRun")
    group = "kotlin browser"
    description = "Ejecuta jsBrowserDevelopmentRun para compatibilidad con Firebase SDK"
}
