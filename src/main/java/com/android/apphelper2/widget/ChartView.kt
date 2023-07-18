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
import com.android.apphelper2.utils.CustomViewUtil.getTextSize
import com.android.apphelper2.utils.LogUtil
import com.android.apphelper2.utils.ResourcesUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

class ChartView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    private var mCanvas: Canvas? = null
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
        return@lazy ResourcesUtil.toPx(55F)
    }

    // todo 默认的1dp
    private val mLinesHeight: Float by lazy {
        return@lazy ResourcesUtil.toPx(0.8F)
    }
    private val mPathEffect: PathEffect by lazy {
        val f5 = ResourcesUtil.toPx(6F)
        val f2 = ResourcesUtil.toPx(6F)
        return@lazy DashPathEffect(floatArrayOf(f5, f2), -1F)
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

    private val mScoreText = "100 分"
    private val mPaintScore: Paint by lazy {
        return@lazy Paint().apply {
            color = Color.parseColor("#B3FFFFFF")
            style = Paint.Style.FILL
            textSize = ResourcesUtil.toPx(14F)
        }
    }

    private val mBottomTextArray = arrayOf("情绪", "气息", "呼吸", "调息", "心肺")
    private val mPaintBottomText: Paint by lazy {
        return@lazy Paint().apply {
            color = Color.parseColor("#B3FFFFFF")
            textSize = ResourcesUtil.toPx(22f)
        }
    }
    private val mPaddingBottomText: Float by lazy {
        return@lazy ResourcesUtil.toPx(84f)
    }
    private var mBottomTextMaxBaseLine: Float = 0F
    private val mBottomTextWithSize: FloatArray by lazy {
        val floatArray = FloatArray(mBottomTextArray.size)
        mBottomTextArray.forEachIndexed { index, s ->
            val textWidth = CustomViewUtil.getTextWidth(mPaintBottomText, s)
            floatArray[index] = textWidth

            val baseLine = CustomViewUtil.getBaseLine(mPaintBottomText, s)
            if (mBottomTextMaxBaseLine < baseLine) {
                mBottomTextMaxBaseLine = baseLine
            }
        }
        return@lazy floatArray
    }
    private val mBottomTextInterval: Float by lazy {
        var with = 0F
        mBottomTextWithSize.forEach {
            with += it
        }
        return@lazy (mMaxWidth - (mPaddingBottomText * 2) - with) / (mBottomTextWithSize.size - 1)
    }
    private val mBottomTextTop: Float by lazy {
        return@lazy ResourcesUtil.toPx(8f)
    }
    private val mBottomLeftArray: FloatArray by lazy {
        return@lazy FloatArray(mBottomTextArray.size)
    }

    private val mPaintProgress: Paint by lazy {
        return@lazy Paint().apply {
            // color = Color.parseColor("#33006FBF")
            color = Color.parseColor("#006FBF")
            style = Paint.Style.FILL
        }
    }
    private val mProgressWith: Float by lazy {
        return@lazy ResourcesUtil.toPx(12F)
    }
    private var mProgressMaxSpace = 0F
    private var mChartBottomArray: FloatArray = FloatArray(mBottomTextArray.size)
    private var mChartTopArray: FloatArray = FloatArray(mBottomTextArray.size)
    private var mProgressBottom = 0F
    private var mAnimationBottomValue: Float = 0F
    private var mAnimationTopValue: Float = 0F
    private val mProgressInterval: Float by lazy {
        return@lazy ResourcesUtil.toPx(3F)
    }
    private var mAnimationFlag: Boolean = false
    private var mProgressIndex = 0

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(mMaxWidth, mMaxHeight)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.let {
            mCanvas = it
            // 1: draw an background
            it.drawRoundRect(mBackgroundRectF, mBackgroundAngle, mBackgroundAngle, mPaintBackground)

            // 2: draw top text
            val mTitleBaseLine = getBaseLine(mPaintTitle, mTitle)
            it.drawText(mTitle, mPadding, mTitleBaseLine + mPadding, mPaintTitle)

            // 3：draw bottom line ---> full
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

            mProgressMaxSpace = (mBottomLineLocation - threeLineLocation)

            // 4: draw score
            val scoreSize = getTextSize(mPaintScore, mScoreText)
            val scoreLeft = mMaxWidth - mPadding - scoreSize[0]
            val scoreBaseLine = getBaseLine(mPaintScore, mScoreText)
            it.drawText(mScoreText, scoreLeft, threeLineLocation - scoreBaseLine, mPaintScore)

            // 5: draw bottom text
            mBottomTextInterval
            var bottomLeft = mPaddingBottomText
            val bottomTop = mBottomLineLocation + mBottomTextMaxBaseLine + mBottomTextTop
            mBottomTextArray.forEachIndexed { index, s ->
                mBottomLeftArray[index] = bottomLeft
                it.drawText(s, bottomLeft, bottomTop, mPaintBottomText)
                bottomLeft += (mBottomTextWithSize[index] + mBottomTextInterval)
            }

            mProgressBottom = mBottomLineLocation

            // 6: draw progress
            mBottomTextArray.indices.forEach { index ->
                drawProgress(index)
            }
            if (mAnimationFlag) {
                mBottomTextArray.indices.forEach { index ->
                    if (index == mProgressIndex) {
                        drawProgress(mProgressIndex)
                    }
                }
            }
        }
    }

    fun setChartArray(chartBottomArray: FloatArray, chartTopArray: FloatArray) {
        this.mChartBottomArray = chartBottomArray
        this.mChartTopArray = chartTopArray
        this.mAnimationFlag = false
        val maxProgress = chartBottomArray.maxOrNull()
        var temp = 0F

        val animation = ValueAnimator.ofFloat(0F, 1F)
            .apply {
                duration = 3000L
                addUpdateListener {
                    mAnimationBottomValue = it.animatedValue as Float
                    if (temp != mAnimationBottomValue) {
                        invalidate()
                        temp = mAnimationBottomValue
                    }
                    if (maxProgress != null) {
                        if (mAnimationBottomValue > maxProgress) {
                            cancel()
                        }
                    }
                    LogUtil.e("value: $mAnimationBottomValue")
                }
                addListener(onEnd = {
                    LogUtil.e("animation: onEnd")
                    startTopProgressAnimation()
                })
            }
        animation.start()
    }

    private fun startTopProgressAnimation() = runBlocking {
        delay(2000)
        var temp = 0F

        val bottomProgress = mChartBottomArray[0]
        val topProgress = mChartTopArray[0]

        if (topProgress > bottomProgress) {
            mProgressIndex = 0

            ValueAnimator.ofFloat(bottomProgress, topProgress)
                .apply {
                    duration = 3000L
                    addUpdateListener {
                        mAnimationTopValue = it.animatedValue as Float
                        if (temp != mAnimationTopValue) {
                            invalidate()
                            temp = mAnimationTopValue
                        }
                        LogUtil.e("progress - : $mAnimationTopValue")
                    }
                    addListener(onStart = {
                        mAnimationFlag = true
                    })
                    start()
                }
            // delay(2000)
        }

//        mBottomTextArray.indices.forEach { index ->
//            val bottomProgress = mChartBottomArray[index]
//            val topProgress = mChartTopArray[index]
//
//            if (topProgress > bottomProgress) {
//                mProgressIndex = index
//
//                ValueAnimator.ofFloat(bottomProgress, topProgress)
//                    .apply {
//                        duration = 3000L
//                        addUpdateListener {
//                            mAnimationTopValue = it.animatedValue as Float
//                            if (temp != mAnimationTopValue) {
//                                invalidate()
//                                temp = mAnimationTopValue
//                            }
//                            LogUtil.e("progress - : $mAnimationTopValue")
//                        }
//                        start()
//                    }
//                // delay(2000)
//            }
//        }
    }

    @SuppressLint("Recycle")
    private fun drawProgress(index: Int) {
        val rectLeft = getRectLeft(index)
        val rectTop = getRectTop(index)
        val rectRight = getRectRight(index)
        val rectBottom = getRectBottom()
        val rect = RectF(rectLeft, rectTop, rectRight, rectBottom)

        LogUtil.e("index: $index  rect:$rect")
        if (mAnimationFlag) {
            mPaintProgress.color = Color.parseColor("#006FBF")
        } else {
            mPaintProgress.color = Color.parseColor("#0094FF")
        }
        mCanvas?.drawRect(rect, mPaintProgress)
    }

    private fun getRectLeft(index: Int): Float {
        // rect left = text left + (text.with - rect.with)/2
        return mBottomLeftArray[index] + (mBottomTextWithSize[index] - mProgressWith) / 2
    }

    private fun getRectTop(index: Int): Float {
        return if (mAnimationFlag) {
            mProgressBottom - mAnimationTopValue * mProgressMaxSpace
        } else {
            // rect top = full.line.bottom - (input.height.percent*scope.maxHeight)
            val targetPercent = mChartBottomArray[index]
            if (targetPercent > 0) {
                if (mAnimationBottomValue < targetPercent) {
                    mProgressBottom - (mAnimationBottomValue * mProgressMaxSpace)
                } else {
                    mProgressBottom - (targetPercent * mProgressMaxSpace)
                }
            } else {
                mProgressBottom
            }
        }
    }

    private fun getRectRight(index: Int): Float {
        return getRectLeft(index) + mProgressWith
    }

    private fun getRectBottom(): Float {
        return if (mAnimationFlag) {
            mProgressBottom - mChartBottomArray[mProgressIndex] * mProgressMaxSpace
        } else {
            mProgressBottom
        }
    }
}