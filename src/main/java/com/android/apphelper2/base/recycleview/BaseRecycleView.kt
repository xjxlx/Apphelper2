package com.android.apphelper2.base.recycleview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

abstract class BaseRecycleView<T, E : BaseVH> : RecyclerView.Adapter<E>() {

    var mList: MutableList<T> = mutableListOf()
    var mContext: Context? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): E {
        mContext = parent.context

        val resource = getLayout(viewType)
        val view = LayoutInflater.from(parent.context)
            .inflate(resource, parent, false)
        return createVH(viewType, view)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    abstract fun createVH(viewType: Int, view: View): E
    abstract fun getLayout(viewType: Int): Int
}