package com.android.apphelper2.utils

import android.app.Activity
import android.content.Context
import android.text.TextUtils
import android.view.KeyEvent
import com.android.common.utils.LogUtil
import com.android.common.utils.ToastUtil
import java.util.*

/**
 * Activity的管理类
 */
object ActivityManager {

    private val activityStack = Stack<Activity>()

    /**
     * 添加Activity到堆栈
     */
    fun addActivity(activity: Activity?) {
        if (activity != null) {
            activityStack.add(activity)
        }
    }

    /**
     * 获取当前Activity（堆栈中最后一个压入的）
     */
    fun currentActivity(): Activity? {
        return if (!activityStack.isEmpty()) {
            activityStack.lastElement()
        } else null
    }

    /**
     * 结束当前Activity（堆栈中最后一个压入的）
     */
    fun finishCurrentActivity() {
        if (!activityStack.isEmpty()) {
            val activity = activityStack.lastElement()
            activity?.let { finishSpecifiedActivity(it) }
        }
    }

    /**
     * 结束指定的Activity
     */
    fun finishSpecifiedActivity(activity: Activity?) {
        var activity = activity
        if (activity != null) {
            if (!activityStack.isEmpty()) {
                activityStack.remove(activity)
                activity.finish()
                activity = null
            }
        }
    }

    /**
     * 结束指定类名的Activity
     */
    fun finishActivity(cls: Class<*>) {
        if (!activityStack.isEmpty()) {
            for (activity in activityStack) {
                if (activity!!.javaClass == cls) {
                    finishSpecifiedActivity(activity)
                }
            }
        }
    }

    /**
     * 结束所有Activity
     */
    private fun finishAllActivity() {
        if (!activityStack.isEmpty()) {
            var i = 0
            val size = activityStack.size
            while (i < size) {
                if (null != activityStack[i]) {
                    activityStack[i]!!.finish()
                }
                i++
            }
            activityStack.clear()
        }
    }

    /**
     * 结束除当前Activity外其他所有Activity
     */
    fun finishAllOtherActivity() {
        if (!activityStack.isEmpty()) {
            var i = 0
            val size = activityStack.size
            while (i < size) {
                if (null != activityStack[i] && i != activityStack.size - 1) {
                    activityStack[i]!!.finish()
                }
                i++
            }
            activityStack.clear()
        }
    }

    /**
     * 退出应用程序
     */
    fun AppExit(context: Context) {
        try {
            finishAllActivity()
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
            activityManager.restartPackage(context.packageName)
            System.exit(0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 退出账号时,第一次点击的时间
    private var firstTime: Long = 0

    fun onKeyDown(keyCode: Int, event: KeyEvent, targetTime: Long = 0, toast: String): Boolean {
        try {
            if (targetTime <= 0 || TextUtils.isEmpty(toast)) {
                LogUtil.e("参数异常，请检查后再执行！")
                return false
            }
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
                val secondTime = System.currentTimeMillis()
                if (secondTime - firstTime > targetTime) {
                    ToastUtil.show(toast)
                    firstTime = secondTime
                    return true
                } else {
                    AppExit(currentActivity()!!)
                }
            }
        } catch (e: java.lang.Exception) {
            LogUtil.e("退出应用异常：" + e.message)
        }
        return false
    }
}