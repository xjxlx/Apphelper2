package com.android.apphelper2.utils.permission

interface PermissionMultipleCallBackListener {
    fun onCallBack(allGranted: Boolean, map: MutableMap<String, Boolean>)
}