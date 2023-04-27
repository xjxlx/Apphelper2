package com.android.apphelper2.utils.permission

interface PermissionCallBackListener {
    fun onCallBack(permission: String, isGranted: Boolean)
}