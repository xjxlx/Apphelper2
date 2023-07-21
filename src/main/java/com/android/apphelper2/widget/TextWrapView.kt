package com.android.apphelper2.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.android.apphelper2.utils.CustomViewUtil
import com.android.apphelper2.utils.ResourcesUtil

class TextWrapView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    private val mMaxWidth: Float by lazy {
        return@lazy ResourcesUtil.toPx(500F)
    }
    private val mMaxHeight: Float by lazy {
        return@lazy ResourcesUtil.toPx(322F)
    }
    private val mPadding: Float by lazy {
        return@lazy ResourcesUtil.toPx(24F)
    }

    private val mBackgroundRectF: RectF by lazy {
        return@lazy RectF(0F, 0F, mMaxWidth, mMaxHeight)
    }
    private val mBackgroundRadius: Float by lazy {
        return@lazy ResourcesUtil.toPx(20F)
    }
    private val mBackgroundPaint: Paint by lazy {
        return@lazy Paint().apply {
            color = Color.parseColor("#33000000")
        }
    }

    private var mTitleContent = "加油!"
    private val mTitlePaint: Paint by lazy {
        return@lazy Paint().apply {
            color = Color.parseColor("#57AB64")
            style = Paint.Style.FILL
            textSize = ResourcesUtil.toPx(40F)
            typeface = Typeface.DEFAULT_BOLD
        }
    }
    private val mTitleTop: Float by lazy {
        return@lazy ResourcesUtil.toPx(32F)
    }

    private var mSubheadContent = "下次努力哦！"
    private val mSubheadPaint: Paint by lazy {
        return@lazy Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            textSize = ResourcesUtil.toPx(28F)
        }
    }
    private val mSubheadTop: Float by lazy {
        return@lazy ResourcesUtil.toPx(100F)
    }

    private var mWrapTextContent = "呼吸时长足够，注意保持均匀的呼吸次数和平缓的心率，让情绪更稳定些效果更好哦！"
    private val mWrapTextPaint: Paint by lazy {
        return@lazy Paint().apply {
            color = Color.WHITE
            textSize = ResourcesUtil.toPx(26F)
            style = Paint.Style.FILL
        }
    }
    private val mWrapTextTop: Float by lazy {
        return@lazy ResourcesUtil.toPx(160F)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(mMaxWidth.toInt(), mMaxHeight.toInt())
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.let {
            // 1: draw background
            it.drawRoundRect(mBackgroundRectF, mBackgroundRadius, mBackgroundRadius, mBackgroundPaint)

            // 2: draw title
            val titleBaseLine = CustomViewUtil.getBaseLine(mTitlePaint, mTitleContent)
            val titleTop = mTitleTop + titleBaseLine
            it.drawText(mTitleContent, mPadding, titleTop, mTitlePaint)

            // 3: draw subhead
            val subheadBaseLine = CustomViewUtil.getBaseLine(mSubheadPaint, mSubheadContent)
            val subheadTop = mSubheadTop + subheadBaseLine
            it.drawText(mSubheadContent, mPadding, subheadTop, mSubheadPaint)

            // 4: draw wrap text
            val wrapBaseLine = CustomViewUtil.getBaseLine(mWrapTextPaint, mWrapTextContent)
            val wrapTop = mWrapTextTop + wrapBaseLine
            it.drawText(mWrapTextContent, mPadding, wrapTop, mWrapTextPaint)

        }
    }
}