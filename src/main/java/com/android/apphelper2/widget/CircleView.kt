package com.android.apphelper2.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import com.android.common.utils.ResourcesUtil

class CircleView(context: Context, attributeSet: AttributeSet) : androidx.appcompat.widget.AppCompatImageView(context, attributeSet) {

    private val mPath: Path by lazy {
        return@lazy Path()
    }
    private val mPaint: Paint by lazy {
        return@lazy Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.RED
            strokeWidth = ResourcesUtil.getDimension(context, com.apphelper.demens.R.dimen.dp_2)
        }
    }
    private val mSrc = Rect()
    private val mDes = Rect()

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        // 1：避免重复性绘制
        // super.onDraw(canvas)

        if (width > 0 && drawable != null) {
            val radius = (width / 2).toFloat()
            if (radius > 0) {
                // 2：保存当前的状态
                canvas?.save()
                /**
                 * 既然是绘制圆形，那么宽和高必须是相同的，这里取宽的高度，或者高的高度，都是一样的，这里就直接取值宽的高度了
                 */
                // 3：绘制圆形的path路径
                mPath.addCircle(radius, radius, radius, Path.Direction.CW)
                // 4：将当前剪切与指定的路径相交,可以理解是在裁剪画布
                canvas?.clipPath(mPath)

                // 5: 绘制图片
                if (drawable is BitmapDrawable) {
                    val bitmap = (drawable as BitmapDrawable).bitmap

                    mSrc.left = 0
                    mSrc.top = 0
                    mSrc.right = bitmap.width
                    mSrc.bottom = bitmap.height

                    mDes.left = 0
                    mDes.top = 0
                    mDes.right = bitmap.width
                    mDes.bottom = bitmap.height

                    canvas?.drawBitmap(bitmap, mSrc, mDes, mPaint)

                    // canvas?.drawBitmap(bitmap, 0F, (0F).toFloat(), mPaint)
                }
                // 7：还原
                canvas?.restore()
            }
        }
    }
}
