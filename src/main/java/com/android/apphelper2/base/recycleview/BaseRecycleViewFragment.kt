package com.android.apphelper2.base.recycleview

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.apphelper2.R
import com.android.apphelper2.app.AppHelper2

abstract class BaseRecycleViewFragment<T, E : BaseVH> : RecyclerView.Adapter<BaseVH>() {

    protected var mList: MutableList<T> = mutableListOf()
    protected lateinit var mContext: Context
    protected lateinit var mParent: ViewGroup
    protected lateinit var mLayoutInflater: LayoutInflater

    // 默认是有数据的布局
    private var mViewType: ViewTypeEnum = ViewType.TYPE_DATA
    private var mRecycleView: RecyclerView? = null

    private var mPlaceHolderEmptyView: View? = null
    private var mPlaceHolderEmptyTryClickListener: View.OnClickListener? = null
    private var mPlaceHolderErrorView: View? = null
    private var mPlaceHolderErrorTryClickListener: View.OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseVH {
        mContext = parent.context
        mParent = parent
        mLayoutInflater = LayoutInflater.from(parent.context)

        var vh: BaseVH? = null
        if (mViewType == ViewType.TYPE_DATA) {
            vh = createVH(viewType, parent)
        } else if (mViewType == ViewType.TYPE_EMPTY) {
            if (mPlaceHolderEmptyView == null) {
                AppHelper2.getBuilder()
                    ?.let { builder ->
                        mPlaceHolderEmptyView = LayoutInflater.from(parent.context)
                            .inflate(builder.placeHolderRecycleTempView, parent, false)
                    }
            }
            mPlaceHolderEmptyView?.let {
                vh = EmptyVH(it)
            }
        } else if (mViewType == ViewType.TYPE_ERROR) {
            if (mPlaceHolderErrorView == null) {
                AppHelper2.getBuilder()
                    ?.let { builder ->
                        mPlaceHolderErrorView = LayoutInflater.from(parent.context)
                            .inflate(builder.placeHolderRecycleErrorView, parent, false)
                    }
            }
            mPlaceHolderErrorView?.let {
                vh = ErrorVH(it)
            }
        }
        assert(vh != null)
        return vh!!
    }

    override fun onBindViewHolder(holder: BaseVH, position: Int) {
        if (mViewType == ViewType.TYPE_DATA) {
            onBindViewHolders(holder as E, position)
        } else if (mViewType == ViewType.TYPE_EMPTY) {
            if (holder is EmptyVH) {
                holder.itemView.findViewById<View>(R.id.iv_base_error_placeholder)
                    ?.let {
                        mPlaceHolderEmptyTryClickListener?.let { listener ->
                            it.visibility = View.VISIBLE
                            it.setOnClickListener(listener)
                        }
                    }
            }
        } else if (mViewType == ViewType.TYPE_ERROR) {
            if (holder is ErrorVH) {
                holder.itemView.findViewById<View>(R.id.tv_base_error_refresh)
                    ?.let {
                        mPlaceHolderErrorTryClickListener?.let { listener ->
                            it.visibility = View.VISIBLE
                            it.setOnClickListener(listener)
                        }
                    }
            }
        }
    }

    abstract fun onBindViewHolders(holder: E, position: Int)

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

    /**
     * @return return an RecyclerView.ViewHolder ，
     * ex：
     * return VH2(ItemTestBinding.inflate(mLayoutInflater, parent, false))
     */
    abstract fun createVH(viewType: Int, parent: ViewGroup): E

    fun placeHolderEmptyTryClick(listener: View.OnClickListener) {
        this.mPlaceHolderEmptyTryClickListener = listener
    }

    fun placeHolderErrorTryClick(listener: View.OnClickListener) {
        this.mPlaceHolderErrorTryClickListener = listener
    }
}