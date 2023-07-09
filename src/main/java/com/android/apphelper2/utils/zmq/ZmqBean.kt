package com.android.apphelper2.utils.zmq

/**
 * @author : 流星
 * @CreateDate: 2023/1/6-09:52
 * @Description:
 */
data class ZmqBean(val heart_rate: Int = 0, val SaO2: Int = 0, val respiratory_rate: Int = 0, val respiratory_state: Int = 0,
    val time: String = "", val time_millis: Long = 0)
