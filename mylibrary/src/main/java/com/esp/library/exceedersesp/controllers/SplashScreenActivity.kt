package com.esp.library.exceedersesp.controllers

import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import com.esp.library.R
import com.esp.library.utilities.common.Shared
import com.esp.library.exceedersesp.BaseActivity
import com.esp.library.exceedersesp.controllers.setup.LoginScreenActivity
import com.google.firebase.analytics.FirebaseAnalytics



class SplashScreenActivity : BaseActivity() {

    private var mFirebaseAnalytics: FirebaseAnalytics? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.requestFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_splash_screen)
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Shared.getInstance().callIntentWithTime(LoginScreenActivity::class.java, this, 2000, null)


    }

    companion object {

        var ACTIVITY_NAME = "controllers.SplashScreenActivity"
    }

    override fun onBackPressed() {
        //super.onBackPressed()
    }


}
