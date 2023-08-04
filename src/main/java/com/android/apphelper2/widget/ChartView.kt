package com.android.apphelper2.widget

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.animation.addListener
import androidx.core.graphics.ColorUtils
import com.android.apphelper2.interfaces.AnimationListener
import com.android.apphelper2.utils.CustomViewUtil
import com.android.apphelper2.utils.CustomViewUtil.getBaseLine
import com.android.apphelper2.utils.CustomViewUtil.getTextWidth
import com.android.apphelper2.utils.ResourcesUtil
import com.android.common.utils.LogUtil
import kotlinx.coroutines.*

class ChartView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    private val mScope: CoroutineScope by lazy {
        return@lazy MainScope()
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
            typeface = Typeface.createFromAsset(context.assets, "DroidSans.ttf")
        }
    }

    private val mLineBottomInterval: Float by lazy {
        return@lazy ResourcesUtil.toPx(55F)
    }
    private val mLineBottomY: Float by lazy {
        return@lazy mMaxHeight - mLineBottomInterval
    }
    private val mLineFirstAlpha: Int by lazy {
        return@lazy (0.2F * 255).toInt()
    }
    private val mLineSecondAlpha: Int by lazy {
        return@lazy (0.1F * 255).toInt()
    }
    private val mLineThreeAlpha: Int by lazy {
        return@lazy (0.15F * 255).toInt()
    }
    private val mLineFourAlpha: Int by lazy {
        return@lazy (0.25F * 255).toInt()
    }
    private val mLinePaint: Paint by lazy {
        return@lazy Paint().apply {
            style = Paint.Style.FILL
            color = Color.WHITE
            strokeWidth = ResourcesUtil.toPx(1F)
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
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
            color = Color.WHITE
            alpha = (0.5F * 255).toInt()
            style = Paint.Style.FILL
            textSize = ResourcesUtil.toPx(14F)
            typeface = Typeface.createFromAsset(context.assets, "DroidSans.ttf")
        }
    }
    private val mScoreInterval: Float by lazy {
        return@lazy ResourcesUtil.toPx(6f)
    }
    private val mScoreLeft: Float by lazy {
        val scoreWidth = getTextWidth(mScorePaint, mScoreText)
        return@lazy mMaxWidth - mPadding - scoreWidth
    }

    private val mBottomTextArray = arrayOf("情绪", "气息", "呼吸", "调息", "心肺")
    private val mBottomTextPaint: Paint by lazy {
        return@lazy Paint().apply {
            color = Color.WHITE
            alpha = (0.7F * 255).toInt()
            textSize = ResourcesUtil.toPx(22f)
            typeface = Typeface.createFromAsset(context.assets, "DroidSans.ttf")
        }
    }
    private val mBottomTextPadding: Float by lazy {
        return@lazy ResourcesUtil.toPx(84F)
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
        return@lazy ResourcesUtil.toPx(10F)
    }
    private val mBottomLeftArray: FloatArray by lazy {
        return@lazy FloatArray(mBottomTextArray.size)
    }

    private var mBottomRectStartFlag = false
    private var mBottomRectChartArray: FloatArray = FloatArray(mBottomTextArray.size)
    private val mBottomRectPaint: Paint by lazy {
        return@lazy Paint().apply {
            // color = Color.parseColor("#005999")
            color = ColorUtils.blendARGB(Color.parseColor("#0094FF"), Color.parseColor("#000000"), 0.25F)
            style = Paint.Style.FILL
        }
    }
    private var mBottomRectAnimationValue: Float = 0F
    private val mBottomRectDelay: Long = 100L
    private val mBottomRectArray: Array<RectF> by lazy {
        return@lazy Array<RectF>(mBottomTextArray.size) {
            RectF()
        }
    }

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
            color = Color.parseColor("#0094FF")
            style = Paint.Style.FILL
        }
    }
    private val mTopRectSpeed: Float by lazy {
        val mTopRectMaxDuration = 1000L
        // s = v * t
        // s = mLineMaxSpace
        // t = mTopRectMaxDuration
        // v = s / t
        return@lazy mLineMaxSpace / mTopRectMaxDuration
    }

    private val mTopRectDelay: Long = 500L

    private val mTopRectTextPaint: Paint by lazy {
        return@lazy Paint().apply {
            textSize = ResourcesUtil.toPx(20F)
            color = Color.parseColor("#0094FF")
            style = Paint.Style.FILL
            typeface = Typeface.createFromAsset(context.assets, "Inter-SemiBoldItalic.otf")
        }
    }
    private var mTopRectTextFlag = false
    private val mTopRectTextEveryInterval: Float by lazy {
        return@lazy ResourcesUtil.toPx(6f)
    }
    private var mTopRectTextAnimationEndCount = 0
    private val mTopRectArray: Array<RectF> by lazy {
        return@lazy Array<RectF>(mBottomTextArray.size) {
            RectF()
        }
    }

    private var mScoreArray: IntArray = IntArray(mBottomTextArray.size)
    private val mScoreDelay: Long = 100L
    private var mAnimationListener: AnimationListener? = null

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
                        mLinePaint.alpha = mLineFirstAlpha
                    }

                    1 -> {
                        mLinePaint.alpha = mLineSecondAlpha
                    }

                    2 -> {
                        mLinePaint.alpha = mLineThreeAlpha
                    }

                    3 -> {
                        mLinePaint.alpha = mLineFourAlpha
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
            if (mTopRectTextFlag && mTopRectTextAnimationEndCount > 0) {
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
                    duration = 1000L
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
                            // wait for the specified time before the execution start
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
                var t = s / mTopRectSpeed

                if (t <= 0) {
                    t = 1F
                }
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
                    mScope.launch {

                        // 1: first draw top text content
                        mTopRectTextFlag = true
                        if (mTopRectTextAnimationEndCount < mBottomTextArray.size) {
                            // delay wait
                            delay(mScoreDelay)
                            mTopRectTextAnimationEndCount++
                            invalidate()

                            if (mTopRectTextAnimationEndCount == mBottomTextArray.size) {
                                delay(500)
                                mAnimationListener?.onEndAnimation()
                            }
                        }

                        // 2: second draw top rect
                        if (mTopRectIndex + 1 < mBottomTextArray.size) {
                            mTopRectIndex++
                            val bottomPercent = mBottomRectChartArray[mTopRectIndex]
                            val topPercent = mTopRectChartArray[mTopRectIndex]

                            // wait next loop start
                            if (topPercent > bottomPercent) {
                                delay(mTopRectDelay)
                            }
                            startTopProgressAnimation(mTopRectIndex, bottomPercent, topPercent)
                        }
                    }
                })
                start()
            }
    }

    @SuppressLint("Recycle")
    private fun drawBottomProgress(canvas: Canvas, index: Int) {
        val rect = mBottomRectArray[index]
        rect.left = getRectLeft(index)
        rect.top = getBottomRectTop(index)
        rect.right = getRectRight(index)
        rect.bottom = getBottomRectBottom()
        LogUtil.e("index -1: $index  rect:$rect")
        canvas.drawRect(rect, mBottomRectPaint)
    }

    @SuppressLint("Recycle")
    private fun drawTopProgress(canvas: Canvas, index: Int) {
        val rect = mTopRectArray[index]
        rect.left = getRectLeft(index)
        rect.top = getTopRectTop(index)
        rect.right = getRectRight(index)
        rect.bottom = getTopRectBottom(index)

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

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        LogUtil.e("tag", "onDetachedFromWindow")
        kotlin.runCatching {
            mScope.cancel()
        }
    }

    fun setAnimationListener(listener: AnimationListener) {
        this.mAnimationListener = listener
    }
}