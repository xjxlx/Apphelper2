package com.android.apphelper2.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
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

    private var mTitleContent = ""
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

    private var mSubheadContent = ""
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

    private var mWrapTextContent = ""
    private val mWrapTextPaint: TextPaint by lazy {
        return@lazy TextPaint().apply {
            color = Color.WHITE
            textSize = ResourcesUtil.toPx(26F)
            style = Paint.Style.FILL
        }
    }
    private val mWrapTextTop: Float by lazy {
        return@lazy ResourcesUtil.toPx(160F)
    }

    private val mStaticLayout: StaticLayout by lazy {
        return@lazy StaticLayout.Builder.obtain(mWrapTextContent, 0, mWrapTextContent.length, mWrapTextPaint,
            mMaxWidth.toInt() - (mPadding * 2).toInt())
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setMaxLines(3)
            .setEllipsize(TextUtils.TruncateAt.END)
            .build()
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
            if (!TextUtils.isEmpty(mTitleContent)) {
                val titleBaseLine = CustomViewUtil.getBaseLine(mTitlePaint, mTitleContent)
                val titleTop = mTitleTop + titleBaseLine
                it.drawText(mTitleContent, mPadding, titleTop, mTitlePaint)
            }

            // 3: draw subhead
            if (!TextUtils.isEmpty(mSubheadContent)) {
                val subheadBaseLine = CustomViewUtil.getBaseLine(mSubheadPaint, mSubheadContent)
                val subheadTop = mSubheadTop + subheadBaseLine
                it.drawText(mSubheadContent, mPadding, subheadTop, mSubheadPaint)
            }

            // 4: draw wrap text
            if (!TextUtils.isEmpty(mWrapTextContent)) {
                it.save()
                it.translate(mPadding, mWrapTextTop)
                mStaticLayout.draw(it)
                it.restore()
            }
        }
    }

    fun setExplain(score: Int, title: String, subhead: String, content: String) {
        this.mTitleContent = title
        this.mSubheadContent = subhead
        this.mWrapTextContent = content

        // todo 临时的逻辑
        if (score < 40) {
            mTitlePaint.color = Color.parseColor("#E26666")
        } else if (score in 41..69) {
            mTitlePaint.color = Color.parseColor("#EDD452")
        } else if (score > 69) {
            mTitlePaint.color = Color.parseColor("#57AB64")
        }
        invalidate()
    }
}