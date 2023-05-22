package com.android.apphelper2.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.text.TextUtils
import androidx.annotation.RequiresPermission
import com.android.apphelper2.app.AppHelperManager.mPackageName

/**
 * 使用这个工具类，大多的方法都会用到一个权限：
 * <uses-permission android:name="android.permission.QUERY_ALL_PACKAGES" />
 */
object SystemUtil {

    private const val TAG = "SystemUtil"

    /**
     * 【可用】
     * @param packageName 指定的包名
     * 已安装时返回 true,不存在时返回 false
     *
     * 如果要使用这个方法，需要权限
     * 1：{@link #Manifest.permission.QUERY_ALL_PACKAGES}
     * 2：或者在清单文件中增加：
     * <queries>
     *      <!-- 如果想要与其他的应用进行AIDL通信的话，需要在这里注册包名的信息 -->
     *      <package android:name="com.android.app.free.debug" />
     *      <package android:name="com.android.poc" />
     * </queries>
     */
    fun appInstallApp(context: Context, packageName: String): Boolean {
        if (!TextUtils.isEmpty(packageName)) {
            val packageManager = context.packageManager
            val packageInfoList = packageManager.getInstalledPackages(PackageManager.MATCH_UNINSTALLED_PACKAGES)
            for (packageInfo in packageInfoList) {
                if (TextUtils.equals(packageInfo.packageName, packageName)) {
                    return true
                }
            }
        }
        return false
    }

    /**
     *【可用】
     * @return 判断本应用是否已经位于最前端，本应用已经位于最前端时，返回 true；否则返回 false
     */
    fun isRunningForeground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcessInfoList = activityManager.runningAppProcesses
        for (appProcessInfo in appProcessInfoList) {
            if (appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                if (appProcessInfo.processName == context.applicationInfo.processName) {
                    return true
                }
            }
        }
        return false
    }

    /**
     *【可用】
     * 把指定的应用设置到前台
     */
    fun appForeground(context: Context, packageNameTarget: String) {
        runCatching {
            val packageManager = context.packageManager
            val intent = packageManager.getLaunchIntentForPackage(packageNameTarget)
            if (intent != null) {
                intent.addCategory(Intent.CATEGORY_LAUNCHER)
                intent.flags = Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or Intent.FLAG_ACTIVITY_NEW_TASK
                intent.action = "android.intent.action.MAIN"
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } else {
                LogUtil.e("appForeground getLaunchIntentForPackage null !")
            }
        }.onFailure {
            LogUtil.e("appForeground ---> error: ${it.message}")
        }
    }

    /**
     *【可用】
     * @return 检测是否在后台运行的白名单当中，也就是电池的优化权限
     */
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        var isIgnoring = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            isIgnoring = powerManager.isIgnoringBatteryOptimizations(context.packageName)
        }
        return isIgnoring
    }

    /**
     *【可用】
     * 申请白名单
     */
    @SuppressLint("BatteryLife")
    fun requestIgnoreBatteryOptimizations(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.data = Uri.parse("package:" + context.packageName)
                context.startActivity(intent)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    /**
     *【可用】
     * 申请白名单
     */
    @SuppressLint("BatteryLife")
    @RequiresPermission(anyOf = [Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS])
    fun requestIgnoreBatteryOptimizations(activity: Activity) {
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.data = Uri.parse("package:" + activity.packageName)
            activity.startActivity(intent)
            activity.startActivityForResult(intent, 100)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    /**
     *【可用】
     * 打开指定的应用
     * 注意：
     *      有些应用无法被直接打开，例如：华为的平板
     *      可以通过两个方式打开指定的应用
     *      1：先打开自己的应用，只要自己的应用活着，别的应用就可以被打开，但是只限于activity的页面，后台和广播无法打开
     *      2：打开设置
     *          【设置】---> 【应用和服务】--->【应用启动管理】---> 【自己的应用】--->
     *           ---> 切换为【自动管理】为【手动管理】
     *           --->【允许自动启动】、【允许后台活动】、【允许管理启动】
     */
    @SuppressLint("QueryPermissionsNeeded")
    fun openApplication(context: Context, packageName: String) {
        val packageManager = context.packageManager
        try {
            val pi: PackageInfo = packageManager.getPackageInfo(packageName, 0)
            val resolveIntent = Intent(Intent.ACTION_MAIN, null)
            resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            resolveIntent.setPackage(pi.packageName)
            val apps = packageManager.queryIntentActivities(resolveIntent, 0)
            val resolveInfo = apps.iterator()
                .next()
            if (resolveInfo != null) {
                val className = resolveInfo.activityInfo.name
                val intent = Intent(Intent.ACTION_MAIN)
                intent.addCategory(Intent.CATEGORY_LAUNCHER)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                val cn = ComponentName(packageName, className)
                intent.component = cn
                context.startActivity(intent)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            LogUtil.e(TAG, "打开指定的应用失败：${e.message}")
        }
    }

    /**
     *【可用】
     * 跳转到应用的设置页面
     */
    fun openApplicationSetting(context: Context) {
        try {
            val intent = Intent()
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
            intent.data = Uri.fromParts("package", mPackageName, null)
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            val intent = Intent(Settings.ACTION_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: java.lang.Exception) {
            LogUtil.e("跳转应用设置页面失败：" + e.message)
        }
    }

    /**
     *【可用】
     * 检测并打开悬浮窗
     */
    fun openOverlayWindowPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(context)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.packageName))
                context.startActivity(intent)
            } else {
                // 已经获取了悬浮窗权限，可以在此进行操作了
                LogUtil.e("已经拥有了悬浮窗的权限！")
            }
        }
    }

    /**
     * 跳转到指定应用的首页
     */
    fun openAppHomePage(context: Context, packageName: String) {
        val intent: Intent? = context.packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    /**
     * 跳转到指定应用的指定页面
     */
    fun openSpecifiedPage(context: Context, packageName: String, activityDir: String) {
        val intent = Intent()
        intent.component = ComponentName(packageName, activityDir)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    /**
     * 强制打开和关闭指定的服务
     */
    private fun toggleNotificationListenerService(context: Context, cls: Class<Service>) {
        val pm = context.packageManager
        // 先关闭，在打开
        pm.setComponentEnabledSetting(ComponentName(context, cls), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
        LogUtil.e("强制关闭！")
        pm.setComponentEnabledSetting(ComponentName(context, cls), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
        LogUtil.e("强制打开！")
    }

    /**
     * 当前应用的notification 是否可用
     */
    fun isNotificationEnabled(context: Context): Boolean {
        val pkgName = context.packageName
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        if (!TextUtils.isEmpty(flat)) {
            val names = flat.split(":")
                .toTypedArray()
            for (i in names.indices) {
                val cn = ComponentName.unflattenFromString(names[i])
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.packageName)) {
                        return true
                    }
                }
            }
        }
        return false
    }
}