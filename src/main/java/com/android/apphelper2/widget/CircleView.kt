package com.android.apphelper2.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import com.android.apphelper2.R
import com.android.common.utils.BitmapUtil
import com.android.common.utils.LogUtil

class CircleView(context: Context, attributeSet: AttributeSet) : androidx.appcompat.widget.AppCompatImageView(context, attributeSet) {

    private val mPath: Path by lazy {
        return@lazy Path()
    }
    private val mPaintBitmap: Paint by lazy {
        return@lazy Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
    }
    private val mBitmapSrc = Rect()
    private val mBitmapDes = Rect()
    private val mPaintStroke: Paint by lazy {
        return@lazy Paint().also {
            it.style = Paint.Style.STROKE
        }
    }

    init {
        val typedArray: TypedArray = context.obtainStyledAttributes(attributeSet, R.styleable.CircleView)
        typedArray.getColor(R.styleable.CircleView_stroke_color, 0)
            .also {
                if (it != 0) {
                    mPaintStroke.color = it
                }
            }
        typedArray.getDimension(R.styleable.CircleView_stroke_width, 0F)
            .also {
                if (it != 0F) {
                    mPaintStroke.strokeWidth = it
                }
            }
        typedArray.recycle()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        // 1：避免重复性绘制
        // super.onDraw(canvas)

        val strokeWidth = mPaintStroke.strokeWidth

        if (width > 0 && drawable != null && canvas != null) {
            //  既然是绘制圆形，那么宽和高必须是相同的，这里取宽的高度，或者高的高度，都是一样的，这里就直接取值宽的高度了
            val targetWidth = width - (strokeWidth * 2).toInt()
            val targetHeight = height - (strokeWidth * 2).toInt()

            val radius = (targetWidth / 2).toFloat()
            if (radius > 0) {
                // 2：保存当前的状态
                canvas.save()
                // 3：绘制圆形的path路径
                mPath.addCircle(radius, radius, radius, Path.Direction.CW)
                // 4：将当前剪切与指定的路径相交,可以理解是在裁剪画布
                canvas.clipPath(mPath)

                if (drawable is BitmapDrawable) {
                    val bitmap = (drawable as BitmapDrawable).bitmap
                    // 5: 缩放图片的比例，重新生成一个bitmap，避免图片过大，只能取值到一部分图片
                    val scaleBitmap = BitmapUtil.getScaleBitmap(bitmap, targetWidth, targetHeight)
                    if (scaleBitmap == null) {
                        LogUtil.e("scaleBitmap is null !")
                        return
                    }

                    val bitmapWidth = scaleBitmap.width
                    val bitmapHeight = scaleBitmap.height

                    if (bitmapWidth > targetWidth) {
                        // left = (bitmap.width - view.width) / 2, this.value > 0
                        mBitmapSrc.left = (bitmapWidth - targetWidth) / 2
                    } else if (bitmapWidth == targetWidth) {
                        mBitmapSrc.left = 0
                    } else {
                        // (view.width - bitmap.with) / 2, this.value > 0
                        mBitmapSrc.left = 0
                    }

                    if (bitmapHeight > targetHeight) {
                        // top = (bitmap.top - view.top)/ 2
                        mBitmapSrc.top = (bitmapHeight - targetHeight) / 2
                    } else if (bitmapHeight == targetHeight) {
                        mBitmapSrc.top = 0
                    } else {
                        // (view.height - bitmap.height) / 2
                        mBitmapSrc.top = (targetHeight - bitmapHeight) / 2
                    }

                    mBitmapSrc.right = mBitmapSrc.left + targetWidth
                    mBitmapSrc.bottom = mBitmapSrc.top + targetHeight

//                    mBitmapDes.left = if (strokeWidth > 0) {
//                        strokeWidth
//                    } else {
//                        0
//                    }
//                    mBitmapDes.top = if (strokeWidth > 0) {
//                        strokeWidth
//                    } else {
//                        0
//                    }
                    mBitmapDes.right = mBitmapDes.left + targetWidth
                    mBitmapDes.bottom = mBitmapDes.top + targetHeight

                    // 6：绘制drawable
//                    canvas.drawBitmap(scaleBitmap, mBitmapSrc, mBitmapDes, mPaintBitmap)
                }

                // 7：还原
                canvas.restore()
            }
        }

        // 8: 绘制描边
        canvas?.let {
            if (strokeWidth > 0) {
                val circleX = (width / 2).toFloat()
                val circleY = (width / 2).toFloat()
                val radius = ((width - (strokeWidth * 2)) / 2)

                it.drawCircle(circleX, circleY, radius, mPaintStroke)
            }
        }
    }
}
