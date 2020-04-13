package com.esp.library.exceedersesp.fragments.setup


import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import com.esp.library.R
import com.esp.library.exceedersesp.ESPApplication
import com.esp.library.utilities.common.*
import com.esp.library.exceedersesp.BaseActivity
import com.esp.library.exceedersesp.controllers.Profile.ProfileMainActivity
import com.esp.library.exceedersesp.controllers.SplashScreenActivity
import com.esp.library.exceedersesp.controllers.WebViewScreenActivity
import com.esp.library.exceedersesp.controllers.applications.ApplicationsActivityDrawer
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_login_screen.view.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import utilities.data.apis.APIs
import utilities.data.applicants.profile.ApplicationProfileDAO
import utilities.data.setup.*
import utilities.model.Labels
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit


class LoginScreenFragment : androidx.fragment.app.Fragment() {

    internal var TAG = "LoginScreenFragment"

    internal lateinit var context: BaseActivity

    internal var email: String? = null
    internal var password: String? = null
    internal var imm: InputMethodManager? = null
    internal var token: TokenDAO? = null

    internal var login_call: Call<TokenDAO>? = null

    internal var anim: ProgressBarAnimation? = null

    internal var alertOrgWindow: SelectOrganizationWindow? = null
    internal var pref: SharedPreference? = null
    internal var ClickCount = 0

    internal var pDialog: AlertDialog? = null
    internal var txtenteremailpass: TextView? = null
    var applicationProfilebody: ApplicationProfileDAO? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val v = inflater.inflate(R.layout.fragment_login_screen, container, false)
        initailize(v)
        setGravity(v)


        v.forgot_password.setOnClickListener { view ->
            val bn = Bundle()
            bn.putString("heading", getString(R.string.forgot_password))
            //   bn.putString("url", "https://qaesp.azurewebsites.net/forgotpassword")
            bn.putString("url", Constants.base_url.replace("webapi/", "") + "forgotpassword")
            Shared.getInstance().callIntent(WebViewScreenActivity::class.java, context, bn)
        }

        CustomLogs.displayLogs("$TAG getDeviceId: ${Shared.getInstance().getDeviceId(context)}")


        if (Shared.getInstance().ReadPref("url", "base_url", context) != null) {
            val url_ = Shared.getInstance().ReadPref("url", "base_url", context)
            v.url_text.setText(url_)
        } else {
            v.url_view.visibility = View.VISIBLE
            v.login_view.visibility = View.GONE
        }

        v.url_ok.setOnClickListener { view ->
            val new_base_url = v.url_text.text.toString()
            if (new_base_url.length == 0) {
                Shared.getInstance().messageBox(getString(R.string.enterbaseurl), context)
            } else if (!Shared.getInstance().checkURL(new_base_url)) {
                Shared.getInstance().messageBox(getString(R.string.entervalidurl), context)
            } else {
                if (Shared.getInstance().ReadPref("url", "base_url", context) != null) {
                    Shared.getInstance().WritePref("url", null, "base_url", context)
                }
                Shared.getInstance().WritePref("url", new_base_url, "base_url", context)
                Constants.base_url = Shared.getInstance().ReadPref("url", "base_url", context) + Constants.base_url_api
                v.url_view.visibility = View.GONE
                v.login_view.visibility = View.VISIBLE
            }
        }

        v.logo.setOnClickListener { view ->
            ClickCount++

            if (ClickCount > 5) {
                Shared.getInstance().messageBox(getString(R.string.youaredevelopernow), context)
                v.url_view.visibility = View.VISIBLE
                v.login_view.visibility = View.GONE
                ClickCount = 0
            }
        }


        if (pref?.getidenediClientId().isNullOrEmpty()) {
            v.idenedi_login_btn.visibility = View.GONE
            v.txtor.visibility = View.GONE
        }

        v.idenedi_login_btn.setOnClickListener {
            val bn = Bundle()
            bn.putString("heading", getString(R.string.logineithidenedi))

               bn.putString("url", "https://app.idenedi.com/app_permission/?response_type=code&client_id=" + pref?.getidenediClientId() + "&redirect_uri=https://isp.exceedgulf.com/login")
           //    bn.putString("url", "https://app.idenedi.com/app_permission/?response_type=code&client_id=" + pref?.getidenediClientId() + "&redirect_uri=https://esp.exceeders.com/login")
      //      bn.putString("url", "https://idenedi-prod-stag.azurewebsites.net/app_permission/?response_type=code&client_id=" + pref?.getidenediClientId() + "&redirect_uri=https://qaesp.azurewebsites.net/login")
            bn.putBoolean("isIdenedi", true)
            Shared.getInstance().callIntent(WebViewScreenActivity::class.java, context, bn)
        }

        v.email_input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun afterTextChanged(editable: Editable) {


                val outputedText = editable.toString()

                if (!Shared.getInstance().isValidEmailAddress(outputedText)) {
                    v.tilFieldLabel.isErrorEnabled = true
                    v.tilFieldLabel.error = context.getString(R.string.invalidemail)
                } else {
                    v.tilFieldLabel.isErrorEnabled = false
                    v.tilFieldLabel.error = null
                }


            }
        })


        val username_pre = Shared.getInstance().ReadPref("Uname", "login_info", context)
        var password_pre = Shared.getInstance().ReadPref("Pass", "login_info", context)


        if (username_pre != null && (password_pre != null && !password_pre.equals(""))) {

            email = username_pre
            password = password_pre


            v.email_input.setText(username_pre)
            v.password_input.setText(password_pre)

            if (Shared.getInstance().isWifiConnected(context)) {
                val pt = PostTokenDAO()
                pt.client_id = "ESPMobile"
                pt.grant_type = "password"
                pt.password = password_pre
                pt.username = username_pre

                GetToken(pt)


            } else {
                Shared.getInstance().showAlertMessage(getString(R.string.internet_error_heading), getString(R.string.internet_connection_error), context)
            }


        } else {
            getIdenediUser()
        }

         pref?.savelanguage("en")
     //   populateSpinner(v)

        FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {

                        CustomLogs.displayLogs("$TAG getFirebaseInstanceId failed $task.exception")
                        return@OnCompleteListener
                    }
                    // Get new Instance ID token
                    val firebaseToken = task.result?.token
                    val firebasetokenid = task.result?.id

                    pref?.saveFirebaseToken(firebaseToken)
                    // Log and toast
                    CustomLogs.displayLogs("$TAG getFirebaseInstanceId token $firebaseToken \ntokenid: $firebasetokenid")
                })



        return v
    }

    private fun initailize(v: View) {
        context = activity as BaseActivity
        pref = SharedPreference(context)
        pDialog = Shared.getInstance().setProgressDialog(context)
        txtenteremailpass = v.findViewById(R.id.txtenteremailpass)
        imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        v.url_view.visibility = View.GONE
        v.login_btn.setOnClickListener { view -> SignInUser(v) }


    }

    private fun getIdenediUser() {
        if (pref?.refreshToken != null) {
            val pt = PostTokenDAO()
            pt.client_id = "ESPMobile"
            pt.grant_type = "refresh_token"
            pt.password = ""
            pt.username = ""
            pt.scope = Shared.getInstance().ReadPref("scropId", "login_info", context)
            pt.refresh_token = pref?.refreshToken
            GetRefreshToken(pt)
        }
    }

    private fun populateSpinner(v: View) {

        if (pref?.language.equals("", ignoreCase = true)) {
            pref?.savelanguage(Locale.getDefault().language)
            setApplicationlanguage(v)
        } else {

            setApplicationlanguage(v)
        }

        if (pref?.language.equals(Locale.getDefault().getLanguage(), ignoreCase = true)) {
            pref?.savelanguage(Locale.getDefault().getLanguage());
            setApplicationlanguage(v);
        }


        val arrayLanguages = ArrayList<String>()
        arrayLanguages.add(getString(R.string.english))
        arrayLanguages.add(getString(R.string.arabic))

        val adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, arrayLanguages)
        v.splanguage.adapter = adapter

        if (pref?.language.equals("en", ignoreCase = true))
            v.splanguage.setSelection(0)
        else if (pref?.language.equals("ar", ignoreCase = true))
            v.splanguage.setSelection(1)
        /*else if (pref.getLanguage().equalsIgnoreCase("fr"))
            splanguage.setSelection(2);*/

        v.splanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                //changeLanguage(arrayLanguages[i], v)
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {

            }
        }

    }

    private fun changeLanguage(languageType: String, v: View) {
        val language: String

        if (languageType.equals(getString(R.string.arabic), ignoreCase = true))
            language = "ar"
        else
            language = "en"

        pref?.savelanguage(language)
        setApplicationlanguage(v)


    }

    fun setApplicationlanguage(v: View) {
        val res = resources
        val dm = res.displayMetrics
        val conf = res.configuration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            conf.setLocale(Locale(pref?.language)) // API 17+ only.
        } else {
            conf.locale = Locale(pref?.language)
        }
        res.updateConfiguration(conf, dm)

        updateFields(v)
    }

    private fun applyLanguage() {
        val res = resources
        val dm = res.displayMetrics
        val conf = res.configuration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            conf.setLocale(Locale(pref?.language)) // API 17+ only.
        } else {
            conf.locale = Locale(pref?.language)
        }
        res.updateConfiguration(conf, dm)


    }


    private fun updateFields(v: View) {
        v.txtwelcome.text = getString(R.string.welcometoesp)
        txtenteremailpass?.text = getString(R.string.enteremailandpassword)
        v.tilFieldLabel.hint = getString(R.string.email)
        v.tilFieldLabelPassword.hint = getString(R.string.password)
        v.login_btn.text = getString(R.string.sign_in)
        v.forgot_password.text = getString(R.string.forgotpassword)
        v.txtlanguage.text = getString(R.string.language)
        setGravity(v)
    }

    private fun setGravity(v: View) {
        if (pref?.language.equals("ar", ignoreCase = true)) {
            v.email_input.gravity = Gravity.END
            v.password_input.gravity = Gravity.END

        } else {
            v.email_input.gravity = Gravity.START
            v.password_input.gravity = Gravity.START
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    private fun SignInUser(v: View) {

        imm!!.hideSoftInputFromWindow(view!!.windowToken, 0)

        email = null
        password = null

        email = v.email_input.text.toString().trim { it <= ' ' }
        password = v.password_input.text.toString().trim { it <= ' ' }


        if (email!!.length == 0) {

            Shared.getInstance().showAlertMessage(context.getString(R.string.login_email_label), context.getString(R.string.login_email_error), context)
            return
        }

        if (!Shared.getInstance().isValidEmailAddress(email)) {

            Shared.getInstance().showAlertMessage(context.getString(R.string.login_email_label), context.getString(R.string.login_email_error), context)
            return
        }

        if (password!!.length == 0) {

            Shared.getInstance().showAlertMessage(context.getString(R.string.login_password_label), context.getString(R.string.login_valid_password_error), context)
            return

        }

        if (password!!.length < 6) {
            Shared.getInstance().showAlertMessage(context.getString(R.string.login_password_label), context.getString(R.string.length_password_error), context)
            return

        }


        if (Shared.getInstance().isWifiConnected(context)) {
            val pt = PostTokenDAO()
            pt.client_id = "ESPMobile"
            pt.grant_type = "password"
            pt.password = password
            pt.username = email



            GetToken(pt)

        } else {
            Shared.getInstance().showAlertMessage(getString(R.string.internet_error_heading), getString(R.string.internet_connection_error), context)
        }

    }

    fun RefreshToken(personaDAO: PersonaDAO) {

        if (alertOrgWindow != null) {
            alertOrgWindow!!.dismiss()
        }

        if (Shared.getInstance().isWifiConnected(context)) {

            val pt = PostTokenDAO()
            pt.client_id = "ESPMobile"
            pt.grant_type = "refresh_token"
            pt.password = password
            pt.username = email
            pt.scope = personaDAO.id
            pt.refresh_token = personaDAO.refresh_token

            Shared.getInstance().WritePref("scropId", personaDAO.id, "login_info", context)

            GetRefreshToken(pt)

        } else {

            Shared.getInstance().showAlertMessage(getString(R.string.internet_error_heading), getString(R.string.internet_connection_error), context)


        }
    }

    private fun linkIdendiUser(isSentToProfile: Boolean) {

        start_loading_animation()

        try {

            val apis = Shared.getInstance().retroFitObject(context)
            var call_idenediToken = apis.linkIdenediUser(pref?.getidenediAuthDAO())


            call_idenediToken.enqueue(object : Callback<IdenediAuthDAO> {
                override fun onResponse(call: Call<IdenediAuthDAO>?, response: Response<IdenediAuthDAO>?) {

                    stop_loading_animation()

                    if (response?.isSuccessful!!) {
                        pref?.saveidenediAuthDAO(null)
                        pref?.saveRefreshToken(null)

                        if (isSentToProfile) {
                            val intentToBeNewRoot = Intent(context, ProfileMainActivity::class.java)
                            val cn = intentToBeNewRoot.component
                            val mainIntent = Intent.makeRestartActivityTask(cn)
                            mainIntent.putExtra("dataapplicant", applicationProfilebody)
                            startActivity(mainIntent)
                        } else
                            Shared.getInstance().callIntentClearAllActivities(ApplicationsActivityDrawer::class.java, context, null)
                    } else {

                        Shared.getInstance().showAlertMessage(getString(R.string.login_label), getString(R.string.some_thing_went_wrong), context)
                    }
                }


                override fun onFailure(call: Call<IdenediAuthDAO>, t: Throwable) {
                    CustomLogs.displayLogs("$TAG there")
                    stop_loading_animation()
                    Shared.getInstance().showAlertMessage(getString(R.string.login_label), getString(R.string.some_thing_went_wrong), context)

                }
            })

        } catch (ex: Exception) {

            ex.printStackTrace()
            stop_loading_animation()
            Shared.getInstance().showAlertMessage(getString(R.string.login_label), getString(R.string.some_thing_went_wrong), context)

        }

    }

    private fun GetToken(postTokenDAO: PostTokenDAO) {

        start_loading_animation()

        try {

            val apis = Shared.getInstance().retroFitObject(context)
            login_call = apis.getToken(postTokenDAO.grant_type, postTokenDAO.username, postTokenDAO.password, postTokenDAO.client_id)

            login_call!!.enqueue(object : Callback<TokenDAO> {
                override fun onResponse(call: Call<TokenDAO>, response: Response<TokenDAO>?) {

                    if (response != null && response.body() != null) {

                        // Shared.getInstance().showAlertMessage("login",response.body().getEmail(),context);
                        getTokenData(response)


                    } else {

                        try {
                            val errorMsg = invalidAttempts(response!!.errorBody().string())
                            Shared.getInstance().showAlertMessage(getString(R.string.login_label), errorMsg, context)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }


                    }
                }

                override fun onFailure(call: Call<TokenDAO>, t: Throwable) {
                    CustomLogs.displayLogs("$TAG theree")
                    stop_loading_animation()
                    Shared.getInstance().showAlertMessage(getString(R.string.login_label), getString(R.string.some_thing_went_wrong), context)

                }
            })

        } catch (ex: Exception) {

            ex.printStackTrace()
            stop_loading_animation()
            Shared.getInstance().showAlertMessage(getString(R.string.login_label), getString(R.string.some_thing_went_wrong), context)

        }

    }

    private fun getTokenData(response: Response<TokenDAO>) {

        if (response.body().personas != null && response.body().personas.length > 0) {

            ESPApplication.getInstance().tokenPersonas = response.body()

            val applicantPersonaId = response.body().applicantPersonaId?.toIntOrNull() ?: 0
            if (applicantPersonaId > 0 && (response.body().role.equals(Enums.admin.toString(), ignoreCase = true)
                            || response.body().role.equals(Enums.user.toString(), ignoreCase = true))) {
                response.body().role = Enums.applicant.toString()
            } else if (applicantPersonaId == 0 && (response.body().role.equals(Enums.admin.toString(), ignoreCase = true)
                            || response.body().role.equals(Enums.user.toString(), ignoreCase = true))) {
                response.body().role = Enums.assessor.toString()
            } else if (applicantPersonaId == 0 && (response.body().role.equals(Enums.applicant.toString(), ignoreCase = true))) {
                response.body().role = Enums.applicant.toString()
            }


            var list: List<PersonaDAO>? = null
            val personas = response.body().personas
            val gson = Gson()
            list = gson.fromJson<List<PersonaDAO>>(personas, object : TypeToken<List<PersonaDAO>>() {

            }.type)

            if (list != null && list.size > 0) {
                val app_list = Shared.getInstance().GetApplicantOrganization(list, context)
                if (app_list == null || app_list.size == 0) {
                    stop_loading_animation()
                    Shared.getInstance().showAlertMessage(getString(R.string.login_label), getString(R.string.login_error), context)
                    return
                }
                response.body().password = password

                if (app_list.size == 1) {
                    //User is Applicant and logged in
                    val userDAO = UserDAO()
                    userDAO.loginResponse = response.body()

                    ESPApplication.getInstance().user = userDAO
                    ESPApplication.getInstance().filter = Shared.getInstance().ResetApplicationFilter(context)

                    Shared.getInstance().WritePref("Uname", response.body().email, "login_info", context)
                    Shared.getInstance().WritePref("Pass", response.body().password, "login_info", context)

                    try {
                        if (Shared.getInstance().ReadPref("scropId", "login_info", context) != null) {
                            Shared.getInstance().WritePref("scropId", null, "login_info", context)
                        }

                        Shared.getInstance().WritePref("scropId", app_list[0].id, "login_info", context)

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    var role = response.body().role
                    if (role.equals(Enums.admin.toString(), ignoreCase = true)
                            || role.equals(Enums.user.toString(), ignoreCase = true) ||
                            role.equals(Enums.assessor.toString(), ignoreCase = true)) {
                        role = Enums.assessor.toString()
                    }
                    pref?.saveSelectedUserRole(role)

                    getlabels(response)


                } else if (app_list.size > 1) {

                    var ScropId: String? = null

                    try {
                        if (Shared.getInstance().ReadPref("scropId", "login_info", context) != null) {
                            ScropId = Shared.getInstance().ReadPref("scropId", "login_info", context)
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }


                    if (ScropId != null && ScropId.length > 0) {

                        val pt = PostTokenDAO()
                        pt.client_id = "ESPMobile"
                        pt.grant_type = "refresh_token"
                        pt.password = response.body().password
                        pt.username = response.body().email
                        pt.scope = ScropId
                        pt.refresh_token = response.body().refresh_token
                        GetRefreshToken(pt)

                    } else {

                        stop_loading_animation()
                        alertOrgWindow = SelectOrganizationWindow.newInstance(response.body())
                        alertOrgWindow!!.show(fragmentManager!!, getString(R.string.alert))
                        alertOrgWindow!!.isCancelable = false


                        //getFragmentManager
                    }
                }

            }
        } else {
            stop_loading_animation()
            Shared.getInstance().showAlertMessage(getString(R.string.login_label), getString(R.string.login_error), context)

        }

    }

    private fun GetRefreshToken(postTokenDAO: PostTokenDAO) {

        start_loading_animation()

        try {

            val apis = Shared.getInstance().retroFitObject(context)
            login_call = apis.getRefreshToken(postTokenDAO.scope, postTokenDAO.grant_type, postTokenDAO.username, postTokenDAO.password, postTokenDAO.client_id, postTokenDAO.scope, postTokenDAO.refresh_token)


            login_call!!.enqueue(object : Callback<TokenDAO> {
                override fun onResponse(call: Call<TokenDAO>, response: Response<TokenDAO>?) {


                    if (response != null && response.body() != null) {

                        if (response.body().personas != null && response.body().personas.length > 0) {


                            if (ESPApplication.getInstance().user != null) {
                                ESPApplication.getInstance().user = null
                            }


                            val applicantPersonaId = response.body().applicantPersonaId?.toIntOrNull()
                                    ?: 0
                            if (applicantPersonaId > 0 && (response.body().role.equals(Enums.admin.toString(), ignoreCase = true)
                                            || response.body().role.equals(Enums.user.toString(), ignoreCase = true))) {
                                response.body().role = Enums.applicant.toString()
                            } else if (applicantPersonaId == 0 && (response.body().role.equals(Enums.admin.toString(), ignoreCase = true)
                                            || response.body().role.equals(Enums.user.toString(), ignoreCase = true))) {
                                response.body().role = Enums.assessor.toString()
                            } else if (applicantPersonaId == 0 && (response.body().role.equals(Enums.applicant.toString(), ignoreCase = true))) {
                                response.body().role = Enums.applicant.toString()
                            }

                            val userDAO = UserDAO()
                            response.body().password = postTokenDAO.password
                            userDAO.loginResponse = response.body()

                            ESPApplication.getInstance().user = userDAO
                            ESPApplication.getInstance().filter = Shared.getInstance().ResetApplicationFilter(context)

                            Shared.getInstance().WritePref("Uname", postTokenDAO.username, "login_info", context)
                            Shared.getInstance().WritePref("Pass", postTokenDAO.password, "login_info", context)


                            getlabels(response)


                        } else {
                            stop_loading_animation()
                            Shared.getInstance().showAlertMessage(getString(R.string.login_label), getString(R.string.applicant_error), context)
                        }


                    } else {
                        stop_loading_animation()
                        Shared.getInstance().showAlertMessage(getString(R.string.login_label), getString(R.string.some_thing_went_wrong), context)

                    }
                }

                override fun onFailure(call: Call<TokenDAO>, t: Throwable?) {

                    stop_loading_animation()

                    if (t != null && isAdded) {
                        Shared.getInstance().showAlertMessage(getString(R.string.login_label), getString(R.string.some_thing_went_wrong), context)
                    }
                    return

                }
            })

        } catch (ex: Exception) {
            stop_loading_animation()
            if (isAdded) {
                Shared.getInstance().showAlertMessage(getString(R.string.login_label), getString(R.string.some_thing_went_wrong), context)
            }
        }

    }

    private fun sendIdendiCode() {

        start_loading_animation()

        var getError: String = ""

        try {
            //  val logging = HttpLoggingInterceptor()
            val logging = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { message ->

                when (Shared.getInstance().isJSONValid(message)) {
                    true -> getError = message;
                }

            })
            logging.level = HttpLoggingInterceptor.Level.BODY
            val httpClient = OkHttpClient.Builder()
            httpClient.addInterceptor(logging)

            httpClient.addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                        .header("locale", Shared.getInstance().getLanguageSimpleContext(context));
                //   .header("Content-Type ", "application/x-www-form-urlencoded")
                val request = requestBuilder.build()
                chain.proceed(request)
            }


            httpClient.connectTimeout(10, TimeUnit.SECONDS)
            httpClient.readTimeout(10, TimeUnit.SECONDS)
            httpClient.writeTimeout(10, TimeUnit.SECONDS)

            val gson = GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                    .create()

            val retrofit = Retrofit.Builder()
                    .baseUrl(Constants.base_url)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(httpClient.build())

                    .build()


            val apis = retrofit.create(APIs::class.java)
            // var call_idenediToken = apis.getIdenediToken()

            var call_idenediToken = apis.getIdenedirefreshToken("password",
                    "", "", "ESPMobile", pref?.idenediCode!!)


            call_idenediToken.enqueue(object : Callback<TokenDAO> {
                override fun onResponse(call: Call<TokenDAO>?, response: Response<TokenDAO>?) {

                    stop_loading_animation()

                    //   CustomLogs.displayLogs("$TAG Full_Response: "+Gson().toJson(response))

                    pref?.saveIdenediCode(null)
                    if (response != null && response.body() != null) {
                        val refreshToken = response.body()?.refresh_token
                        pref?.saveRefreshToken(refreshToken)
                        getTokenData(response)

                    } else {

                        if (response?.code() == 400) {

                            val idenediAuthDAO = Gson().fromJson(getError, IdenediAuthDAO::class.java)
                            val error_description = idenediAuthDAO?.error_description
                            val errorDescriptionObj = Gson().fromJson(error_description, IdenediAuthDAO::class.java)
                            val refreshToken = errorDescriptionObj.RefreshToken
                            pref?.saveRefreshToken(refreshToken)

                            val idenediDAO = IdenediAuthDAO()
                            idenediDAO.RefreshToken = refreshToken
                            idenediDAO.AccessToken = errorDescriptionObj.AccessToken
                            idenediDAO.EmailAddress = errorDescriptionObj.EmailAddress
                            idenediDAO.IdenediId = errorDescriptionObj.IdenediId

                            pref?.saveidenediAuthDAO(idenediDAO)

                            txtenteremailpass?.text = getString(R.string.idenedinotlinkedtext)
                            view?.login_btn?.text = getString(R.string.logintolink)
                            view?.idenedi_login_btn?.visibility = View.GONE
                            view?.txtor?.visibility = View.GONE


                        } else
                            pref?.saveIdenediCode(null)
                    }
                }


                override fun onFailure(call: Call<TokenDAO>, t: Throwable) {
                    CustomLogs.displayLogs("$TAG thereee")
                    pref?.saveIdenediCode(null)
                    stop_loading_animation()
                    Shared.getInstance().messageBox(getString(R.string.some_thing_went_wrong), context as Activity?)

                }
            })

        } catch (ex: Exception) {
            pref?.saveIdenediCode(null)
            ex.printStackTrace()
            stop_loading_animation()
            Shared.getInstance().messageBox(getString(R.string.some_thing_went_wrong), context as Activity?)

        }

    }

    private fun invalidAttempts(errorBody: String): String {
        var remainingVal = 0
        var errorMsg = getString(R.string.login_error)
        try {
            val jsonObject = JSONObject(errorBody)
            val error = jsonObject.getString("error")
            val error_description = jsonObject.getString("error_description")
            if (error.equals(getString(R.string.locked), ignoreCase = true)) {
                errorMsg = getString(R.string.login_error_account_locked)
                remainingVal = -1
            } else {
                val remainingSplit = error.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (remainingSplit[1] != null)
                    remainingVal = Integer.parseInt(remainingSplit[1])
            }
            CustomLogs.displayLogs("$TAG error: $error error_description: $error_description remainingAttempts: $remainingVal")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        stop_loading_animation()
        if (remainingVal == 3)
            errorMsg = getString(R.string.login_error_remaining3)
        else if (remainingVal == 2)
            errorMsg = getString(R.string.login_error_remaining2)
        else if (remainingVal == 1)
            errorMsg = getString(R.string.login_error_remaining1)
        else if (remainingVal > 5)
            errorMsg = getString(R.string.login_error_locked)

        return errorMsg
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

    private fun getlabels(serviceResponse: Response<TokenDAO>?) {
        start_loading_animation()
        try {

            val labels_call = Shared.getInstance().retroFitObject(context).getLabels()

            labels_call.enqueue(object : Callback<Labels> {
                override fun onResponse(call: Call<Labels>, response: Response<Labels>) {

                    val pref = SharedPreference(context)
                    pref.savelabels(response.body())


                    val personas = ESPApplication.getInstance()?.user?.loginResponse?.personas
                    val organizationId = ESPApplication.getInstance()?.user?.loginResponse?.organizationId

                    try {
                        val jsonArray = JSONArray(personas)
                        for (i in 0 until jsonArray.length()) {
                            val jobj = jsonArray.getJSONObject(i)

                            val orgId = jobj.getString("orgId")


                            if (organizationId.equals(orgId, ignoreCase = true)) {
                                val locales = jobj.getString("locales")
                                val myList = ArrayList(Arrays.asList(*locales.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))
                                val role = serviceResponse?.body()?.role
                                ESPApplication.getInstance().user.role = role

                                if (role.equals(Enums.admin.toString(), ignoreCase = true)
                                        || role.equals(Enums.user.toString(), ignoreCase = true) ||
                                        role.equals(Enums.assessor.toString(), ignoreCase = true)) {
                                    stop_loading_animation()
                                    if (myList.contains(pref.language)) {
                                        if (txtenteremailpass?.text.toString() == getString(R.string.idenedinotlinkedtext))
                                            linkIdendiUser(false)
                                        else
                                            Shared.getInstance().callIntentClearAllActivities(ApplicationsActivityDrawer::class.java, context, null)
                                    } else {
                                        pref.savelanguage(myList[0])
                                        applyLanguage()
                                        val intentToBeNewRoot = Intent(context, SplashScreenActivity::class.java)
                                        val cn = intentToBeNewRoot.component
                                        val mainIntent = Intent.makeRestartActivityTask(cn)
                                        startActivity(mainIntent)
                                    }
                                } else
                                    getApplicant(myList)


                            }

                        }
                    } catch (e: JSONException) {
                        stop_loading_animation()
                        e.printStackTrace()
                    }


                }

                override fun onFailure(call: Call<Labels>, t: Throwable) {
                    stop_loading_animation()
                    Shared.getInstance().messageBox(getString(R.string.some_thing_went_wrong), context)
                }
            })


        } catch (ex: Exception) {
            ex.printStackTrace()
            stop_loading_animation()
            Shared.getInstance().messageBox(getString(R.string.some_thing_went_wrong), context)
        }

    }

    private fun getApplicant(myList: List<String>) {
        start_loading_animation()
        try {

            val apis = Shared.getInstance().retroFitObject(context)
            val labels_call = apis.Getapplicant()

            labels_call.enqueue(object : Callback<ApplicationProfileDAO> {
                override fun onResponse(call: Call<ApplicationProfileDAO>, response: Response<ApplicationProfileDAO>) {
                    stop_loading_animation()
                    applicationProfilebody = response.body()

                    if (applicationProfilebody != null) {
                        val profileSubmitted = response.body().applicant.isProfileSubmitted

                        if (profileSubmitted) {
                            if (myList.contains(pref?.language)) {
                                if (txtenteremailpass?.text?.toString() == context.getString(R.string.idenedinotlinkedtext))
                                    linkIdendiUser(false)
                                else
                                    Shared.getInstance().callIntentClearAllActivities(ApplicationsActivityDrawer::class.java, context, null)
                            } else {
                                pref?.savelanguage(myList[0])
                                applyLanguage()
                                val intentToBeNewRoot = Intent(context, SplashScreenActivity::class.java)
                                val cn = intentToBeNewRoot.component
                                val mainIntent = Intent.makeRestartActivityTask(cn)
                                startActivity(mainIntent)
                            }
                        } else {
                            if (txtenteremailpass?.text.toString() == getString(R.string.idenedinotlinkedtext))
                                linkIdendiUser(true)
                            else {
                                val intentToBeNewRoot = Intent(context, ProfileMainActivity::class.java)
                                val cn = intentToBeNewRoot.component
                                val mainIntent = Intent.makeRestartActivityTask(cn)
                                mainIntent.putExtra("dataapplicant", applicationProfilebody)
                                startActivity(mainIntent)
                            }
                        }
                    } else
                        Shared.getInstance().messageBox(getString(R.string.some_thing_went_wrong), context)
                }

                override fun onFailure(call: Call<ApplicationProfileDAO>, t: Throwable) {
                    stop_loading_animation()
                    t.printStackTrace()
                    try {
                        Shared.getInstance().messageBox(getString(R.string.some_thing_went_wrong), context)
                    } catch (e: java.lang.Exception) {
                    }
                }
            })


        } catch (ex: Exception) {
            ex.printStackTrace()
            stop_loading_animation()
            try {
                Shared.getInstance().messageBox(getString(R.string.some_thing_went_wrong), context)
            } catch (e: java.lang.Exception) {
            }
        }

    }


    companion object {

        fun newInstance(param1: String, param2: String): LoginScreenFragment {
            return LoginScreenFragment()
        }
    }

    override fun onResume() {
        super.onResume()

        if (!pref?.idenediCode.isNullOrEmpty())
            sendIdendiCode()

    }


}
