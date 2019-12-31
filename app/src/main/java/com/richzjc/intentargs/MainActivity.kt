package com.richzjc.intentargs

import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity

import com.richzjc.annotation.Parameter
import com.richzjc.anotation_api.manager.ParameterManager

class MainActivity : AppCompatActivity() {

    @JvmField
    @Parameter(name = "rich")
    var sarr11: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_main)
        ParameterManager.getInstance().loadParameter(this)
    }
}
