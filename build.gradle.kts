// Top-level build file...
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false

    // PEGA ESTO AQUÍ:
    id("com.google.gms.google-services") version "4.4.2" apply false
}