plugins {
    id("com.android.application")
}

android {
    namespace = "com.leaders"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.paulrosselle.leaders"
        minSdk = 28
        targetSdk = 37
        versionCode = 9
        versionName = "v0.9.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation("androidx.activity:activity-ktx:1.13.0")
    implementation("androidx.appcompat:appcompat:1.8.0")
    implementation("androidx.documentfile:documentfile:1.1.0")
    implementation("com.google.android.material:material:1.14.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.2")
    implementation("org.json:json:20260814")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
}

tasks.register<JavaExec>("runNemesisMovesSearch") {
    description = "Run the NemesisMovesSearch algorithm."
    dependsOn("compileDebugJavaWithJavac")

    val classFile = fileTree(layout.buildDirectory).matching {
        include("**/NemesisMovesSearch.class")
    }

    doFirst {
        val matches = classFile.files
        check(matches.isNotEmpty()) {
            "NemesisMovesSearch.class introuvable : vérifie la compilation."
        }

        val classFilePath = matches.first()
        var root = classFilePath.parentFile

        repeat(
            "com.leaders.puzzlelogic.entities.blocker"
                .split(".").size
        ) {
            root = root.parentFile
        }

        classpath = files(
            root,
            configurations.getByName("debugRuntimeClasspath")
        )

        println("Classe trouvée : $classFilePath")
        println("Classpath classes : $root")
    }

    mainClass.set(
        "com.leaders.puzzlelogic.entities.blocker.NemesisMovesSearch"
    )
}