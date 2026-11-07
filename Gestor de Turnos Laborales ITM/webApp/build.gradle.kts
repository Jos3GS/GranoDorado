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

// Garantizar que firebase-config.js y los recursos estáticos se copien a las carpetas de distribución
tasks.matching { it.name.contains("BrowserDistribution") }.configureEach {
    doLast {
        val resDir = file("src/webMain/resources")
        listOf("build/dist/js/productionExecutable", "build/dist/js/developmentExecutable").forEach { path ->
            val destDir = file(path)
            if (destDir.exists() && resDir.exists()) {
                resDir.listFiles()?.forEach { f ->
                    if (f.isFile) {
                        f.copyTo(destDir.resolve(f.name), overwrite = true)
                    }
                }
            }
        }
    }
}
