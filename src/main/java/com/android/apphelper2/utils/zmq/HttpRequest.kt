package com.android.apphelper2.utils.zmq

/**
 * @author : 流星
 * @CreateDate: 2023/1/6-14:40
 * @Description:
 */
data class HttpRequest<T>(
    val code: Int = 0,
    val msg: String = "",
    val data: T
    )
