package com.android.apphelper2.utils

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import kotlinx.coroutines.*

class TouchUtil {
    private val mScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private val tag = "TouchUtil"

    @SuppressLint("ClickableViewAccessibility")
    fun countdown(view: View, interval: Int, block: () -> Unit) {
        var job: Job? = null

        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    job = mScope.launch(start = CoroutineStart.LAZY) {
                        repeat(interval) {
                            val item = interval - it
                            LogUtil.e(tag, " item: $item")
                            if (item <= 1) {
                                LogUtil.e(tag, " item: --- block ")
                                withContext(Dispatchers.Main) {
                                    block()
                                }
                            }
                            delay(1000)
                        }
                    }
                    job?.start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    LogUtil.e(tag, " item: clear")
                    job?.let {
                        if (it.isActive) {
                            it.cancel()
                        }
                    }
                }
            }
            return@setOnTouchListener true
        }
    }
}