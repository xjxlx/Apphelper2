package com.android.apphelper2.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import com.android.common.utils.ResourcesUtil

class CircleView(context: Context, attributeSet: AttributeSet) : androidx.appcompat.widget.AppCompatImageView(context, attributeSet) {

    private val mPath: Path by lazy {
        return@lazy Path()
    }
    private val mPaint: Paint by lazy {
        return@lazy Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.RED
            strokeWidth = ResourcesUtil.getDimension(context, com.apphelper.demens.R.dimen.dp_2)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        // super.onDraw(canvas)

        canvas?.save()
        // 添加圆形路径
        mPath.addCircle((width / 2).toFloat(), (height / 2).toFloat(), width.toFloat() / 2, Path.Direction.CW)
        // 将当前剪切与指定的路径相交,可以理解是在裁剪画布
        canvas?.clipPath(mPath)
        // 绘制图片

        drawable?.let {
//            drawable.draw(canvas!!)

            if (drawable is BitmapDrawable) {
                val bitmap = (drawable as BitmapDrawable).bitmap
//               val rect
                canvas?.drawBitmap(bitmap, 0F, ( 0F  ).toFloat(), mPaint)
            }
        }
        canvas?.restore()

//        canvas?.let {
//            it.save()
//            // 添加圆形路径
//            mPath.addCircle((width / 2).toFloat(), (height / 2).toFloat(), width.toFloat() / 2, Path.Direction.CW)
//
//            // 将当前剪切与指定的路径相交,可以理解是在裁剪画布
//            it.clipPath(mPath)
//
//            // 绘制图片
//            it.drawBitmap(mBitmap!!, 0f, 0f, mPaint)
//            it.restore()
//        }
    }
}
