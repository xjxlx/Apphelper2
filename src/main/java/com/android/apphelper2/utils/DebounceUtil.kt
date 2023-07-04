package com.android.apphelper2.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce

class DebounceUtil<T>(private val debounceTime: Long) {

    private val mFlow: MutableSharedFlow<T> by lazy {
        return@lazy MutableSharedFlow()
    }

    suspend fun listener(block: (T) -> Unit) {
        mFlow.debounce(debounceTime)
            .collect {
                block(it)
            }
    }

    suspend fun send(t: T) {
        mFlow.emit(t)
    }
}