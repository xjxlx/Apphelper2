package com.android.apphelper2.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtil {

    const val YYYY_MM_DD_HH_MM_SS = "yyyy_MM_dd_HH_mm_ss"

    /**
     * 例子：yyyy_MM_dd_HH_mm_ss，或者用 {@link Pattern}
     */
    fun format(pattern: String): String {
        return SimpleDateFormat(pattern, Locale.CHINA).format(Date())
    }

}