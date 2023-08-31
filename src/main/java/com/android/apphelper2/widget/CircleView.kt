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

/**
 * 绘制一个圆形的view
 *  注意：在使用描边的时候，宽度是经过屏幕计算的，只能传递4dp这种基础的值，不能传递@dimen这类型计算过的值，不然会描边宽度会异常
 */
class CircleView(context: Context, attributeSet: AttributeSet) : androidx.appcompat.widget.AppCompatImageView(context, attributeSet) {

    private var mPaintBlur: Paint? = null
    private var mBlurWidth = 0F
    private var mPaintStroke: Paint? = null
    private var mStrokeWidth = 0F
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

    init {
        val typedArray: TypedArray = context.obtainStyledAttributes(attributeSet, R.styleable.CircleView)

        // 描边
        typedArray.getDimension(R.styleable.CircleView_circle_stroke_width, 0F)
            .also {
                if (it != 0F) {
                    val strokeColor = typedArray.getColor(R.styleable.CircleView_circle_stroke_color, 0)
                    if (strokeColor != 0) {
                        mPaintStroke = Paint().also { paint ->
                            paint.style = Paint.Style.STROKE
                            paint.color = strokeColor
                            paint.strokeWidth = it
                            mStrokeWidth = it
                        }
                    }
                }
            }

        // 模糊
        typedArray.getDimension(R.styleable.CircleView_circle_blur_width, 0F)
            .also {
                if (it != 0F) {
                    val blurColor = typedArray.getColor(R.styleable.CircleView_circle_blur_color, 0)
                    if (blurColor != 0) {
                        mPaintBlur = Paint().also { paint ->
                            paint.style = Paint.Style.FILL
                            paint.color = Color.WHITE
                            paint.strokeWidth = it
                            paint.setShadowLayer(it, 0F, 0F, blurColor);
                            mBlurWidth = it
                        }
                    }
                }
            }
        typedArray.recycle()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        // 1：避免重复性绘制
        // super.onDraw(canvas)

        if (width > 0 && canvas != null) {
            val centerX = (width / 2).toFloat()
            val centerY = (height / 2).toFloat()
            val radius = (width / 2)

            // 1：绘制模糊效果
            mPaintBlur?.let {
                canvas.drawCircle(centerX, centerY, radius - mBlurWidth, it)
            }

            // 2：绘制描边
            mPaintStroke?.let {
                /**
                 * stroke.width : 扩散的时候是从中心往外扩散的，值 = 直径，使用的时候要使用半径才可以保证位置
                 */
                val strokeCenterRadius: Float
                if (mBlurWidth > 0) {
                    strokeCenterRadius = radius - mBlurWidth - mStrokeWidth / 2
                } else {
                    strokeCenterRadius = radius - mStrokeWidth / 2
                }
                canvas.drawCircle(centerX, centerY, strokeCenterRadius, it)
            }

            // bitmap.radius = view.radius - blur.width - stroke.width
            var bitmapRadius: Float = radius.toFloat()
            if (mBlurWidth > 0) {
                bitmapRadius -= mBlurWidth
            }
            if (mStrokeWidth > 0) {
                bitmapRadius -= mStrokeWidth
            }

            // 3：绘制bitmap
            if (drawable != null) {
                // 既然是绘制圆形，那么宽和高必须是相同的，这里取宽的高度，或者高的高度，都是一样的，这里就直接取值宽的高度了
                val targetWidth = (bitmapRadius * 2).toInt()
                val targetHeight = (bitmapRadius * 2).toInt()

                // 3.1：保存当前的状态
                canvas.save()
                // 3.2：绘制圆形的path路径
                mPath.addCircle(centerX, centerY, bitmapRadius, Path.Direction.CW)
                // 3.3：将当前剪切与指定的路径相交,可以理解是在裁剪画布
                canvas.clipPath(mPath)

                if (drawable is BitmapDrawable) {
                    val bitmap = (drawable as BitmapDrawable).bitmap
                    // 3.4: 缩放图片的比例，重新生成一个bitmap，避免图片过大，只能取值到一部分图片
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
                        mBitmapSrc.left = (targetWidth - bitmapWidth) / 2
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

                    mBitmapDes.left = (mBlurWidth + mStrokeWidth).toInt()
                    mBitmapDes.top = (mBlurWidth + mStrokeWidth).toInt()
                    mBitmapDes.right = mBitmapDes.left + targetWidth
                    mBitmapDes.bottom = mBitmapDes.top + targetHeight
                    // 3.5：绘制drawable
                    canvas.drawBitmap(scaleBitmap, mBitmapSrc, mBitmapDes, mPaintBitmap)
                }
                // 3.6：还原
                canvas.restore()
            }
        }
    }
}
