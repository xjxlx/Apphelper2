package com.android.apphelper2.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.fragment.app.FragmentActivity
import com.android.apphelper2.R
import com.android.common.utils.KeyBoardUtil
import com.android.common.utils.ResourcesUtil

/**
 * 搜索的View
 * 1：默认的是有设置的宽高的，如果自定义了宽高，需要重新调用方法 # initView()
 * 2：如果要监听搜索的输入内容，需要调用方法 # setSearchListener(SearchListener)
 */
class SearchView(private val context: Context, private val attributeSet: AttributeSet) : LinearLayout(context, attributeSet) {

    private var mBackground: Drawable? = ResourcesUtil.getDrawable(context, R.drawable.shape_round_4_search_background)

    private var mLeftText: TextView? = null
    private var mLeftShow = true
    private var mLeftContent: String = "搜索"
    private var mLeftSize: Float = 17F
    private var mLeftColor: Int = ResourcesUtil.getColor(context, R.color.search_hint_color)
    private var mLeftStart = ResourcesUtil.dp(17F)

    private var mSearch: EditText? = null
    private var mSearchContent: String = ""
    private var mSearchSize: Float = 17F
    private var mSearchLeft: Float = ResourcesUtil.dp(12F)
    private var mSearchMarginVertical: Float = ResourcesUtil.px(12F)

    private var mRightImage: ImageView? = null
    private var mSearchButton: Int = R.drawable.icon_search_button
    private var mSearchButtonWidth: Float = ResourcesUtil.dp(20F)
    private var mSearchButtonHeight: Float = ResourcesUtil.dp(20F)
    private var mSearchButtonLeft: Float = ResourcesUtil.dp(10F)
    private var mSearchButtonRight: Float = ResourcesUtil.dp(18F)
    private var mSearchListener: SearchListener? = null

    private val mKeyBoardUtil: KeyBoardUtil? by lazy {
        if (context is FragmentActivity) {
            return@lazy KeyBoardUtil(context)
        }
        return@lazy null
    }

    init {
        this.orientation = HORIZONTAL
        if (isInEditMode) {
            initView()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var maxHeight = 0

        for (index in 0 until childCount) {
            val childAt = getChildAt(index)
            if (childAt is EditText) {
                val paddingTop = childAt.paddingTop
                val marginBottom = childAt.paddingBottom
                val measuredHeight = childAt.measuredHeight
                maxHeight = maxHeight.coerceAtLeast((paddingTop + measuredHeight + marginBottom))
                // LogUtil.e("child: $childAt paddingTop: $paddingTop marginBottom: $marginBottom measuredHeight: $measuredHeight")
            }
        }
        setMeasuredDimension(measuredWidth, maxHeight)
    }

    fun setBackground(@DrawableRes resource: Int) {
        val drawable = ResourcesUtil.getDrawable(context, resource)
        this.mBackground = drawable
        background = this.mBackground
    }

    fun setLeftShow(showHint: Boolean = false): SearchView {
        this.mLeftShow = showHint
        if (this.mLeftShow) {
            this.mLeftText?.visibility = View.VISIBLE
        } else {
            this.mLeftText?.visibility = View.GONE
        }
        return this
    }

    fun setLeftContent(content: String = ""): SearchView {
        if (!TextUtils.isEmpty(content)) {
            this.mLeftContent = content
            this.mLeftText?.text = this.mLeftContent
        }
        return this
    }

    /**
     * 只能设置具体的数字，不能设置@DimenRes的值
     */
    fun setLeftSize(size: Float = 0F): SearchView {
        if (size != 0F) {
            this.mLeftSize = size
            this.mLeftText?.textSize = mLeftSize
        }
        return this
    }

    fun setLeftColor(@ColorRes color: Int = 0): SearchView {
        if (color != 0) {
            this.mLeftColor = ResourcesUtil.getColor(context, color)
            this.mLeftText?.setTextColor(this.mLeftColor)
        }
        return this
    }

    /**
     * 只能设置具体的数字，不能设置@DimenRes的值
     */
    fun setLeftStart(left: Float = 0F): SearchView {
        if (left != 0F) {
            this.mLeftStart = ResourcesUtil.dp(left)
            this.mLeftText?.setPadding(mLeftStart.toInt(), 0, 0, 0)
        }
        return this
    }

    /**
     * 只能设置具体的数字，不能设置@DimenRes的值
     */
    fun setSearchMarginVertical(marginVertical: Float = 0F): SearchView {
        if (marginVertical != 0F) {
            this.mSearchMarginVertical = ResourcesUtil.px(marginVertical)
        }
        return this
    }

    fun setSearchText(content: String = ""): SearchView {
        if (!TextUtils.isEmpty(content)) {
            mSearch?.setText(content)
        }
        return this
    }

    /**
     * 只能设置具体的数字，不能设置@DimenRes的值
     */
    fun setSearchTextSize(size: Float = 0F): SearchView {
        if (size != 0F) {
            this.mSearchSize = size
            this.mSearch?.textSize = mSearchSize
        }
        return this
    }

    /**
     * 只能设置具体的数字，不能设置@DimenRes的值
     */
    fun setSearchLeft(left: Float = 0F): SearchView {
        if (left != 0F) {
            this.mSearchLeft = ResourcesUtil.dp(left)
            this.mSearch?.setPadding(mSearchLeft.toInt(), mSearchMarginVertical.toInt(), 0, mSearchMarginVertical.toInt())
        }
        return this
    }

    /**
     * 只能设置具体的数字，不能设置@DimenRes的值
     */
    fun setRightEnd(right: Float = 0F): SearchView {
        if (right != 0F) {
            this.mSearchButtonRight = ResourcesUtil.dp(right)
            this.mRightImage?.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).also {
                it.rightMargin = this.mSearchButtonRight.toInt()
            }
        }
        return this
    }

    /**
     * 只能设置具体的数字，不能设置@DimenRes的值
     */
    fun setRightStart(left: Float = 0F): SearchView {
        if (left != 0F) {
            this.mSearchButtonLeft = ResourcesUtil.dp(left)
            this.mRightImage?.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).also {
                it.leftMargin = this.mSearchButtonLeft.toInt()
            }
        }
        return this
    }

    /**
     * 只能设置具体的数字，不能设置@DimenRes的值
     */
    fun setRightHeight(height: Float = 0F): SearchView {
        if (height != 0F) {
            this.mSearchButtonHeight = ResourcesUtil.dp(height)
            this.mRightImage?.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).also {
                it.height = this.mSearchButtonHeight.toInt()
            }
        }

        return this
    }

    /**
     * 只能设置具体的数字，不能设置@DimenRes的值
     */
    fun setRightWidth(width: Float = 0F): SearchView {
        if (width != 0F) {
            this.mSearchButtonWidth = ResourcesUtil.dp(width)
            this.mRightImage?.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).also {
                it.width = this.mSearchButtonWidth.toInt()
            }
        }
        return this
    }

    fun setRightResource(@DrawableRes resource: Int = 0): SearchView {
        if (resource != 0) {
            this.mSearchButton = resource
            this.mRightImage?.setImageResource(mSearchButton)
        }
        return this
    }

    @SuppressLint("ResourceAsColor")
    fun initView() {
        removeAllViews()
        // add background
        background = mBackground

        // add search hint text
        this.addView(TextView(context).also {
            this.mLeftText = it
            if (mLeftShow) {
                it.text = mLeftContent
                it.textSize = mLeftSize
                it.setTextColor(mLeftColor)
                it.setPadding(mLeftStart.toInt(), 0, 0, 0)
            }
        }, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).also { params ->
            params.gravity = Gravity.CENTER_VERTICAL
        })

        // add editText
        addView(EditText(context).also {
            this.mSearch = it
            it.background = null
            it.isSingleLine = true
            it.maxLines = 1
            // 把 enter 按钮修改为 搜索
            it.imeOptions = EditorInfo.IME_ACTION_SEARCH
            it.textSize = mSearchSize
            it.setPadding(mSearchLeft.toInt(), mSearchMarginVertical.toInt(), 0, mSearchMarginVertical.toInt())
            it.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    s?.let {
                        if (!TextUtils.isEmpty(s)) {
                            mSearchContent = s.toString()
                        }
                    }
                }
            })

            // 监听按键
            it.setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_SEARCH || keyCode == KeyEvent.KEYCODE_ENTER) {
                        mSearchListener?.search(mSearchContent)
                        mKeyBoardUtil?.hideEditText()
                        return@setOnKeyListener true
                    }
                }
                return@setOnKeyListener false
            }

        }, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT).also { params ->
            params.width = 0
            params.gravity = Gravity.CENTER_VERTICAL
            params.height = LayoutParams.WRAP_CONTENT
            params.weight = 1F
        })

        // add search icon
        this.addView(ImageView(context).also {
            this.mRightImage = it
            it.setImageResource(mSearchButton)
            it.adjustViewBounds = true
            it.setOnClickListener {
                mSearchListener?.search(mSearchContent)
            }
        }, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).also { params ->
            params.gravity = Gravity.CENTER_VERTICAL
            params.leftMargin = mSearchButtonLeft.toInt()
            params.rightMargin = mSearchButtonRight.toInt()
            params.width = mSearchButtonWidth.toInt()
            params.height = mSearchButtonHeight.toInt()
        })

        requestLayout()
    }

    fun setSearchListener(listener: SearchListener) {
        this.mSearchListener = listener
    }

    interface SearchListener {
        fun search(search: String)
    }
}