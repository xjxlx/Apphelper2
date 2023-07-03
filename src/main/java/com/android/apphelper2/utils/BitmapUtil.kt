package com.android.apphelper2.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

object BitmapUtil {

    private const val TAG: String = "BitmapUtil"

    fun getBitmap(context: Context, id: Int): Bitmap? {
        runCatching {
            return BitmapFactory.decodeResource(context.resources, id)
        }.onFailure {
            LogUtil.e(TAG, "getBitmap failed: ${it.message}")
        }
        return null
    }
}