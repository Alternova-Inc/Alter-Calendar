plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

android {
    namespace = "com.alternova.component"
    compileSdk = 34

    defaultConfig {
        minSdk = 28

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    android {
        publishing {
            singleVariant("release") {
                withSourcesJar()
            }
        }
    }
}

tasks.register("createAar", Copy::class) {
    from("build/intermediates/local_aar_for_lint/release/")
    into("build/outputs/aar/")
    include("classes.jar", "**/*.aar")
}

tasks.getByName("build").dependsOn("createAar")

dependencies {
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
}

publishing {
    publications {
        create<MavenPublication>("alter-calendar") {
            groupId = "com.alternova.components"
            artifactId = "alter-calendar"
            version = "0.1.6"
            afterEvaluate {
                from(components["release"])
            }
        }
    }
}
