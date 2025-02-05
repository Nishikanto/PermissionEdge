package com.calyptasapps.permissionedge

data class PermissionConfig(
    val permission: String,
    val rationaleMessage: String,
    val deniedMessage: String,
    val permanentlyDeniedMessage: String
)