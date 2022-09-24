package com.example.myapplication

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent

/**
 * 没有使用Lifecycle，可能会出现内存泄漏的Dialog
 */
class LifeCycleTipDialog(context: Context):Dialog(context), LifecycleObserver {

    init {
        if (context is LifecycleOwner) {
            context.lifecycle.addObserver(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_tip)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        if (isShowing) {
            dismiss()
        }
    }
}