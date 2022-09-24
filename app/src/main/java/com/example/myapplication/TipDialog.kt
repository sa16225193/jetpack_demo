package com.example.myapplication

import android.app.Dialog
import android.content.Context
import android.os.Bundle

/**
 * 没有使用Lifecycle，可能会出现内存泄漏的Dialog
 */
class TipDialog(context: Context):Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_tip)
    }
}