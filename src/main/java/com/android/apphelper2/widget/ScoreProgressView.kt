package com.android.apphelper2.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.animation.addListener
import com.android.apphelper2.interfaces.AnimationListener
import com.android.apphelper2.utils.CustomViewUtil
import com.android.common.utils.LogUtil
import com.android.common.utils.ResourcesUtil
import kotlinx.coroutines.*
import kotlin.math.min

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
    private val mArcStartAngle: Float = 139.3F
    private val mArcMaxSweepAngle: Float by lazy {
        return@lazy mArcStartAngle + 120F
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
    private var mArcScorePercentValue: Int = 0
    private val mArcScorePercent: Float by lazy {
        //  mArcMaxSweepAngle * x =
        // s =  v * t
        // s = mArcMaxSweepAngle
        // t =  100
        // v = s / t
        // mArcMaxSweepAngle / 100
        return@lazy mArcMaxSweepAngle / 100F
    }
    private val mArcMaxDuration: Long = 3000
    private val mArcDurationSpeed: Long by lazy {
        // s = v * t
        // s = mArcMaxDuration
        // t = mScoreMaxValue
        // v = s / t
        // v = mArcMaxSweepAngle / mScoreMaxValue
        return@lazy (mArcMaxDuration / mTotalScoreMaxValue).toLong()
    }

    private val mTitleContent = "综合评分"
    private val mTitlePaint: Paint by lazy {
        return@lazy Paint().apply {
            color = Color.WHITE
            textSize = ResourcesUtil.toPx(36F)
            style = Paint.Style.FILL
            typeface = Typeface.createFromAsset(context.assets, "DroidSans.ttf")
        }
    }
    private val mTitleSize: FloatArray by lazy {
        return@lazy CustomViewUtil.getTextSize(mTitlePaint, mTitleContent)!!
    }
    private val mTitleLeft: Float by lazy {
        return@lazy (mMaxWidth - mTitleSize[0]) / 2
    }
    private val mTitleTop: Float by lazy {
        return@lazy ResourcesUtil.toPx(60F)
    }
    private val mTitleBaseLine: Float by lazy {
        return@lazy CustomViewUtil.getBaseLine(mTitlePaint, mTitleContent)
    }

    private var mTotalScoreValue: Int = 0
    private val mTotalScoreMaxValue: Int = 100
    private val mTotalSorePaint: Paint by lazy {
        return@lazy Paint().apply {
            color = Color.WHITE
            textSize = ResourcesUtil.toPx(108F)
            style = Paint.Style.FILL
            typeface = Typeface.createFromAsset(context.assets, "Niramit-SemiBold.ttf")
        }
    }
    private val mTotalScoreInterval: Float by lazy {
        return@lazy ResourcesUtil.toPx(119.5F)
    }

    private val mUpValueInterval: Float by lazy {
        return@lazy ResourcesUtil.toPx(229F)
    }
    private val mTotalUpTextContent = "总提升"
    private val mTotalUpTextAlpha = (255 * 0.8F).toInt()
    private val mTotalUpTextPaint: Paint by lazy {
        return@lazy Paint().apply {
            textSize = ResourcesUtil.toPx(24F)
            color = Color.WHITE
            alpha = mTotalUpTextAlpha
            style = Paint.Style.FILL
            typeface = Typeface.createFromAsset(context.assets, "DroidSans.ttf")
        }
    }
    private val mTotalUpTextWith: Float by lazy {
        return@lazy CustomViewUtil.getTextWidth(mTotalUpTextPaint, mTotalUpTextContent)
    }
    private val mTotalUpTextBaseLine: Float by lazy {
        return@lazy CustomViewUtil.getBaseLine(mTotalUpTextPaint, mTotalUpTextContent)
    }

    private val mUpTextToValueInterval: Float by lazy {
        return@lazy ResourcesUtil.toPx(5F)
    }

    private var mUpScoreValue: String = ""
    private val mUpScoreValuePaint: Paint by lazy {
        return@lazy Paint().apply {
            color = Color.WHITE
            alpha = mTotalUpTextAlpha
            textSize = ResourcesUtil.toPx(32F)
            style = Paint.Style.FILL
            typeface = Typeface.createFromAsset(context.assets, "Inter-SemiBoldItalic.otf")
        }
    }
    private val mUpScoreValueWith: Float by lazy {
        return@lazy CustomViewUtil.getTextWidth(mUpScoreValuePaint, mUpScoreValue)
    }
    private var mDrawUpScoreTextFlag = false
    private val mScope: CoroutineScope by lazy {
        return@lazy CoroutineScope(Dispatchers.Main)
    }
    private var mAnimationListener: AnimationListener? = null

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(mMaxWidth.toInt(), mMaxHeight.toInt())
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.let {
            // 1: draw background arc
            it.drawArc(mArcRectF, mArcStartAngle, mArcMaxSweepAngle, false, mArcPaintBackground)

            // 2: draw top text
            it.drawText(mTitleContent, mTitleLeft, mTitleTop + mTitleBaseLine, mTitlePaint)

            if (mArcBeforeFlag) {
                // 3: draw before arc
                it.drawArc(mArcRectF, mArcStartAngle, (mArcScorePercent * mArcScorePercentValue), false, mArcPaintBefore)

                // 4: draw total score
                val content = mArcScorePercentValue.toString()
                val scoreWidth = CustomViewUtil.getTextWidth(mTotalSorePaint, content)
                val scoreLeft = (mMaxWidth - scoreWidth) / 2
                val scoreBaseLine = CustomViewUtil.getBaseLine(mTotalSorePaint, content)
                it.drawText(content, scoreLeft, (mTotalScoreInterval + scoreBaseLine), mTotalSorePaint)

                if (mDrawUpScoreTextFlag) {
                    // 5: draw total up text
                    val totalUpTextLeft = (mMaxWidth - mTotalUpTextWith - mUpScoreValueWith - mUpTextToValueInterval) / 2
                    it.drawText(mTotalUpTextContent, totalUpTextLeft, mUpValueInterval + mTotalUpTextBaseLine, mTotalUpTextPaint)

                    // 6: draw up score value
                    val totalUpScoreBaseLine = CustomViewUtil.getBaseLine(mUpScoreValuePaint, mUpScoreValue + "")
                    val totalUpScoreLeft = totalUpTextLeft + mTotalUpTextWith + mUpTextToValueInterval
                    it.drawText(mUpScoreValue, totalUpScoreLeft, mUpValueInterval + totalUpScoreBaseLine, mUpScoreValuePaint)
                }
            }
        }
    }

    fun setScore(totalScore: Int, upScore: Int) {
        this.mArcBeforeFlag = true
        this.mTotalScoreValue = min(totalScore, mTotalScoreMaxValue)
        this.mUpScoreValue = "+$upScore"

        if (mTotalScoreValue <= 40) {
            mArcPaintBefore.color = Color.parseColor("#E26666")
        } else if (mTotalScoreValue in 41..70) {
            mArcPaintBefore.color = Color.parseColor("#EDD452")
        } else if (mTotalScoreValue in 71..100) {
            mArcPaintBefore.color = Color.parseColor("#57AB64")
        }

        ValueAnimator.ofInt(0, this.mTotalScoreValue)
            .apply {
                duration = (mArcDurationSpeed * mTotalScoreValue)
                addUpdateListener {
                    mArcScorePercentValue = it.animatedValue as Int
                    invalidate()
                }
                addListener(onEnd = {
                    mScope.launch {
                        delay(500)
                        mDrawUpScoreTextFlag = true
                        alphaAnimation()
                        invalidate()
                    }
                })
                start()
            }
    }

    private fun alphaAnimation() {
        ValueAnimator.ofInt(0, mTotalUpTextAlpha)
            .apply {
                duration = 1000
                addUpdateListener {
                    val value = it.animatedValue as Int
                    mTotalUpTextPaint.alpha = value
                    mUpScoreValuePaint.alpha = value
                    invalidate()
                }
                addListener(onEnd = {
                    mScope.launch {
                        LogUtil.e("score", "onEndAnimation")
                        delay(500)
                        mAnimationListener?.onEndAnimation()
                    }
                })
                start()
            }
    }

    fun setAnimationListener(listener: AnimationListener) {
        this.mAnimationListener = listener
    }

    fun reset() {
        this.mTotalScoreValue = 0
        this.mArcBeforeFlag = false
        this.mDrawUpScoreTextFlag = false
        invalidate()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        kotlin.runCatching {
            mScope.cancel()
        }
    }
}