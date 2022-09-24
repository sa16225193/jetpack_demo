package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

/**
 * 实现了LifecycleOwner的Activity
 * 如果不想实现LifeCycleOwner接口，也可以直接继承AppCompatActivity
 */
class AdvertisingActivity : Activity(), LifecycleOwner {

    //跳过广告按钮
    lateinit var btnIgnore:AppCompatButton
    //广告时间
    lateinit var tvAdvertisingTime:TextView

    lateinit var lifecycleRegistry: LifecycleRegistry

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_advertising)
        btnIgnore = findViewById(R.id.btn_ignore)
        tvAdvertisingTime = findViewById(R.id.tv_advertising_time)
        val advertisingManager = AdvertisingManager()
        lifecycleRegistry = LifecycleRegistry(this)
        lifecycle.addObserver(advertisingManager)
        advertisingManager.advertisingManageListener = object :AdvertisingManager.AdvertisingManageListener {

            override fun timing(second: Int) {
                tvAdvertisingTime.text = "广告剩余$second 秒"
            }

            override fun enterMainActivity() {
                startMainActivity()
            }

        }

        btnIgnore.setOnClickListener{
            startMainActivity()
        }

        //开始广告
        advertisingManager.start()
    }

    private fun startMainActivity() {
        val intent = Intent(this@AdvertisingActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }
}