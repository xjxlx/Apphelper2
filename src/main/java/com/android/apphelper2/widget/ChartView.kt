package com.android.apphelper2.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.android.apphelper2.utils.ResourcesUtil

class ChartView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    private var mMaxWidth = 500
    private var mMaxHeight = 322

    private val mBackgroundAngle = 20F
    private val mBackgroundRectF: RectF = RectF(0F, 0f, mMaxWidth.toFloat(), mMaxHeight.toFloat())
    private val mPaintBackground: Paint by lazy {
        return@lazy Paint().apply {
            color = Color.parseColor("#33000000")
            style = Paint.Style.FILL
        }
    }
    private val mTitle = "各项提升"
    private val mTitleLeft = 24F
    private val mPaintTitle: Paint by lazy {
        return@lazy Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            textSize = ResourcesUtil.toPx(24F)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(mMaxWidth, mMaxHeight)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.let {
            // 1: draw an background
            it.drawRoundRect(mBackgroundRectF, mBackgroundAngle, mBackgroundAngle, mPaintBackground)

            // 2: draw top text
            it.drawText(mTitle, mTitleLeft, mTitleLeft, mPaintTitle)

        }
    }
}