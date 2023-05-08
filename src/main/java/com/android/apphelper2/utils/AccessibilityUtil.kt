package com.android.apphelper2.utils

import android.Manifest
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityManager
import androidx.core.app.ActivityCompat

class AccessibilityUtil(val context: Context) {

    private val mManager: AccessibilityManager by lazy {
        return@lazy context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    }

    /**
     * 打开 accessibility service 的设置页面
     */
    fun openAccessibilitySetting() {
        runCatching {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }

    /**
     * 获取 accessibility service id
     */
    fun getAccessibilityServiceId(cls: Class<*>): String {
        var serviceId = ""
        runCatching {
            val canonicalName = cls.canonicalName
            canonicalName?.let {
                val packageName = context.packageName
                if (it.contains(packageName)) {
                    serviceId = it.replace(packageName, "$packageName/")
                }
            }
        }
        return serviceId
    }

    /**
     * 是否已经注册了 accessibility service
     */
    fun isInstallAccessibility(serviceId: String): Boolean {
        val installedAccessibilityServiceList = mManager.installedAccessibilityServiceList
        if (installedAccessibilityServiceList != null && installedAccessibilityServiceList.isNotEmpty()) {
            for (item in installedAccessibilityServiceList) {
                if (TextUtils.equals(item.id, serviceId)) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * accessibility service 是否可用
     */
    fun accessibilityEnabled(serviceId: String): Boolean {
        val serviceList = mManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
        if (serviceList != null && serviceList.isNotEmpty()) {
            for (item in serviceList) {
                if (TextUtils.equals(item.id, serviceId)) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * 强制开启 accessibility service
     */
    fun forceEnableAccessibility(serviceId: String): Boolean {
        var result: Boolean = false
        try {
            val hasPermission =
                ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                Settings.Secure.putString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, serviceId)
                Settings.Secure.putString(context.contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED, "1")
                result = true
            }
        } catch (e: IllegalStateException) {
            LogUtil.e("强制开启失败：" + e.message)
        }
        return result
    }
}

