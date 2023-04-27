package com.android.apphelper2.utils.permission

interface PermissionRationaleCallBackListener {
    fun onCallBack(permission: String, rationale: Boolean)
}