package com.esp.library.exceedersesp

import android.os.Bundle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import android.util.DisplayMetrics
import android.os.Build
import android.app.Activity
import androidx.core.app.ComponentActivity
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import com.esp.library.R


open class BaseActivity : AppCompatActivity() {


    var bContext: BaseActivity? = null
    private var activityDestroyed = false
    private val isMenuVisible = true
    var drawer_layout: androidx.drawerlayout.widget.DrawerLayout? = null


    internal var onStartCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
      //  Shared.getInstance().translucentStatusBar(this)
        bContext = this@BaseActivity
        onStartCount = 1
        if (savedInstanceState == null)
        // 1st time
        {
            this.overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left)
        } else {
            onStartCount = 2
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        activityDestroyed = true

    }


    override fun onStart() {
        super.onStart()
        if (onStartCount > 1) {
            this.overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right)

        } else if (onStartCount == 1) {
            onStartCount++
        }
    }


}
