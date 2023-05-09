package com.android.apphelper2.utils

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityManager
import androidx.core.app.ActivityCompat

class AccessibilityUtil(val context: Context) {
    companion object {
        const val TAG = "accessibility: "
    }

    private val mManager: AccessibilityManager by lazy {
        return@lazy context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    }
    private val mServiceUtil = ServiceUtil()
    private val mIntent: Intent by lazy {
        return@lazy Intent(context, AccessibilityService::class.java)
    }
    private val mServiceId: String by lazy {
        return@lazy getAccessibilityServiceId(AccessibilityService::class.java)
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
        var result = false
        try {
            val hasPermission =
                ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                Settings.Secure.putString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, serviceId)
                Settings.Secure.putString(context.contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED, "1")
                result = true
            }
        } catch (e: IllegalStateException) {
            LogUtil.e(TAG, "强制开启失败：" + e.message)
        }
        return result
    }

    fun startAccessibility() {
        if (!mServiceUtil.isServiceRunning(context, AccessibilityService::class.java)) {
            mServiceUtil.startService(context, mIntent)
            LogUtil.e(TAG, "开启无障碍服务")
        } else {
            LogUtil.e(TAG, "无障碍服务正在运行中！")
        }

        val installAccessibility = isInstallAccessibility(mServiceId)
        LogUtil.e(TAG, "无障碍服务是否安装了: $installAccessibility")

        val accessibilityEnabled = accessibilityEnabled(mServiceId)
        LogUtil.e(TAG, "无障碍服务是否可用: $accessibilityEnabled")

        if (!accessibilityEnabled) {
            LogUtil.e(TAG, "无障碍功能不可用，尝试强制开启！ ")
            val enableAccessibility = forceEnableAccessibility(mServiceId)
            LogUtil.e(TAG, "强制开启成功：$enableAccessibility")
            if (!enableAccessibility) {
                LogUtil.e(TAG,
                    "强制开启失败，请手动开启，或者使用adb命令开启： adb shell pm grant com.android.accessibility android.permission.WRITE_SECURE_SETTINGS ")
            }
        }
    }

    fun stopAccessibility() {
        context.stopService(mIntent)
        LogUtil.e(TAG, "停止了无障碍服务")
    }
}

