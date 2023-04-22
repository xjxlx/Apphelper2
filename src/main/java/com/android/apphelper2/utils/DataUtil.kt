package com.android.apphelper2.utils

import java.text.SimpleDateFormat
import java.util.*

object DataUtil {

    /**
     * 例子：yyyy_MM_dd_HH_mm_ss，或者用 {@link Pattern}
     */
    fun format(pattern: String): String {
        return SimpleDateFormat(pattern, Locale.CHINA).format(Date())
    }

}