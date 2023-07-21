package com.android.apphelper2.widget

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.animation.addListener
import com.android.apphelper2.utils.CustomViewUtil
import com.android.apphelper2.utils.CustomViewUtil.getBaseLine
import com.android.apphelper2.utils.CustomViewUtil.getTextWidth
import com.android.apphelper2.utils.LogUtil
import com.android.apphelper2.utils.ResourcesUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChartView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    private val mScope: CoroutineScope by lazy {
        return@lazy CoroutineScope(Dispatchers.Main)
    }
    private val mMaxWidth: Int by lazy {
        return@lazy ResourcesUtil.toPx(500F)
            .toInt()
    }
    private val mMaxHeight: Int by lazy {
        return@lazy ResourcesUtil.toPx(322F)
            .toInt()
    }
    private val mPadding: Float by lazy {
        return@lazy ResourcesUtil.toPx(24F)
    }
    private val mRectWith: Float by lazy {
        return@lazy ResourcesUtil.toPx(12F)
    }

    private val mBackgroundAngle: Float by lazy {
        return@lazy ResourcesUtil.toPx(20F)
    }
    private val mBackgroundRectF: RectF by lazy {
        return@lazy RectF(0F, 0f, mMaxWidth.toFloat(), mMaxHeight.toFloat())
    }
    private val mBackgroundPaint: Paint by lazy {
        return@lazy Paint().apply {
            color = Color.parseColor("#33000000")
            style = Paint.Style.FILL
        }
    }

    private val mTitle = "各项提升"
    private val mTitlePaint: Paint by lazy {
        return@lazy Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            textSize = ResourcesUtil.toPx(24F)
        }
    }

    private val mLineBottomInterval: Float by lazy {
        return@lazy ResourcesUtil.toPx(55F)
    }
    private val mLineBottomY: Float by lazy {
        return@lazy mMaxHeight - mLineBottomInterval
    }
    private val mLineFirstColor: Int by lazy {
        return@lazy Color.parseColor("#33FFFFFF")
    }
    private val mLineSecondColor: Int by lazy {
        return@lazy Color.parseColor("#1AFFFFFF")
    }
    private val mLineThreeColor: Int by lazy {
        return@lazy Color.parseColor("#26FFFFFF")
    }
    private val mLineFourColor: Int by lazy {
        return@lazy Color.parseColor("#40FFFFFF")
    }
    private val mLinePaint: Paint by lazy {
        return@lazy Paint().apply {
            style = Paint.Style.FILL
            color = mLineFirstColor
            // todo 默认的1dp
            strokeWidth = ResourcesUtil.toPx(0.8F)
            strokeCap = Paint.Cap.ROUND
        }
    }
    private val mLinePathEffect: PathEffect by lazy {
        val f5 = ResourcesUtil.toPx(6F)
        val f2 = ResourcesUtil.toPx(6F)
        return@lazy DashPathEffect(floatArrayOf(f5, f2), -1F)
    }
    private val mLinesEveryInterval: Float by lazy {
        return@lazy ResourcesUtil.toPx(60f)
    }
    private var mLineMaxSpace: Float = 0F

    private val mScoreText = "100  分"
    private val mScorePaint: Paint by lazy {
        return@lazy Paint().apply {
            color = Color.parseColor("#B3FFFFFF")
            style = Paint.Style.FILL
            textSize = ResourcesUtil.toPx(14F)
        }
    }
    private val mScoreInterval: Float by lazy {
        // todo 假数据
        return@lazy ResourcesUtil.toPx(10f)
    }
    private val mScoreLeft: Float by lazy {
        val scoreWidth = getTextWidth(mScorePaint, mScoreText)
        return@lazy mMaxWidth - mPadding - scoreWidth
    }

    private val mBottomTextArray = arrayOf("情绪", "气息", "呼吸", "调息", "心肺")
    private val mBottomTextPaint: Paint by lazy {
        return@lazy Paint().apply {
            color = Color.parseColor("#B3FFFFFF")
            textSize = ResourcesUtil.toPx(22f)
        }
    }
    private val mBottomTextPadding: Float by lazy {
        return@lazy ResourcesUtil.toPx(84f)
    }
    private var mBottomTextMaxBaseLine: Float = 0F
    private val mBottomTextWithSize: FloatArray by lazy {
        val floatArray = FloatArray(mBottomTextArray.size)
        mBottomTextArray.forEachIndexed { index, s ->
            val textWidth = CustomViewUtil.getTextWidth(mBottomTextPaint, s)
            floatArray[index] = textWidth

            val baseLine = CustomViewUtil.getBaseLine(mBottomTextPaint, s)
            if (mBottomTextMaxBaseLine < baseLine) {
                mBottomTextMaxBaseLine = baseLine
            }
        }
        return@lazy floatArray
    }
    private val mBottomTextEveryInterval: Float by lazy {
        var with = 0F
        mBottomTextWithSize.forEach {
            with += it
        }
        return@lazy (mMaxWidth - (mBottomTextPadding * 2) - with) / (mBottomTextWithSize.size - 1)
    }
    private val mBottomTextTop: Float by lazy {
        return@lazy ResourcesUtil.toPx(8f)
    }
    private val mBottomLeftArray: FloatArray by lazy {
        return@lazy FloatArray(mBottomTextArray.size)
    }

    private var mBottomRectStartFlag = false
    private var mBottomRectChartArray: FloatArray = FloatArray(mBottomTextArray.size)
    private val mBottomRectPaint: Paint by lazy {
        return@lazy Paint().apply {
            color = Color.parseColor("#400094FF")
            style = Paint.Style.FILL
        }
    }
    private var mBottomRectAnimationValue: Float = 0F
    private val mBottomRectDelay: Long = 2000

    private val mRectEveryInterval: Float by lazy {
        return@lazy ResourcesUtil.toPx(3F)
    }

    private var mTopRectStartFlag: Boolean = false
    private var mTopRectIndex = 0
    private var mTopRectStartCount = 0
    private var mTopRectChartArray: FloatArray = FloatArray(mBottomTextArray.size)
    private var mTopRectAnimationValue: Float = 0F
    private val mTopRectPaint: Paint by lazy {
        return@lazy Paint().apply {
            color = Color.parseColor("#006FBF")
            style = Paint.Style.FILL
        }
    }
    private val mTopRectSpeed: Float by lazy {
        // todo 临时数据
        val mTopRectMaxDuration = 2000L
        // s = v * t
        // s = mLineMaxSpace
        // t = mTopRectMaxDuration
        // v = s / t
        return@lazy mLineMaxSpace / mTopRectMaxDuration
    }

    private val mTopRectTextPaint: Paint by lazy {
        return@lazy Paint().apply {
            textSize = ResourcesUtil.toPx(20F)
            color = Color.parseColor("#0094FF")
            style = Paint.Style.FILL
        }
    }
    private var mTopRectTextAnimationEnd = false
    private val mTopRectTextEveryInterval: Float by lazy {
        return@lazy ResourcesUtil.toPx(3f)
    }
    private var mTopRectTextAnimationEndCount = 0

    private var mScoreArray: IntArray = IntArray(mBottomTextArray.size)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(mMaxWidth, mMaxHeight)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.let {
            // 1: draw an background
            it.drawRoundRect(mBackgroundRectF, mBackgroundAngle, mBackgroundAngle, mBackgroundPaint)

            // 2: draw title
            val mTitleBaseLine = getBaseLine(mTitlePaint, mTitle)
            it.drawText(mTitle, mPadding, mPadding + mTitleBaseLine, mTitlePaint)

            // 3：draw line
            var lineY = mLineBottomY
            for (index in 0 until 4) {
                when (index) {
                    0 -> {
                        mLinePaint.color = mLineFirstColor
                    }

                    1 -> {
                        mLinePaint.color = mLineSecondColor
                    }

                    2 -> {
                        mLinePaint.color = mLineThreeColor
                    }

                    3 -> {
                        mLinePaint.color = mLineFourColor
                    }
                }

                if (index > 0) {
                    lineY -= mLinesEveryInterval
                    mLinePaint.pathEffect = mLinePathEffect
                } else {
                    mLinePaint.pathEffect = null
                }

                it.drawLine(mPadding, lineY, (mMaxWidth - mPadding), lineY, mLinePaint)
            }
            mLineMaxSpace = mLineBottomY - lineY

            // 4: draw score
            it.drawText(mScoreText, mScoreLeft, lineY - mScoreInterval, mScorePaint)

            // 5: draw bottom text
            mBottomTextEveryInterval
            var bottomLeft = mBottomTextPadding
            val bottomTop = mLineBottomY + mBottomTextMaxBaseLine + mBottomTextTop
            mBottomTextArray.forEachIndexed { index, s ->
                mBottomLeftArray[index] = bottomLeft
                it.drawText(s, bottomLeft, bottomTop, mBottomTextPaint)
                bottomLeft += (mBottomTextWithSize[index] + mBottomTextEveryInterval)
            }

            // 6: draw bottom rect
            if (mBottomRectStartFlag) {
                mBottomTextArray.indices.forEach { index ->
                    drawBottomProgress(it, index)
                }
            }

            // 7: draw top rect
            if (mTopRectStartFlag && mTopRectStartCount > 0) {
                mBottomTextArray.indices.forEach { index ->
                    if (index < mTopRectStartCount) {
                        drawTopProgress(it, index)
                    }
                }
            }

            // 8: draw top text
            if (mTopRectTextAnimationEnd && mTopRectTextAnimationEndCount > 0) {
                mBottomTextArray.indices.forEach { index ->
                    if (index < mTopRectTextAnimationEndCount) {
                        drawProgressText(it, index)
                    }
                }
            }
        }
    }

    fun setChartArray(chartBottomArray: FloatArray, chartTopArray: FloatArray, scoreArray: IntArray) {
        this.mBottomRectChartArray = chartBottomArray
        this.mTopRectChartArray = chartTopArray
        this.mScoreArray = scoreArray
        this.mBottomRectStartFlag = false
        this.mTopRectStartFlag = false
        this.mTopRectIndex = 0
        this.mTopRectStartCount = 0
        this.mTopRectTextAnimationEndCount = 0

        val maxProgress = chartBottomArray.maxOrNull()
        var temp = 0F

        if (maxProgress != null) {
            ValueAnimator.ofFloat(0F, maxProgress)
                .apply {
                    duration = 2000L
                    addUpdateListener {
                        mBottomRectAnimationValue = it.animatedValue as Float
                        if (temp != mBottomRectAnimationValue) {
                            invalidate()
                            temp = mBottomRectAnimationValue
                        }
                        LogUtil.e("value: $mBottomRectAnimationValue")
                    }
                    addListener(onStart = {
                        mBottomRectStartFlag = true
                        LogUtil.e("animation: onStart")
                    }, onEnd = {

                        mScope.launch {
                            delay(mBottomRectDelay)
                            LogUtil.e("animation: onEnd")
                            mTopRectIndex = 0
                            val bottomPercent = mBottomRectChartArray[mTopRectIndex]
                            val topPercent = mTopRectChartArray[mTopRectIndex]
                            startTopProgressAnimation(mTopRectIndex, bottomPercent, topPercent)
                        }
                    })
                    start()
                }
        }
    }

    private fun startTopProgressAnimation(index: Int, bottomProgress: Float = 0F, topProgress: Float = 0F) {
        this.mTopRectAnimationValue = 0F

        var temp = 0F
        ValueAnimator.ofFloat(bottomProgress, topProgress)
            .apply {
                val s = (topProgress - bottomProgress) * mLineMaxSpace
                val t = s / mTopRectSpeed

                LogUtil.e(
                    "topPer:$topProgress botPer:$bottomProgress cz:${topProgress - bottomProgress}  speed: $mTopRectSpeed  s:${s} t:${t}")

                duration = t.toLong()
                addUpdateListener {
                    mTopRectAnimationValue = it.animatedValue as Float
                    if (temp != mTopRectAnimationValue) {
                        invalidate()
                        temp = mTopRectAnimationValue
                    }
                    LogUtil.e("progress - index:$index ----: mAnimationTopValue : $mTopRectAnimationValue")
                }
                addListener(onStart = {
                    mTopRectStartFlag = true
                    if (mTopRectStartCount < mTopRectChartArray.size) {
                        mTopRectStartCount += 1
                    }
                    LogUtil.e("top - start  onStart  : $mTopRectStartCount")
                }, onEnd = {
                    mTopRectTextAnimationEnd = true
                    if (mTopRectTextAnimationEndCount < mTopRectChartArray.size) {
                        mTopRectTextAnimationEndCount += 1
                    }

                    if (mTopRectIndex < mBottomTextArray.size - 1) {
                        mTopRectIndex += 1
                        val bottomPercent = mBottomRectChartArray[mTopRectIndex]
                        val topPercent = mTopRectChartArray[mTopRectIndex]
                        startTopProgressAnimation(mTopRectIndex, bottomPercent, topPercent)
                    }
                })
                start()
            }
    }

    @SuppressLint("Recycle")
    private fun drawBottomProgress(canvas: Canvas, index: Int) {
        val rectLeft = getRectLeft(index)
        val rectTop = getBottomRectTop(index)
        LogUtil.e("bottom - index:${index} top:$rectTop")
        val rectRight = getRectRight(index)
        val rectBottom = getBottomRectBottom()
        val rect = RectF(rectLeft, rectTop, rectRight, rectBottom)
        LogUtil.e("index -1: $index  rect:$rect")
        canvas.drawRect(rect, mBottomRectPaint)
    }

    @SuppressLint("Recycle")
    private fun drawTopProgress(canvas: Canvas, index: Int) {
        val rectLeft = getRectLeft(index)
        val rectTop = getTopRectTop(index)
        val rectRight = getRectRight(index)
        val rectBottom = getTopRectBottom(index)
        val rect = RectF(rectLeft, rectTop, rectRight, rectBottom)

        LogUtil.e("index -2: $index  rect:$rect")
        canvas.drawRect(rect, mTopRectPaint)
    }

    private fun drawProgressText(canvas: Canvas, index: Int) {
        val rectLeft = getRectLeft(index)
        val text = "+${mScoreArray[index]}"
        val textWidth = CustomViewUtil.getTextWidth(mTopRectTextPaint, text)
        val realX = rectLeft - ((textWidth - mRectWith) / 2)
        val targetPercent = mTopRectChartArray[index]
        val maxTop = mLineBottomY - (targetPercent * mLineMaxSpace)
        canvas.drawText(text, realX, maxTop - mTopRectTextEveryInterval, mTopRectTextPaint)
    }

    private fun getRectLeft(index: Int): Float {
        return mBottomLeftArray[index] + ((mBottomTextWithSize[index] - mRectWith) / 2)
    }

    private fun getRectRight(index: Int): Float {
        return getRectLeft(index) + mRectWith
    }

    private fun getBottomRectTop(index: Int): Float {
        val targetBottomPercent = mBottomRectChartArray[index]
        return if (mBottomRectAnimationValue < targetBottomPercent) {
            mLineBottomY - (mBottomRectAnimationValue * mLineMaxSpace)
        } else {
            mLineBottomY - (targetBottomPercent * mLineMaxSpace)
        }
    }

    private fun getBottomRectBottom(): Float {
        return mLineBottomY
    }

    private fun getTopRectTop(index: Int): Float {
        val topRectMaxPercent = mTopRectChartArray[index]
        val topRectMaxValue = mLineBottomY - (topRectMaxPercent * mLineMaxSpace)

        return if (index != mTopRectIndex) {
            topRectMaxValue
        } else {
            if (mTopRectAnimationValue < topRectMaxPercent) {
                val currentValue = mLineBottomY - (mTopRectAnimationValue * mLineMaxSpace) - mRectEveryInterval
                if (currentValue > topRectMaxValue) {
                    currentValue
                } else {
                    topRectMaxValue
                }
            } else {
                topRectMaxValue
            }
        }
    }

    private fun getTopRectBottom(index: Int): Float {
        val bottomPercent = mBottomRectChartArray[index]
        val topPercent = mTopRectChartArray[index]
        return if (bottomPercent != topPercent) {
            mLineBottomY - (bottomPercent * mLineMaxSpace) - mRectEveryInterval
        } else {
            mLineBottomY - (bottomPercent * mLineMaxSpace)
        }
    }
}