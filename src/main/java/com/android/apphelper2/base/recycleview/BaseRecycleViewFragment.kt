package com.android.apphelper2.base.recycleview

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.apphelper2.app.AppHelper2

abstract class BaseRecycleViewFragment<T, E : BaseVH> : RecyclerView.Adapter<E>() {

    protected var mList: MutableList<T> = mutableListOf()
    protected var mContext: Context? = null

    // 默认是有数据的布局
    private var mViewType: ViewTypeEnum = ViewType.TYPE_DATA
    private var mRecycleView: RecyclerView? = null

    private var mPlaceHolderEmptyView: View? = null
        get() {
            AppHelper2.getBuilder()
                ?.let {
                    return LayoutInflater.from(mContext)
                        .inflate(it.placeHolderRecycleTempView, null, false)
                }
            return null
        }

    private var mPlaceHolderErrorView: View? = null
        get() {
            AppHelper2.getBuilder()
                ?.let {
                    return LayoutInflater.from(mContext)
                        .inflate(it.PlaceHolderRecycleErrorView, null, false)
                }
            return null
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): E {
        mContext = parent.context

        if (mViewType == ViewType.TYPE_DATA) {
            val resource = getLayout(viewType)
            val view = LayoutInflater.from(parent.context)
                .inflate(resource, parent, false)
            return createVH(viewType, view)
        } else if (mViewType == ViewType.TYPE_EMPTY) {

        }
    }

    override fun getItemCount(): Int {
        return if (mViewType == ViewType.TYPE_DATA) {
            mList.size
        } else if (mViewType == ViewType.TYPE_EMPTY || mViewType == ViewType.TYPE_ERROR) {
            1
        } else {
            1
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (mViewType == ViewType.TYPE_DATA) {
            return ViewType.TYPE_DATA.type
        } else if (mViewType == ViewType.TYPE_ERROR || mViewType == ViewType.TYPE_EMPTY) {
            return ViewType.TYPE_EMPTY.type
        }
        return super.getItemViewType(position)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setList(list: MutableList<T>, isRefresh: Boolean = true) {
        // 数据为空，设置空布局
        if (list.isEmpty()) {
            // 空布局
            mViewType = ViewType.TYPE_EMPTY
            this.mList = list
            notifyDataSetChanged()
        } else {
            // 数据不为空
            mViewType = ViewType.TYPE_DATA
            if (isRefresh) {
                // 加载全部数据
                this.mList = list
                notifyDataSetChanged()
            } else {
                insertedList(list)
            }
        }
    }

    fun <R : Throwable> setList(error: R) {
        mViewType = ViewType.TYPE_ERROR
        notifyDataSetChanged()
    }

    /**
     * add all data ,default position in list bottom
     */
    open fun insertedList(list: MutableList<T>) {
        if (list.size > 0) {
            mViewType = ViewType.TYPE_DATA
            mList.addAll(list)
            notifyItemInserted(mList.size)
        }
    }

    /**
     * add single item, default position in list bottom
     */
    fun insertItem(item: T) {
        mViewType = ViewType.TYPE_DATA
        mList.add(item)
        notifyItemInserted(mList.size)
    }

    /**
     * add single item, position in the top
     */
    open fun insertedItemToTop(item: T) {
        mViewType = ViewType.TYPE_DATA
        mList.add(0, item)
        notifyItemInserted(0)
        if (0 != mList.size) {
            notifyItemRangeChanged(0, mList.size)
        }
    }

    open fun scrollTop() {
        mRecycleView?.smoothScrollToPosition(0)
    }

    fun setPlaceHolderErrorView(resource: Int) {
        val inflate = LayoutInflater.from(mContext)
            ?.inflate(resource, null, false)
        if (inflate != null) {
            setPlaceHolderErrorView(inflate)
        }
    }

    fun setPlaceHolderErrorView(errorView: View) {
        this.mPlaceHolderErrorView = errorView
    }

    fun setPlaceHolderEmptyView(resource: Int) {
        val inflate = LayoutInflater.from(mContext)
            ?.inflate(resource, null, false)
        if (inflate != null) {
            setPlaceHolderEmptyView(inflate)
        }
    }

    fun setPlaceHolderEmptyView(errorView: View) {
        this.mPlaceHolderEmptyView = errorView
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.mRecycleView = recyclerView
    }

    abstract fun createVH(viewType: Int, view: View): E
    abstract fun getLayout(viewType: Int): Int
}