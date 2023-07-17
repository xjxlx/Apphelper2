package com.android.apphelper2.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.android.apphelper2.utils.CustomViewUtil
import com.android.apphelper2.utils.ResourcesUtil

class ChartView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    private val mMaxWidth: Int by lazy {
        return@lazy ResourcesUtil.toPx(500F)
            .toInt()
    }
    private val mMaxHeight: Int by lazy {
        return@lazy ResourcesUtil.toPx(322F)
            .toInt()
    }
    private val mBackgroundAngle: Float by lazy {
        return@lazy ResourcesUtil.toPx(20F)
    }
    private val mBackgroundRectF: RectF by lazy {
        return@lazy RectF(0F, 0f, mMaxWidth.toFloat(), mMaxHeight.toFloat())
    }
    private val mPaintBackground: Paint by lazy {
        return@lazy Paint().apply {
            color = Color.parseColor("#33000000")
            style = Paint.Style.FILL
        }
    }
    private val mTitle = "各项提升"
    private val mPadding: Float by lazy {
        return@lazy ResourcesUtil.toPx(24F)
    }
    private val mPaintTitle: Paint by lazy {
        return@lazy Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            textSize = ResourcesUtil.toPx(24F)
        }
    }
    private val mBottomLine: Float by lazy {
        return@lazy ResourcesUtil.toPx(50F)
    }

    // todo 默认的1dp
    private val mLinesHeight: Float by lazy {
        return@lazy ResourcesUtil.toPx(1F)
    }
    private val mPathEffect: PathEffect by lazy {
        return@lazy DashPathEffect(floatArrayOf(15F, 15F), -1F)
    }
    private val mPaintLines: Paint by lazy {
        return@lazy Paint().apply {
            style = Paint.Style.FILL
            color = Color.parseColor("#33FFFFFF")
            strokeWidth = mLinesHeight
            strokeCap = Paint.Cap.ROUND
        }
    }
    private val mLineFirstColor: Int by lazy {
        return@lazy Color.parseColor("#1AFFFFFF")
    }
    private val mLineSecondColor: Int by lazy {
        return@lazy Color.parseColor("#26FFFFFF")
    }
    private val mLineThreadColor: Int by lazy {
        return@lazy Color.parseColor("#40FFFFFF")
    }
    private val mLinesInterval: Float by lazy {
        return@lazy ResourcesUtil.toPx(60f)
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
            val mTitleBaseLine = CustomViewUtil.getBaseLine(mPaintTitle, mTitle)
            it.drawText(mTitle, mPadding, mTitleBaseLine + mPadding, mPaintTitle)

            // 3：draw bottom line
            val mBottomLineLocation = mMaxHeight - mBottomLine
            val mLineLeft = mPadding
            val mLineRight = mMaxWidth - mPadding

            it.drawLine(mLineLeft, mBottomLineLocation, mLineRight, mBottomLineLocation, mPaintLines)

            mPaintLines.pathEffect = mPathEffect

            // 3.1 draw first line
            val firstLineLocation = mBottomLineLocation - mLinesInterval
            mPaintLines.color = mLineFirstColor
            it.drawLine(mLineLeft, firstLineLocation, mLineRight, firstLineLocation, mPaintLines)

            // 3.2 draw second line
            val secondLineLocation = firstLineLocation - mLinesInterval
            mPaintLines.color = mLineSecondColor
            it.drawLine(mLineLeft, secondLineLocation, mLineRight, secondLineLocation, mPaintLines)

            // 3.3 draw three line
            val threeLineLocation = secondLineLocation - mLinesInterval
            mPaintLines.color = mLineThreadColor
            it.drawLine(mLineLeft, threeLineLocation, mLineRight, threeLineLocation, mPaintLines)

        }
    }
}