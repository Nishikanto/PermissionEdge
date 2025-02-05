# PermissionEdge

PermissionEdge is a lightweight Android library that simplifies runtime permission handling with a clean and intuitive API. It handles all the complexity of permission requests, rationale dialogs, and permanent denial cases.

## Features

- Simple and fluent API for requesting multiple permissions
- Built-in handling of permission rationale dialogs
- Automatic management of "Don't ask again" cases
- Customizable messages for different permission states
- Proper lifecycle management to prevent memory leaks

## Installation

Add JitPack repository to your root `build.gradle` or `settings.gradle`:

### **`settings.gradle` (recommended)**

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
```

### Add the dependency:

```groovy
dependencies {
    implementation 'com.github.Nishikanto:PermissionEdge:v1.0.0'
}
```

## Usage

### 1. Initialize `PermissionManager` in your Activity's `onCreate()`:

```kotlin
class MainActivity : AppCompatActivity() {
    private lateinit var permissionManager: PermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionManager = PermissionManager(this)
    }
}
```

### 2. Create Permission Configurations:

```kotlin
val cameraPermissionConfig = PermissionConfig(
    permission = Manifest.permission.CAMERA,
    rationaleMessage = "Camera access is needed to take pictures",
    deniedMessage = "Camera permission was denied. Some features may not work.",
    permanentlyDeniedMessage = "Camera permission was permanently denied. Please enable it in app settings."
)
```

### 3. Request Permissions:

```kotlin
permissionManager.requestPermissions(
    permissionsToRequest = listOf(cameraPermissionConfig)
) {
    // This callback is called when all permissions are granted
    startCamera()
}
```

### Requesting Multiple Permissions

```kotlin
val permissionConfigs = listOf(
    PermissionConfig(
        permission = Manifest.permission.CAMERA,
        rationaleMessage = "Camera access is needed to take pictures",
        deniedMessage = "Camera permission was denied",
        permanentlyDeniedMessage = "Please enable camera permission in settings"
    ),
    PermissionConfig(
        permission = Manifest.permission.RECORD_AUDIO,
        rationaleMessage = "Microphone access is needed to record audio",
        deniedMessage = "Microphone permission was denied",
        permanentlyDeniedMessage = "Please enable microphone permission in settings"
    )
)

permissionManager.requestPermissions(permissionConfigs) {
    // All permissions granted
    startFeature()
}
```

## Important Notes

- Initialize `PermissionManager` in `onCreate()` before any permission requests.
- The library automatically handles:
    - Permission rationale dialogs
    - Permanent denial cases with settings redirect
    - Permission request callbacks
    - Lifecycle-aware implementation

## License

```
MIT License

Copyright (c) 2024 YourName

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Support

If you find any bugs or have feature requests, please create an issue in the GitHub repository.

---
