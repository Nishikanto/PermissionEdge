package com.calyptasapps.permissionedge

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.permissionedge.PermissionDialogHelper.showDialog
import com.permissionedge.PermissionDialogHelper.showInfoDialog
import java.lang.ref.WeakReference

/**
 * Manages runtime permissions for the application.
 * IMPORTANT: Must be initialized before onResume() of the Activity.
 * Will throw IllegalStateException if initialized after onResume.
 *
 * Usage example:
 * ```
 * class YourActivity : AppCompatActivity() {
 *     private lateinit var permissionManager: PermissionManager
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         permissionManager = PermissionManager(this) // Correct initialization
 *     }
 * }
 * ```
 */
class PermissionManager(
    activity: AppCompatActivity,
) {
    // Launcher for handling multiple permission requests
    private lateinit var requestMultiplePermissionsLauncher: ActivityResultLauncher<Array<String>>

    // Weak reference to avoid memory leaks
    private val activityReference = WeakReference(activity)

    // Callback to be executed when all permissions are granted
    private var permissionsGrantedCallback: () -> Unit = {}

    // List to keep track of requested permissions
    private var requestedPermissions: List<PermissionConfig> = listOf()

    init {
        // Check if the activity is past onCreate
        if (activity.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)) {
            throw IllegalStateException(
                "PermissionManager must be initialized in onCreate(). " +
                        "Current state: ${activity.lifecycle.currentState}. " +
                        "Initialize only in onCreate()."
            )
        }

        if (isActivityValid()) {
            initializePermissionLauncher(activity)
        }
    }

    private fun initializePermissionLauncher(activity: AppCompatActivity) {
        requestMultiplePermissionsLauncher =
            activity.registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { grantResults: Map<String, Boolean> ->
                handlePermissionResult(grantResults)
            }
    }

    private fun handlePermissionResult(grantResults: Map<String, Boolean>) {
        val allGranted = grantResults.all { it.value }
        if (allGranted) {
            permissionsGrantedCallback()
        } else {
            handlePermissionsDenied(grantResults)
        }
    }

    private fun isActivityValid(): Boolean {
        val activity = activityReference.get()
        return activity != null && !activity.isFinishing && !activity.isDestroyed
    }

    /**
     * Requests the specified permissions and handles the result.
     * If permissions are already granted, the callback is executed immediately.
     * Otherwise, shows rationale dialog or launches permission request.
     *
     * @param permissionsToRequest List of permission configurations to request
     * @param permissionsGrantedCallback Callback to execute when all permissions are granted
     */
    fun requestPermissions(
        permissionsToRequest: List<PermissionConfig>,
        permissionsGrantedCallback: () -> Unit,
    ) {
        if (!isActivityValid()) return

        val activity = activityReference.get() ?: return

        this.permissionsGrantedCallback = permissionsGrantedCallback
        this.requestedPermissions = permissionsToRequest

        val permissionsNotGranted = permissionsToRequest.filterNot {
            ContextCompat.checkSelfPermission(
                activity, it.permission
            ) == PackageManager.PERMISSION_GRANTED
        }.map { it.permission }

        if (permissionsNotGranted.isEmpty()) {
            permissionsGrantedCallback()
            return
        }

        if (shouldShowRationale(activity, permissionsNotGranted)) {
            showRationaleDialog(permissionsNotGranted)
        } else {
            requestMultiplePermissionsLauncher.launch(permissionsNotGranted.toTypedArray())
        }
    }

    /**
     * Determines if permission rationale should be shown for any of the requested permissions.
     * Returns true if any permission in the list requires rationale explanation.
     *
     * @param activity The activity context
     * @param requestedPermissions List of permission strings to check
     * @return Boolean indicating if rationale should be shown
     */
    private fun shouldShowRationale(
        activity: AppCompatActivity,
        requestedPermissions: List<String>,
    ): Boolean {
        return requestedPermissions.any {
            ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                it
            )
        }
    }

    /**
     * Shows appropriate rationale dialog based on denied permissions.
     * Uses the rationaleMessage from PermissionConfig if available,
     * otherwise falls back to default message.
     *
     * @param rationaleDeniedPermissions List of permissions that need rationale explanation
     */
    private fun showRationaleDialog(rationaleDeniedPermissions: List<String>) {
        if (!isActivityValid()) return
        val activity = activityReference.get() ?: return

        val message = requestedPermissions.find {
            it.permission == rationaleDeniedPermissions[0]
        }?.rationaleMessage ?: activity.getString(R.string.default_rationale_message)

        showDialog(
            activity,
            activity.getString(R.string.Request),
            activity.getString(R.string.Cancel),
            message,
            {
                requestMultiplePermissionsLauncher.launch(rationaleDeniedPermissions.toTypedArray())
            },
            null
        )
    }

    /**
     * Handles the case when permissions are denied.
     * Differentiates between "denied" and "denied and don't ask again" cases.
     * Shows appropriate dialog based on denial type:
     * - Settings dialog for permanently denied permissions
     * - Denied dialog for temporarily denied permissions
     *
     * @param grantResults Map of permission strings to their grant results
     */
    private fun handlePermissionsDenied(grantResults: Map<String, Boolean>) {
        if (!isActivityValid()) return
        val activity = activityReference.get() ?: return

        val settingsDeniedPermissions = grantResults
            .filter { !it.value }
            .map { it.key }
            .filter { !ActivityCompat.shouldShowRequestPermissionRationale(activity, it) }

        if (settingsDeniedPermissions.isNotEmpty()) {
            showSettingsDialog(settingsDeniedPermissions)
        } else {
            showPermissionsDeniedDialog(grantResults)
        }
    }

    /**
     * Shows dialog when permissions are temporarily denied.
     * Displays the deniedMessage from PermissionConfig if available,
     * otherwise uses default denied message.
     *
     * @param grantResults Map of permission strings to their grant results
     */
    private fun showPermissionsDeniedDialog(grantResults: Map<String, Boolean>) {
        if (!isActivityValid()) return
        val activity = activityReference.get() ?: return

        val firstPermission = grantResults.keys.firstOrNull()
        val message = requestedPermissions.find {
            it.permission == firstPermission
        }?.deniedMessage ?: activity.getString(R.string.default_denied_message)

        showInfoDialog(activity, message)
    }

    /**
     * Shows dialog when permissions are permanently denied.
     * Provides option to open app settings.
     * Uses permanentlyDeniedMessage from PermissionConfig if available,
     * otherwise uses default settings message.
     *
     * @param settingsDeniedPermissions List of permanently denied permissions
     */
    private fun showSettingsDialog(settingsDeniedPermissions: List<String>) {
        if (!isActivityValid()) return
        val activity = activityReference.get() ?: return

        val message = requestedPermissions.find {
            it.permission == settingsDeniedPermissions[0]
        }?.permanentlyDeniedMessage ?: activity.getString(R.string.default_settings_message)

        showDialog(
            activity,
            activity.getString(R.string.Open_settings),
            activity.getString(R.string.Cancel),
            message,
            {
                openAppSettings(activity)
            },
            null
        )
    }

    /**
     * Opens the application settings page
     * Used when permissions need to be granted manually
     */
    private fun openAppSettings(context: Context) {
        val intent = Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}