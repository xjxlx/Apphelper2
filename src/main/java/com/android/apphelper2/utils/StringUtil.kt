package com.android.apphelper2.utils

import android.text.TextUtils

object StringUtil {

    fun isEmpty(s: String, block: (String) -> Unit) {
        if (!TextUtils.isEmpty(s)) {
            block(s)
        }
    }

}