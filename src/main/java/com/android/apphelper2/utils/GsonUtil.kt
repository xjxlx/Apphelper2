package com.android.apphelper2.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object GsonUtil {

    val mGson: Gson by lazy {
        return@lazy Gson()
    }

    fun <T> toJson(t: T): String {
        return mGson.toJson(t)
    }

    inline fun <reified T> fromJson(json: String): T {
        return mGson.fromJson(json, T::class.java)
    }

    inline fun <reified T> fromJsonNested(json: String): T {
        val type = object : TypeToken<T>() {}.type
        return mGson.fromJson(json, type)
    }
}