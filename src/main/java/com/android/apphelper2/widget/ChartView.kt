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
import com.android.apphelper2.utils.LogUtil
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
    private val mPadding: Float by lazy {
        return@lazy ResourcesUtil.toPx(24F)
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

    private val mLineBottomY: Float by lazy {
        return@lazy ResourcesUtil.toPx(55F)
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

    private val mPaintBottomProgress: Paint by lazy {
        return@lazy Paint().apply {
            color = Color.parseColor("#400094FF")
            style = Paint.Style.FILL
        }
    }
    private val mProgressWith: Float by lazy {
        return@lazy ResourcesUtil.toPx(12F)
    }
    private var mProgressMaxSpace = 0F
    private var mChartBottomArray: FloatArray = FloatArray(mBottomTextArray.size)
    private var mChartTopArray: FloatArray = FloatArray(mBottomTextArray.size)
    private var mBottomLineX = 0F
    private var mAnimationBottomValue: Float = 0F
    private var mAnimationTopValue: Float = 0F
    private val mProgressInterval: Float by lazy {
        return@lazy ResourcesUtil.toPx(3F)
    }
    private var mAnimationFlag: Boolean = false

    private val mPaintTopProgress: Paint by lazy {
        return@lazy Paint().apply {
            color = Color.parseColor("#006FBF")
            style = Paint.Style.FILL
        }
    }
    private var mTopMaxX = 0F
    private var mTopMaxPercent = 0F
    private var mProgressTopIndex = 0
    private val mPath: Path = Path()

    private val mPaintProgressText: Paint by lazy {
        return@lazy Paint().apply {
            textSize = ResourcesUtil.toPx(20F)
            color = Color.parseColor("#0094FF")
            style = Paint.Style.FILL
        }
    }
    private var mAnimationEnd = false
    private val mProgressTopTextInterval: Float by lazy {
        return@lazy ResourcesUtil.toPx(3f)
    }
    private var mAnimationEndCount = 0

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(mMaxWidth, mMaxHeight)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.let {
            // 1: draw an background
            it.drawRoundRect(mBackgroundRectF, mBackgroundAngle, mBackgroundAngle, mBackgroundPaint)

            // 2: draw top text
            val mTitleBaseLine = getBaseLine(mTitlePaint, mTitle)
            it.drawText(mTitle, mPadding, mTitleBaseLine + mPadding, mTitlePaint)

            // 3：draw bottom line ---> full
            val lineStartX = mPadding
            var lineStartY = mMaxHeight - mLineBottomY
            val lineStopX = mMaxWidth - mPadding
            var lineStopY = mMaxHeight - mLineBottomY
            for (index in 0 until 5) {
                when (index) {
                    0 -> {
                        mLinePaint.pathEffect = null
                        mLinePaint.color = mLineFirstColor
                    }
                    1 -> {
                        mLinePaint.pathEffect = mLinePathEffect
                        lineStartY -= mLinesInterval
                        lineStopY -= mLinesInterval
                        mLinePaint.color = mLineSecondColor
                    }
                    2 -> {
                        lineStartY -= mLinesInterval
                        lineStopY -= mLinesInterval
                        mLinePaint.color = mLineThreeColor
                    }
                    3 -> {
                        lineStartY -= mLinesInterval
                        lineStopY -= mLinesInterval
                        mLinePaint.color = mLineFirstColor
                    }
                }
                it.drawLine(lineStartX, lineStartY, lineStopX, lineStopY, mLinePaint)
            }

//            val lineFirstY = mMaxHeight - mLineFirstY
//
//            val mLineRight = mMaxWidth - mPadding
//            it.drawLine(mPadding, lineFirstY, mLineRight, lineFirstY, mLinePaint)
//
//            mLinePaint.pathEffect = mLinePathEffect
//
//            // 3.1 draw first line
//            val firstLineLocation = lineFirstY - mLinesInterval
//            mLinePaint.color = mLineSecondColor
//            it.drawLine(mPadding, firstLineLocation, mLineRight, firstLineLocation, mLinePaint)
//
//            // 3.2 draw second line
//            val secondLineLocation = firstLineLocation - mLinesInterval
//            mLinePaint.color = mLineThreeColor
//            it.drawLine(mPadding, secondLineLocation, mLineRight, secondLineLocation, mLinePaint)
//
//            // 3.3 draw three line
//            val threeLineLocation = secondLineLocation - mLinesInterval
//            mLinePaint.color = mLineFourColor
//            it.drawLine(mPadding, threeLineLocation, mLineRight, threeLineLocation, mLinePaint)

//            mProgressMaxSpace = (lineFirstY - threeLineLocation)
//
//            // 4: draw score
//            val scoreSize = getTextSize(mPaintScore, mScoreText)
//            val scoreLeft = mMaxWidth - mPadding - scoreSize[0]
//            val scoreBaseLine = getBaseLine(mPaintScore, mScoreText)
//            it.drawText(mScoreText, scoreLeft, threeLineLocation - scoreBaseLine, mPaintScore)
//
//            // 5: draw bottom text
//            mBottomTextInterval
//            var bottomLeft = mPaddingBottomText
//            val bottomTop = lineFirstY + mBottomTextMaxBaseLine + mBottomTextTop
//            mBottomTextArray.forEachIndexed { index, s ->
//                mBottomLeftArray[index] = bottomLeft
//                it.drawText(s, bottomLeft, bottomTop, mPaintBottomText)
//                bottomLeft += (mBottomTextWithSize[index] + mBottomTextInterval)
//            }
//
//            mBottomLineX = lineFirstY
//
//            // 6: draw bottom progress
//            mBottomTextArray.indices.forEach { index ->
//                drawBottomProgress(it, index)
//            }
//
//            // 7: draw top progress
//            if (mAnimationFlag) {
//                drawTopProgress(mProgressTopIndex)
//                it.drawPath(mPath, mPaintTopProgress)
//            }
//
//            if (mAnimationEnd) {
//                mBottomTextArray.indices.forEach { index ->
//                    if (index < mAnimationEndCount) {
//                        drawProgressText(it, index)
//                    }
//                }
//            }
        }
    }

    fun setChartArray(chartBottomArray: FloatArray, chartTopArray: FloatArray) {
        this.mChartBottomArray = chartBottomArray
        this.mChartTopArray = chartTopArray
        this.mAnimationFlag = false
        this.mProgressTopIndex = 0
        this.mAnimationEndCount = 0
        mPath.reset()
        val maxProgress = chartBottomArray.maxOrNull()
        var temp = 0F

        ValueAnimator.ofFloat(0F, 1F)
            .apply {
                duration = 2000L
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
                    mProgressTopIndex = 0
                    val bottomProgress = mChartBottomArray[mProgressTopIndex]
                    val topProgress = mChartTopArray[mProgressTopIndex]
                    startTopProgressAnimation(mProgressTopIndex, bottomProgress, topProgress)
                })
                start()
            }
    }

    private fun startTopProgressAnimation(index: Int, bottomProgress: Float = 0F, topProgress: Float = 0F) {
        mTopMaxX = 0F
        mAnimationTopValue = 0F
        mTopMaxPercent = 0F
        var temp = 0F
        ValueAnimator.ofFloat(bottomProgress, topProgress)
            .apply {
                duration = 1000L
                addUpdateListener {
                    mAnimationTopValue = it.animatedValue as Float
                    if (temp != mAnimationTopValue) {
                        invalidate()
                        temp = mAnimationTopValue
                    }
                    LogUtil.e("progress - index:$index ----: mAnimationTopValue : $mAnimationTopValue")
                }
                addListener(onStart = {
                    mAnimationFlag = true
                }, onEnd = {
                    LogUtil.e("thread: ${Thread.currentThread().name}")
                    mAnimationEnd = true
                    mAnimationEndCount++

                    if ((mProgressTopIndex + 1) < mBottomTextArray.size) {
                        mProgressTopIndex += 1
                        if (mProgressTopIndex < mBottomTextArray.size) {
                            val bottomProgressNext = mChartBottomArray[mProgressTopIndex]
                            val topProgressNext = mChartTopArray[mProgressTopIndex]
                            startTopProgressAnimation(mProgressTopIndex, bottomProgressNext, topProgressNext)
                        }
                    }
                })
                start()
            }
    }

    @SuppressLint("Recycle")
    private fun drawBottomProgress(canvas: Canvas, index: Int) {
        val rectLeft = getRectLeft(index)
        val rectTop = getBottomRectTop(index)
        val rectRight = getRectRight(index)
        val rectBottom = getBottomRectBottom()
        val rect = RectF(rectLeft, rectTop, rectRight, rectBottom)
        LogUtil.e("index -1: $index  rect:$rect")
        canvas.drawRect(rect, mPaintBottomProgress)
    }

    @SuppressLint("Recycle")
    private fun drawTopProgress(index: Int) {
        val rectLeft = getRectLeft(index)
        val rectTop = getTopRectTop(index)
        val rectRight = getRectRight(index)
        val rectBottom = getTopRectBottom(index)
        val rect = RectF(rectLeft, rectTop, rectRight, rectBottom)

        LogUtil.e("index -2: $index  rect:$rect")
        // mCanvas?.drawRect(rect, mPaintTopProgress)
        mPath.addRect(rect, Path.Direction.CW)
    }

    val score = 45
    private fun drawProgressText(canvas: Canvas, index: Int) {
        val rectLeft = getRectLeft(index)
        // val score = Random.nextInt(10, 100)
        val text = "+$score"

        val textWidth = CustomViewUtil.getTextWidth(mPaintProgressText, text)
        val realX = rectLeft - ((textWidth - mProgressWith) / 2)

        val targetPercent = mChartTopArray[index]
        val maxTop = mBottomLineX - (targetPercent * mProgressMaxSpace)

        canvas.drawText(text, realX, maxTop - mProgressTopTextInterval, mPaintProgressText)
    }

    private fun getRectLeft(index: Int): Float {
        // rect left = text left + (text.with - rect.with)/2
        return mBottomLeftArray[index] + (mBottomTextWithSize[index] - mProgressWith) / 2
    }

    private fun getBottomRectTop(index: Int): Float {
        // rect top = full.line.bottom - (input.height.percent*scope.maxHeight)
        // bottom max height
        val targetBottomPercent = mChartBottomArray[index]
        return if (mAnimationBottomValue < targetBottomPercent) {
            mBottomLineX - (mAnimationBottomValue * mProgressMaxSpace)
        } else {
            mBottomLineX - (targetBottomPercent * mProgressMaxSpace)
        }
    }

    private fun getTopRectTop(index: Int): Float {
        if (mTopMaxX == 0F) {
            mTopMaxPercent = mChartTopArray[index]
            mTopMaxX = mBottomLineX - (mTopMaxPercent * mProgressMaxSpace)
        }

        return if (mAnimationTopValue < mTopMaxPercent) {
            val real = mBottomLineX - (mAnimationTopValue * mProgressMaxSpace) - mProgressInterval
            if (real > mTopMaxX) {
                real
            } else {
                mTopMaxX
            }
        } else {
            mTopMaxX
        }
    }

    private fun getRectRight(index: Int): Float {
        return getRectLeft(index) + mProgressWith
    }

    private fun getBottomRectBottom(): Float {
        return mBottomLineX
    }

    private fun getTopRectBottom(index: Int): Float {
        return (mBottomLineX - (mChartBottomArray[index] * mProgressMaxSpace)) - mProgressInterval
    }
}