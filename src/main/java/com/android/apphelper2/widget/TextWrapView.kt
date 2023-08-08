package com.android.apphelper2.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import com.android.apphelper2.utils.CustomViewUtil
import com.android.common.utils.ResourcesUtil

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
            typeface = Typeface.create(Typeface.createFromAsset(context.assets, "DroidSans.ttf"), Typeface.BOLD)
        }
    }
    private val mTitleTop: Float by lazy {
        return@lazy ResourcesUtil.toPx(40F)
    }

    private var mSubheadContent = ""
    private val mSubheadPaintAlpha = (255 * 0.8).toInt()
    private val mSubheadPaint: Paint by lazy {
        return@lazy Paint().apply {
            color = Color.WHITE
            alpha = mSubheadPaintAlpha
            style = Paint.Style.FILL
            textSize = ResourcesUtil.toPx(28F)
            typeface = Typeface.createFromAsset(context.assets, "DroidSans.ttf")
        }
    }
    private val mSubheadTop: Float by lazy {
        return@lazy ResourcesUtil.toPx(108.45F)
    }

    private var mWrapTextContent = ""
    private var mWrapTextPaintAlpha = (0.5 * 255).toInt()
    private val mWrapTextPaint: TextPaint by lazy {
        return@lazy TextPaint().apply {
            textSize = ResourcesUtil.toPx(24F)
            style = Paint.Style.FILL
            color = Color.WHITE
            alpha = mWrapTextPaintAlpha
            typeface = Typeface.createFromAsset(context.assets, "DroidSans.ttf")
        }
    }
    private val mWrapTextTop: Float by lazy {
        return@lazy ResourcesUtil.toPx(156F)
    }

    private val mStaticLayout: StaticLayout by lazy {
        return@lazy StaticLayout.Builder.obtain(mWrapTextContent, 0, mWrapTextContent.length, mWrapTextPaint,
            mMaxWidth.toInt() - (mPadding * 2).toInt())
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setMaxLines(4)
            .setLineSpacing(8f, 1F)
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

        alphaAnimation()

        if (score <= 40) {
            mTitlePaint.color = Color.parseColor("#E26666")
        } else if (score in 41..70) {
            mTitlePaint.color = Color.parseColor("#EDD452")
        } else if (score in 71..100) {
            mTitlePaint.color = Color.parseColor("#57AB64")
        }

        invalidate()
    }

    private fun alphaAnimation() {
        ValueAnimator.ofInt(0, 255)
            .apply {
                duration = 1000
                addUpdateListener {
                    val value = it.animatedValue as Int
                    mTitlePaint.alpha = value
                    if (value <= mSubheadPaintAlpha) {
                        mSubheadPaint.alpha = value
                    }
                    if (value <= mWrapTextPaintAlpha) {
                        mWrapTextPaint.alpha = value
                    }
                    invalidate()
                }
                start()
            }
    }

    fun reset() {
        mTitleContent = ""
        mSubheadContent = ""
        mWrapTextContent = ""
        invalidate()
    }
}