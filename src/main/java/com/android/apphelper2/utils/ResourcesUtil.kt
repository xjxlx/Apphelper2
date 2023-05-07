package com.android.apphelper2.utils

import android.content.Context
import androidx.annotation.StringRes

object ResourcesUtil {

    fun getString(context: Context, @StringRes id: Int): String {
        kotlin.runCatching {
            context.resources.getString(id)
        }
            .onSuccess {
                return it
            }
            .onFailure {
                return ""
            }
        return ""
    }
}