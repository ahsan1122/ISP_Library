package com.esp.library.exceedersesp.controllers

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.webkit.*
import com.esp.library.R
import com.esp.library.utilities.common.CustomLogs
import com.esp.library.utilities.common.Shared
import com.esp.library.utilities.common.SharedPreference
import com.esp.library.exceedersesp.BaseActivity
import kotlinx.android.synthetic.main.activity_web_view.*
import kotlinx.android.synthetic.main.custom_alert_view.view.*

class WebViewScreenActivity : BaseActivity() {

    internal var TAG = javaClass.simpleName
    internal var context: BaseActivity? = null
    internal var heading: String? = null
    internal var url: String? = null
    internal var pDialog: AlertDialog? = null
    var pref: SharedPreference? = null
    var isIdenediServiceRunning = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)
        initialize()
        // setGravity()
        val bnd = intent.extras
        if (bnd != null) {
            heading = bnd.getString("heading")
            txttitle.text = heading
             url = bnd.getString("url")
         //   url = "https://idenedi-prod-stag.azurewebsites.net/app_permission/?response_type=code&client_id=XSP1980031200&redirect_uri=https://qaesp.azurewebsites.net/login"

        }

        web_view.webViewClient = MyWebViewClient()
        web_view.webChromeClient = YourWebChromeClient();
        web_view.settings.javaScriptEnabled = true
        web_view.settings.domStorageEnabled = true
        // web_view.settings.userAgentString = "Mozilla/5.0 (Linux; U; Android 2.0; en-us; Droid Build/ESD20) AppleWebKit/530.17 (KHTML, like Gecko) Version/4.0 Mobile Safari/530.17"
        web_view.settings.setJavaScriptEnabled(true)
        //  web_view.addJavascriptInterface(JavaScriptInterface(), "AndroidInterface")
        web_view.loadUrl(url)


    }

    class YourWebChromeClient : WebChromeClient() {
        override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean {

            CustomLogs.displayLogs("WebViewScreenActivity alert message =  $message")

            result.confirm()
            return true
        }
    }

    private fun initialize() {
        context = this@WebViewScreenActivity
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""
        supportActionBar?.setDisplayShowHomeEnabled(true)
        pDialog = Shared.getInstance().setProgressDialog(context)
        pref = SharedPreference(context)

        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_nav_back)
        toolbar.navigationIcon?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        toolbar.setNavigationOnClickListener { finish() }
    }

    internal inner class JavaScriptInterface {
        @JavascriptInterface
        fun processHTML(formData: String) {
            CustomLogs.displayLogs("AWESOME_TAG form data: $formData")
        }
    }


    private inner class MyWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)

            return true
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            start_loading_animation()
        }

        override fun onPageFinished(view: WebView, str: String) {
            stop_loading_animation()

            if(intent.extras!!.getBoolean("isIdenedi",false)) {
                if (str.contains("code=")) {
                    str.substring(0, str.lastIndexOf("/"))
                    val code = str.substring(str.indexOf("=") + 1, str.lastIndexOf("&"))
                    CustomLogs.displayLogs("$TAG code: $code")

                    // web_view.loadUrl("https://app.idenedi.com/authorization/?response_type=code&client_id=XSP1980031200&redirect_uri=https://qaesp.azurewebsites.net&&state")
                    if (!isIdenediServiceRunning) {
                        isIdenediServiceRunning = true
                        pref?.saveIdenediCode(code)
                        finish()
                    }
                } else if (str.contains("access_denied")) {
                    finish()
                } else if (str.contains("error=")) {
                    CustomLogs.displayLogs("$TAG Error")
                }
            }


        }
    }

    private fun start_loading_animation() {
        try {
            if (!pDialog!!.isShowing())
                pDialog!!.show()
        } catch (e: java.lang.Exception) {
        }

    }

    private fun stop_loading_animation() {
        try {
            if (pDialog!!.isShowing())
                pDialog!!.dismiss()
        } catch (e: java.lang.Exception) {
        }
    }


    companion object {

        var ACTIVITY_NAME = "controllers.WebViewScreenActivity"
    }

}
