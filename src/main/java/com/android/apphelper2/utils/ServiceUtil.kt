package com.android.apphelper2.utils

import android.app.ActivityManager
import android.app.job.JobScheduler
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.TextUtils
import org.w3c.dom.Text

class ServiceUtil {

    private var mContext: Context? = null
    private lateinit var mScheduler: JobScheduler
    private val mManager: ActivityManager? by lazy {
        return@lazy mContext?.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    }

    fun startService(context: Context, intent: Intent) {
        mContext = context
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
            LogUtil.e("开启了前台的服务！")
        } else {
            context.startService(intent)
            LogUtil.e("开启了后台的服务！")
        }
    }

    /**
     * 判断服务是否正在运行
     *
     * @param cls     服务类的全路径名称 例如： com.jaychan.demo.service.PushService  是包名+服务的类名（例如：net.loonggg.testbackstage.TestService）
     * @param context 上下文对象
     * @return true ：运行中  false:  没有运行中 ，在8.0 以后，只能返回自己的服务是否在运行
     */
    @SuppressWarnings
    fun isServiceRunning(context: Context, cls: Class<*>): Boolean {
        mContext = context
        val runningServices = mManager?.getRunningServices(100)
        runningServices?.let {
            for (info in it) {
                val className = info.service.className
                if (TextUtils.equals(className, cls.name)) {
                    return true //判断服务是否运行
                }
            }
        }
        return false
    }

    /**
     * 判断服务是否正在运行
     *
     * @param cls     服务类的全路径名称 例如： com.jaychan.demo.service.PushService  是包名+服务的类名（例如：net.loonggg.testbackstage.TestService）
     * @param context 上下文对象
     * @return true ：运行中  false:  没有运行中
     */
    fun isServiceRunning(context: Context, cls: String): Boolean {
        mContext = context
        val services = mManager?.getRunningServices(100)
        services?.let {
            for (info in it) {
                val className = info.service.className
                if (TextUtils.equals(cls, className)) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * @param context 上下文
     * @param cls     jobService的对象
     * @return 如果当前有指定的jobService在运行，就返回true，否则返回false
     */
    @Synchronized
    fun isJobServiceRunning(context: Context, cls: Class<*>): Boolean {
        mContext = context

        if (!this::mScheduler.isInitialized) {
            mScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        }

        if (this::mScheduler.isInitialized) {
            for (jobInfo in mScheduler.allPendingJobs) {
                val service = jobInfo.service
                if (TextUtils.equals(service.className, cls.name)) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * @param context 上下文
     * @param clsName jobService的名字，例如： com.jaychan.demo.service.PushService
     * @return 如果当前有指定的jobService在运行，就返回true，否则返回false
     */
    @Synchronized
    fun isJobServiceRunning(context: Context, clsName: String?): Boolean {
        if (!this::mScheduler.isInitialized) {
            mScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        }

        if (this::mScheduler.isInitialized) {
            for (jobInfo in mScheduler.allPendingJobs) {
                val service = jobInfo.service
                if (TextUtils.equals(service.className, clsName)) {
                    return true
                }
            }
        }
        return false
    }

}