package com.android.apphelper2.utils

import android.graphics.Paint
import android.graphics.Rect
import android.text.TextUtils
import kotlin.math.abs

object CustomViewUtil {

    /**
     * @param paint paint
     * @param content content
     * @return return the baseLine for paint and content
     */
    fun getBaseLine(paint: Paint?, content: String): Float {
        if ((paint == null) || (TextUtils.isEmpty(content))) {
            return 0f
        }
        val rect = Rect()
        paint.getTextBounds(content, 0, content.length, rect)
        return abs(rect.top).toFloat()
    }

    /**
     * @return return the collection of with and height  for the paint and content, [0] = width ,[1] = height
     */
    fun getTextSize(paint: Paint?, content: String): FloatArray? {
        if ((paint == null) || TextUtils.isEmpty(content)) {
            return null
        }
        val ints = FloatArray(2)
        val rect = Rect()
        paint.getTextBounds(content, 0, content.length, rect)
        ints[0] = rect.width()
            .toFloat()
        ints[1] = rect.height()
            .toFloat()
        return ints
    }

    /**
     * @return return the measure of the text width
     */
    fun getTextViewWidth(paint: Paint?, content: String): Float {
        return if (paint == null || TextUtils.isEmpty(content)) {
            0F
        } else paint.measureText(content, 0, content.length)
    }

    /**
     * @return return the width of text
     */
    fun getTextHeight(paint: Paint?, content: String): Float {
        if (paint != null && !TextUtils.isEmpty(content)) {
            val rect = Rect()
            paint.getTextBounds(content, 0, content.length, rect)
            return rect.height()
                .toFloat()
        }
        return 0F
    }

    /**
     * @return return the height of the text
     */
    fun getTextWidth(paint: Paint?, content: String): Float {
        if (paint != null && !TextUtils.isEmpty(content)) {
            val rect = Rect()
            paint.getTextBounds(content, 0, content.length, rect)
            return rect.width()
                .toFloat()
        }
        return 0F
    }

    /**
     * @return Returns a centered baseLine, suitable for the most centered display in a region
     */
    fun getBaseLienCenter(paint: Paint?, rect: Rect?, content: String?): Float {
        if (paint != null && !TextUtils.isEmpty(content) && rect != null) {
            val fontMetrics = paint.fontMetrics
            val distance = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom
            return rect.centerY() + distance
        }
        return 0F
    }
}