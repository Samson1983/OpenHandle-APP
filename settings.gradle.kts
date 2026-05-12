pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        maven { setUrl("https://jitpack.io") }
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        maven { setUrl("https://jitpack.io") }
        maven { url = uri("D:/IdeaProject/openHID/Android_HID_SDK/repo") }
        mavenCentral()
    }
}

rootProject.name = "HID_BLE"
include(":app")
//include(":ble-hid-sdk")
