package com.android.apphelper2.base.recycleview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class BaseSingRecycleView<T, E : BaseVH, V : ViewBinding>() : RecyclerView.Adapter<E>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): E {
        val resource = getLayout(viewType)
        val view = LayoutInflater.from(parent.context)
            .inflate(resource, parent, false)
        return createVH(viewType,view)
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: E, position: Int) {

    }

    abstract fun createVH(viewType: Int, view: View): E
    abstract fun getLayout(viewType: Int): Int

}