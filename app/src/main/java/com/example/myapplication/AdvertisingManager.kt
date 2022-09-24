package com.example.myapplication

import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

class AdvertisingManager: LifecycleObserver {
    var TAG = "AdvertisingManager"
    //监听事件
    var advertisingManageListener:AdvertisingManageListener? = null

    //定时器
    private var countDownTimer:CountDownTimer? = object :CountDownTimer(5000, 1000) {
        override fun onTick(p0: Long) {
            Log.d(TAG, "广告剩余${(p0 / 1000).toInt()}秒")
            advertisingManageListener?.timing((p0 / 1000).toInt())
        }

        override fun onFinish() {
            Log.d(TAG, "广告结束，准备进入主页面")
            advertisingManageListener?.enterMainActivity()
        }
    }

    /**
     * 开始计时
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun start() {
        Log.d(TAG, "开始计时")
        countDownTimer?.start()
    }

    /**
     * 停止计时
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onCancel() {
        Log.d(TAG, "停止计时")
        countDownTimer?.cancel()
        countDownTimer = null
    }

    /**
     * 广告管理接口
     */
    interface AdvertisingManageListener {

        /**
         * 计时
         * @param second 秒
         */
        fun timing(second:Int)

        /**
         * 计时结束，进入主页面
         */
        fun enterMainActivity()
    }
}