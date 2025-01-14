plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.example.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation ("""com.google.mlkit:text-recognition:16.0.0""")
    implementation ("""androidx.activity:activity-compose:1.8.0""")
    implementation ("""androidx.compose.ui:ui:1.4.3""")
    implementation ("""androidx.compose.ui:ui-graphics:1.4.3""")
    implementation ("""androidx.compose.ui:ui-tooling-preview:1.4.3""")
    implementation ("""androidx.compose.material3:material3:1.1.0""")
    implementation ("""androidx.compose.material:material:1.4.3""")
    implementation ("""androidx.compose.material:material-icons-core:1.4.3""")
    implementation ("""androidx.compose.material:material-icons-extended:1.4.3""")
    implementation ("""androidx.core:core-ktx:1.10.1""")
    implementation ("""androidx.lifecycle:lifecycle-runtime-ktx:2.6.2""")
    implementation ("""androidx.activity:activity-compose:1.8.0""")
    implementation ("""androidx.compose.ui:ui-tooling-preview:1.4.3""")
    testImplementation ("""junit:junit:4.13.2""")
    androidTestImplementation ("""androidx.test.ext:junit:1.1.5""")
    androidTestImplementation ("""androidx.test.espresso:espresso-core:3.5.1""")
    androidTestImplementation ("""androidx.compose.ui:ui-test-junit4:1.4.3""")
    debugImplementation ("""androidx.compose.ui:ui-tooling:1.4.3""")
    debugImplementation ("""androidx.compose.ui:ui-test-manifest:1.4.3""")
    implementation ("""com.google.android.gms:play-services-mlkit-text-recognition:19.0.0""")
    implementation("com.google.android.gms:play-services-location:21.0.1") // Replace with the latest version
    implementation ("com.google.android.gms:play-services-maps:17.0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation ("com.google.android.gms:play-services-location:18.0.0")

}