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
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import com.android.apphelper2.app.AppHelperManager.packageName

object SystemUtil {

    private const val TAG = "SystemUtil"

    /**
     * 跳转到应用的设置页面
     */
    fun openApplicationSetting(context: Context) {
        try {
            val intent = Intent()
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
            intent.data = Uri.fromParts("package", packageName, null)
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
     * 打开指定的应用
     */
    @SuppressLint("QueryPermissionsNeeded")
    fun openApplication(packageName: String, context: Context) {
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
                val cn = ComponentName(packageName, className)
                intent.component = cn
                context.startActivity(intent)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            LogUtil.e(TAG, "打开指定的应用失败：${e.message}")
        }
    }

    /**
     * 判断本地是否已经安装好了指定的应用程序包
     *
     * @param packageNameTarget ：待判断的 App 包名，如 微博 com.sina.weibo
     * @return 已安装时返回 true,不存在时返回 false
     */
    @RequiresApi(Build.VERSION_CODES.N)
    private fun appIsExist(context: Context, packageNameTarget: String): Boolean {
        if ("" != packageNameTarget.trim { it <= ' ' }) {
            val packageManager = context.packageManager
            val packageInfoList = packageManager.getInstalledPackages(PackageManager.MATCH_UNINSTALLED_PACKAGES)
            for (packageInfo in packageInfoList) {
                val packageNameSource = packageInfo.packageName
                if (packageNameSource == packageNameTarget) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * 判断本应用是否已经位于最前端
     *
     * @param context
     * @return 本应用已经位于最前端时，返回 true；否则返回 false
     */
    fun isRunningForeground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcessInfoList = activityManager.runningAppProcesses
        /**枚举进程 */
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
     * 将本应用置顶到最前端
     * 当本应用位于后台时，则将它切换到最前端
     */
    @RequiresPermission(android.Manifest.permission.REORDER_TASKS)
    fun setTopApp(context: Context): Boolean {
        if (!isRunningForeground(context)) {
            /**获取ActivityManager */
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

            /**获得当前运行的task(任务) */
            val taskInfoList = activityManager.getRunningTasks(100)
            if (taskInfoList.size > 0) {
                for (taskInfo in taskInfoList) {
                    /**找到本应用的 task，并将它切换到前台 */
                    if (taskInfo.topActivity!!.packageName == context.packageName) {
                        val id = taskInfo.id
                        LogUtil.e("id::::$id")
                        activityManager.moveTaskToFront(taskInfo.id, ActivityManager.MOVE_TASK_WITH_HOME)
                        return true
                    }
                }
            }
        }
        return false
    }

    /**
     * 强制打开和关闭指定的服务
     */
    private fun toggleNotificationListenerService(context: Context, cls: Class<Service>) {
        val pm = context.packageManager
        // 先关闭，在打开
        pm.setComponentEnabledSetting(ComponentName(context, cls), PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP)
        LogUtil.e("强制关闭！")
        pm.setComponentEnabledSetting(ComponentName(context, cls), PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP)
        LogUtil.e("强制打开！")
    }

    /**
     * 把指定的应用设置到前台
     */
    private fun startLocalApp(context: Context, packageNameTarget: String) {
        LogUtil.e("Wmx logs::", "-----------------------开始启动第三方 APP=$packageNameTarget")
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(packageNameTarget)
        if (intent != null) {
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            intent.flags = Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or Intent.FLAG_ACTIVITY_NEW_TASK
            intent.action = "android.intent.action.MAIN"
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } else {
            LogUtil.e("intent  null ")
        }
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