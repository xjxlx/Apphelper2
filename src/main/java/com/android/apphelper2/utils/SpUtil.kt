package com.android.apphelper2.utils

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import com.android.apphelper2.app.BaseApplication

object SpUtil {

    private const val TAG = "spUtil"
    private const val SP_FILE_NAME = "userInfo"
    private val mSp: SharedPreferences by lazy {
        return@lazy BaseApplication.application.getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE)
    }

    fun putString(key: String, value: String) {
        runCatching {
            if (!TextUtils.isEmpty(key)) {
                mSp.edit()
                    ?.putString(key, value)
                    ?.apply()
            }
        }.onFailure {
            LogUtil.e(TAG, "put string type error: ${it.message}")
        }
    }

    fun getString(key: String, defaultValue: String = ""): String {
        runCatching {
            return mSp.getString(key, defaultValue) ?: ""
        }.onFailure {
            LogUtil.e(TAG, "get string type error: ${it.message}")
        }
        return defaultValue
    }

    fun putInt(key: String, value: Int) {
        runCatching {
            if (!TextUtils.isEmpty(key)) {
                mSp.edit()
                    ?.putInt(key, value)
                    ?.apply()
            }
        }.onFailure {
            LogUtil.e(TAG, "put int type error: ${it.message}")
        }
    }

    fun getInt(key: String, defaultValue: Int = 0): Int {
        runCatching {
            return mSp.getInt(key, defaultValue)
        }.onFailure {
            LogUtil.e(TAG, "get Int type error: ${it.message}")
        }
        return defaultValue
    }

    fun putLong(key: String, value: Long) {
        runCatching {
            if (!TextUtils.isEmpty(key)) {
                mSp.edit()
                    ?.putLong(key, value)
                    ?.apply()
            }
        }.onFailure {
            LogUtil.e(TAG, "put long type error: ${it.message}")
        }
    }

    fun getLong(key: String, defaultValue: Long = 0): Long {
        runCatching {
            return mSp.getLong(key, defaultValue)
        }.onFailure {
            LogUtil.e(TAG, "get long type error: ${it.message}")
        }
        return defaultValue
    }

    fun putBoolean(key: String, value: Boolean) {
        runCatching {
            if (!TextUtils.isEmpty(key)) {
                mSp.edit()
                    ?.putBoolean(key, value)
                    ?.apply()
            }
        }.onFailure {
            LogUtil.e(TAG, "put Boolean type error: ${it.message}")
        }
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        runCatching {
            return mSp.getBoolean(key, defaultValue)
        }.onFailure {
            LogUtil.e(TAG, "get long type error: ${it.message}")
        }
        return defaultValue
    }
}