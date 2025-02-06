// Package declaration for the MainActivity
package com.calyptasapps.sample

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.calyptasapps.permissionedge.PermissionConfig
import com.calyptasapps.permissionedge.PermissionEdge

/**
 * MainActivity is the main entry point for the application.
 * It handles permission requests for camera, notifications, and audio recording using the
 * PermissionManager. It also displays a UI using Jetpack Compose that allows users to trigger
 * permission requests.
 */
class MainActivity : AppCompatActivity() {
    // Initialize the PermissionManager to handle permission requests
    private lateinit var permissionEdge: PermissionEdge

    // Override the onCreate method to initialize the activity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize PermissionManager
        permissionEdge = PermissionEdge(this)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PermissionDemoScreen(
                        onRequestCameraPermission = { requestCameraAndNotificationPermission() },
                        onRequestNotificationPermission = { requestAudioPermission() },
                    )
                }
            } // End of MaterialTheme
        }
    } // End of onCreate method

    // Method to request camera and notification permissions
    private fun requestCameraAndNotificationPermission() {
        val permissionConfigs = arrayListOf(
            PermissionConfig(
                permission = Manifest.permission.CAMERA,
                rationaleMessage = getString(R.string.camera_permission_rationale),
                deniedMessage = getString(R.string.camera_permission_denied),
                permanentlyDeniedMessage = getString(R.string.camera_permission_permanently_denied)
            ),
            PermissionConfig(
                permission = Manifest.permission.READ_CONTACTS,
                rationaleMessage = getString(R.string.read_contact_permission_rationale),
                deniedMessage = getString(R.string.read_contact_permission_denied),
                permanentlyDeniedMessage = getString(R.string.read_contact_permission_permanently_denied)
            )
        )

        permissionEdge.requestPermissions(permissionConfigs) {
            // Callback when permissions are granted
            showToast("All permissions granted")
        }
    }

    // Method to request audio permission
    private fun requestAudioPermission() {
        // Define the configuration for audio permission
        val audioPermissionConfig = PermissionConfig(
            permission = Manifest.permission.RECORD_AUDIO,
            rationaleMessage = getString(R.string.audio_permission_rationale),
            deniedMessage = getString(R.string.audio_permission_denied),
            permanentlyDeniedMessage = getString(R.string.audio_permission_permanently_denied)
        )

        permissionEdge.requestPermissions(listOf(audioPermissionConfig)) {
            // Callback when audio permission is granted
            showToast("Audio permission granted")
        }
    }

    // Method to show a short toast message
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}