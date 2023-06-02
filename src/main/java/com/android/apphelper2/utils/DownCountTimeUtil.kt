package com.android.apphelper2.utils

import kotlinx.coroutines.*

class DownCountTimeUtil {

    private val TAG = "DownCountTime"
    private val mScope: CoroutineScope by lazy {
        return@lazy CoroutineScope(Dispatchers.IO + CoroutineName(TAG))
    }
    private var mTotal: Long = 0L
    private var mTempTotal: Long = 0L
    private var mInterval: Long = 0L
    private var mCurrent: Long = 0
    private var mJob: Job? = null
    private var mCallBackListener: CallBack? = null

    fun setCountdown(total: Long, interval: Long, listener: CallBack) {
        this.mTotal = total
        this.mTempTotal = total
        this.mInterval = interval
        this.mCallBackListener = listener

        mJob?.let {
            if (it.isActive) {
                LogUtil.e(TAG, "down count time is running ...")
                return
            }
        }
        LogUtil.e(TAG, "down count time execute ...")
        next()
    }

    private fun stop() {
        mJob?.cancel()
    }

    fun clear() {
        LogUtil.e(TAG, "down count clear !")
        mJob?.cancel()
        mScope.cancel()
    }

    fun pause() {
        LogUtil.e(TAG, "down count pause !")
        mJob?.cancel()
    }

    fun resume() {
        LogUtil.e(TAG, "down count resume !")
        next()
    }

    private fun next() {
        val interval = (mTotal / mInterval).toInt()
        if (interval > 0) {
            mJob = mScope.launch {
                repeat(interval) {
                    if (mTempTotal > 0) {
                        mTempTotal -= mInterval
                        mCurrent = mTempTotal
                        mCallBackListener?.onTick(mTempTotal, (mTotal - mCurrent))
                        delay(mInterval)
                    }

                    if (mCurrent == 0L) {
                        mCallBackListener?.onFinish()
                        stop()
                        delay(mInterval)
                    }
                }
            }
        }
    }

    interface CallBack {

        /**
         * @param current The current timer, starting at 0
         * @param countdown current countdown
         */
        fun onTick(current: Long, countdown: Long)
        fun onFinish()
    }
}