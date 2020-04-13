package com.esp.library.exceedersesp.controllers.setup

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.esp.library.R
import com.esp.library.utilities.common.Constants
import com.esp.library.utilities.common.Shared
import com.esp.library.utilities.common.SharedPreference
import com.esp.library.exceedersesp.BaseActivity
import com.esp.library.exceedersesp.fragments.setup.LoginScreenFragment
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import utilities.adapters.setup.ListPersonaDAOAdapter
import utilities.data.apis.APIs
import utilities.data.applicants.SettingsDAO
import utilities.data.setup.PersonaDAO
import java.util.concurrent.TimeUnit


class LoginScreenActivity : BaseActivity(), ListPersonaDAOAdapter.RefreshToken{//, InAppUpdateManager.InAppUpdateHandler {
    internal var context: BaseActivity? = null
    internal var fm: androidx.fragment.app.FragmentManager? = null
    internal var submit_request: LoginScreenFragment? = null
    var pref: SharedPreference? = null
    internal var pDialog: AlertDialog? = null
   // private var inAppUpdateManager: InAppUpdateManager? = null

    override fun StatusChange(update: PersonaDAO) {

        if (submit_request != null) {
            submit_request!!.RefreshToken(update)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_screen)
        initailize()
        

        if (Shared.getInstance().ReadPref("url", "base_url", context) != null) {
            Constants.base_url = Shared.getInstance().ReadPref("url", "base_url", context) + Constants.base_url_api
        }/* else {
            Shared.getInstance().WritePref("url", "https://esp.exceeders.com", "base_url", context)
            Constants.base_url = Shared.getInstance().ReadPref("url", "base_url", context) + Constants.base_url_api
        }*/

        else {
            Shared.getInstance().WritePref("url", "https://isp.exceedgulf.com", "base_url", context)
            Constants.base_url = Shared.getInstance().ReadPref("url", "base_url", context) + Constants.base_url_api
        }

        /*else {
            Shared.getInstance().WritePref("url", "https://qaesp.azurewebsites.net", "base_url", context)
            //   Shared.getInstance().WritePref("url", "http://espdemo.azurewebsites.net/", "base_url", context)
            Constants.base_url = Shared.getInstance().ReadPref("url", "base_url", context) + Constants.base_url_api
        }*/

       /* inAppUpdateManager = InAppUpdateManager.Builder(this, REQ_CODE_VERSION_UPDATE)
                .resumeUpdates(true) // Resume the update, if the update was stalled. Default is true
                .mode(InAppConstants.UpdateMode.IMMEDIATE)

        inAppUpdateManager?.checkForAppUpdate()*/

      //  callFragment()
        getSettings()
    }

    private fun initailize() {
        context = this@LoginScreenActivity
        pref = SharedPreference(context)
        pDialog = Shared.getInstance().setProgressDialog(context)

    }

/*
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQ_CODE_VERSION_UPDATE) {
            if (resultCode == Activity.RESULT_CANCELED) {
                // If the update is cancelled by the user,
                // you can request to start the update again.
                inAppUpdateManager?.checkForAppUpdate()

                CustomLogs.displayLogs(ACTIVITY_NAME+" onActivityResult code"+resultCode)

            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    // InAppUpdateHandler implementation

    override fun onInAppUpdateError(code: Int, error: Throwable) {
        *//*
         * Called when some error occurred. See Constants class for more details
         *//*

        CustomLogs.displayLogs(ACTIVITY_NAME+" onInAppUpdateError code"+code+", error")


    }

    override fun onInAppUpdateStatus(status: InAppUpdateStatus) {

        *//*
         * Called when the update status change occurred.
         *//*
        CustomLogs.displayLogs(ACTIVITY_NAME+" onInAppUpdateStatus availableVersionCode "+status.availableVersionCode())
        CustomLogs.displayLogs(ACTIVITY_NAME+" onInAppUpdateStatus isUpdateAvailable "+status.isUpdateAvailable())


        if (status.isDownloaded()) {
            CustomLogs.displayLogs(ACTIVITY_NAME+" onInAppUpdateStatus isUpdateAvailable "+status.isDownloaded())
        }
    }*/


    fun getSettings() {

        start_loading_animation()
        val apis = Shared.getInstance().retroFitObject(context)
        val call = apis.getSettings
        call.enqueue(object : Callback<SettingsDAO> {
            override fun onResponse(call: Call<SettingsDAO>, response: Response<SettingsDAO>?) {

                stop_loading_animation()
                if (response?.body() != null) {
                    val versionCode = response.body().andriodForceVersion?.toInt()
                    if (versionCode!! > Shared.getInstance().getVersionCode(context)) {
                        newVersionDialog()
                    } else {
                        callFragment()
                    }

                    pref?.saveidenediClientId(response.body().idenediClientId);
                } else
                    callFragment()
            }

            override fun onFailure(call: Call<SettingsDAO>, t: Throwable) {
                stop_loading_animation()
                callFragment()
            }
        })


    }

    private fun callFragment() {
        try {
            fm = supportFragmentManager
            val ft = fm!!.beginTransaction()
            submit_request = LoginScreenFragment.newInstance("", "")
            ft.add(R.id.request_fragment, submit_request!!)
            ft.commit()
        }
        catch (e:Exception){}
    }

    private fun newVersionDialog() {
        AlertDialog.Builder(context)
                .setTitle(getString(R.string.newversionavailable))
                .setMessage(getString(R.string.newversionavailabletext))
                .setCancelable(false)
                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(R.string.updatenow) { dialog, which ->

                    val appPackageName = packageName // getPackageName() from Context or Activity object
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.exceedersesp")))
                    onBackPressed()
                }
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
    }


    private fun start_loading_animation() {
        try {
            if (!pDialog!!.isShowing)
                pDialog!!.show()
        } catch (e: java.lang.Exception) {
        }
    }

    private fun stop_loading_animation() {
        try {
            if (pDialog!!.isShowing)
                pDialog!!.dismiss()
        } catch (e: java.lang.Exception) {
        }
    }

    companion object {
        private val REQ_CODE_VERSION_UPDATE = 530
        var ACTIVITY_NAME = "controllers.setup.LoginScreenActivity"

    }

    override fun onBackPressed() {
        super.onBackPressed()

        if (pref?.refreshToken != null)
            pref?.clearPref()
    }
}
