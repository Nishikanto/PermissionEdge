package com.permissionedge

import android.app.AlertDialog
import android.content.Context

object PermissionDialogHelper {
    fun showDialog(
        context: Context,
        positiveButton: String,
        negativeButton: String,
        message: String,
        positiveAction: () -> Unit,
        negativeAction: (() -> Unit)? = null
    ) {
        AlertDialog.Builder(context)
            .setMessage(message)
            .setPositiveButton(positiveButton) { dialog, _ ->
                dialog.dismiss()
                positiveAction()
            }
            .setNegativeButton(negativeButton) { dialog, _ ->
                dialog.dismiss()
                negativeAction?.invoke()
            }
            .show()
    }

    fun showInfoDialog(context: Context, message: String) {
        AlertDialog.Builder(context)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
} 