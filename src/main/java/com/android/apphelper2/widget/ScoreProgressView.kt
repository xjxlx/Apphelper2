package com.android.apphelper2.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.android.apphelper2.utils.CustomViewUtil
import com.android.apphelper2.utils.LogUtil
import com.android.apphelper2.utils.ResourcesUtil

class ScoreProgressView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    private val mMaxWidth: Float by lazy {
        return@lazy ResourcesUtil.toPx(322F)
    }
    private val mMaxHeight: Float by lazy {
        return@lazy ResourcesUtil.toPx(322F)
    }

    private val mArcStrokeWidth: Float by lazy {
        return@lazy ResourcesUtil.toPx(20F)
    }
    private val mArcRadius: Float = mArcStrokeWidth / 2
    private val mArcRectF: RectF by lazy {
        return@lazy RectF(mArcRadius, mArcRadius, mMaxWidth - mArcRadius, mMaxHeight - mArcRadius)
    }
    private val mArcStartAngle: Float = 159F
    private val mArcMaxSweepAngle: Float by lazy {
        return@lazy mArcStartAngle + 63F
    }
    private val mArcPaintBackground: Paint by lazy {
        return@lazy Paint().apply {
            color = Color.parseColor("#52555A")
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeWidth = mArcStrokeWidth
        }
    }
    private val mArcPaintBefore: Paint by lazy {
        return@lazy Paint().apply {
            color = Color.parseColor("#57AB64")
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeWidth = mArcStrokeWidth
        }
    }
    private var mArcBeforeFlag: Boolean = false
    private var mArcScoreValue: Float = 0F
    private val mArcMaxDuration: Long = 3000
    private val mArcDuration: Long by lazy {
        // s = v * t
        // s = mArcMaxSweepAngle
        // t = 3000
        // v = s / t
        // v = mArcMaxSweepAngle / mArcMaxDuration
        return@lazy (mArcMaxSweepAngle / mArcMaxDuration).toLong()
    }

    private val mTitleContent = "综合评分"
    private val mTitlePaint: Paint by lazy {
        return@lazy Paint().apply {
            color = Color.WHITE
            textSize = ResourcesUtil.toPx(36F)
            style = Paint.Style.FILL
        }
    }
    private val mTitleSize: FloatArray by lazy {
        return@lazy CustomViewUtil.getTextSize(mTitlePaint, mTitleContent)
    }
    private val mTitleLeft: Float by lazy {
        return@lazy (mMaxWidth - mTitleSize[0]) / 2
    }
    private val mTitleTop: Float by lazy {
        return@lazy ResourcesUtil.toPx(50F)
    }
    private val mTitleBaseLine: Float by lazy {
        return@lazy CustomViewUtil.getBaseLine(mTitlePaint, mTitleContent)
    }

    private var mScoreValue: Float = 0F

    val paint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 1f
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(mMaxWidth.toInt(), mMaxHeight.toInt())
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        // oval :指定圆弧的外轮廓矩形区域。
        // startAngle: 圆弧起始角度，单位为度。
        // sweepAngle: 圆弧扫过的角度，顺时针方向，单位为度,从右中间开始为零度。
        // useCenter:为True时，在绘制圆弧时将圆心包括在内，通常用来绘制扇形。
        // paint: 绘制圆弧的画板属性
        canvas?.let {
            val y = 226.28F
            it.drawLine(0f, y, mMaxWidth, y, paint)

            // 1: draw background arc
            it.drawArc(mArcRectF, mArcStartAngle, mArcMaxSweepAngle, false, mArcPaintBackground)

            // 2: draw top text
            it.drawText(mTitleContent, mTitleLeft, mTitleTop + mTitleBaseLine, mTitlePaint)

            if (mArcBeforeFlag) {
                // 3: draw before arc
                it.drawArc(mArcRectF, mArcStartAngle, mArcScoreValue, false, mArcPaintBefore)
            }

            LogUtil.e("mMaxSweepAngle: $mArcMaxSweepAngle")
        }
    }

    fun setScore(score: Float) {
        this.mArcBeforeFlag = true
        if (score > mArcMaxSweepAngle) {
            this.mArcScoreValue = mArcMaxSweepAngle
        } else {
            this.mArcScoreValue = score
        }

        ValueAnimator.ofFloat(0F, this.mArcScoreValue)
            .apply {
                duration = mArcMaxDuration
                addUpdateListener {
                    mArcScoreValue = it.animatedValue as Float
                    invalidate()
                }
                start()
            }
    }
}