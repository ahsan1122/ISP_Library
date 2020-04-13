package com.esp.library.exceedersesp.controllers.applications

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.esp.library.R
import com.esp.library.exceedersesp.ESPApplication
import com.esp.library.utilities.common.*
import com.esp.library.exceedersesp.BaseActivity
import com.esp.library.exceedersesp.fragments.applications.AssesscorApplicationStagesCriteriaCommentsFragment
import com.google.gson.GsonBuilder
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_assessor_application_stages_criteria_comments.*
import kotlinx.android.synthetic.main.add_comments_view.*
import kotlinx.android.synthetic.main.repeater_application_stages_criteria_comments.user_img
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import utilities.adapters.setup.applications.ListApplicationStageCriteriaCommentsAdapter
import utilities.common.*
import utilities.data.apis.APIs
import utilities.data.applicants.ApplicationSingleton
import utilities.data.applicants.addapplication.PostApplicationsCriteriaCommentsDAO
import utilities.data.applicants.dynamics.DynamicResponseDAO
import utilities.data.applicants.dynamics.DynamicStagesCriteriaCommentsListDAO
import utilities.data.applicants.dynamics.DynamicStagesCriteriaListDAO
import java.util.concurrent.TimeUnit

class AssessorApplicationStagesCeriteriaCommentsScreenActivity : BaseActivity(), ListApplicationStageCriteriaCommentsAdapter.CriteriaStatusChange {
    internal var context: BaseActivity? = null
    internal var detail_call: Call<DynamicResponseDAO>? = null
    internal var status_call: Call<Int>? = null
    internal var anim: ProgressBarAnimation? = null
    internal var mCriteria: DynamicStagesCriteriaListDAO? = null
    internal var fm: androidx.fragment.app.FragmentManager? = null
    internal var submit_request: AssesscorApplicationStagesCriteriaCommentsFragment? = null
    internal var imm: InputMethodManager? = null
    internal var pref: SharedPreference? = null
    internal var pDialog: android.app.AlertDialog? = null
    override fun StatusChange(update: DynamicStagesCriteriaCommentsListDAO) {
        AddCriterComments(update, mCriteria)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assessor_application_stages_criteria_comments)
        initialize()
        setGravity()
        val bnd = intent.extras
        if (bnd != null) {
            mCriteria = bnd.getSerializable(DynamicStagesCriteriaListDAO.BUNDLE_KEY) as DynamicStagesCriteriaListDAO
            if (mCriteria != null) {
                UpdateTopView()
            }
        }
        try {
            Shared.getInstance().createFolder(Constants.FOLDER_PATH, Constants.FOLDER_NAME, context)
        } catch (e: Exception) {
        }


        fm = this.context!!.getSupportFragmentManager()
        submit_request = mCriteria?.let { AssesscorApplicationStagesCriteriaCommentsFragment.newInstance(it) }
        val ft = fm!!.beginTransaction()
        ft.add(R.id.request_fragment, submit_request!!)
        ft.commit()

        if (user_img != null && ESPApplication.getInstance().user != null && ESPApplication.getInstance().user.loginResponse?.imageUrl != null && ESPApplication.getInstance().user.loginResponse?.imageUrl!!.length > 0) {
            Picasso.with(context)
                    .load(ESPApplication.getInstance().user.loginResponse?.imageUrl)
                    .placeholder(R.drawable.ic_contact_default)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .transform(RoundedPicasso())
                    .resize(30, 30)
                    .into(user_img)


        }

        if (add_comments != null) {

            add_comments.setOnClickListener { AddCriterComments(null, mCriteria) }

        }

    }

    private fun initialize() {
        context = this@AssessorApplicationStagesCeriteriaCommentsScreenActivity
        setSupportActionBar(toolbar)
        pref = SharedPreference(context!!)
        pDialog = Shared.getInstance().setProgressDialog(context)
        supportActionBar!!.setDisplayShowHomeEnabled(false)
        supportActionBar!!.setTitle("")
        toolbar.navigationIcon = ContextCompat.getDrawable(this,R.drawable.ic_nav_back)
        toolbar.navigationIcon?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        toolbar.setNavigationOnClickListener { finish() }

    }


    private fun start_loading_animation() {
        appbar.visibility = View.GONE
        if (!pDialog!!.isShowing)
            pDialog!!.show()
    }

    private fun stop_loading_animation() {
        appbar.visibility = View.VISIBLE
        if (pDialog!!.isShowing)
            pDialog!!.dismiss()
    }

    fun GetApplicationDetail() {

        start_loading_animation()
        try {

            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY
            val httpClient = OkHttpClient.Builder()
            httpClient.addInterceptor(logging)

            httpClient.addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                        .header("locale", Shared.getInstance().getLanguage(context))
                        .header("Authorization", "bearer " + ESPApplication.getInstance().user.loginResponse?.access_token)
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

            if (ApplicationSingleton.instace.application != null) {
                detail_call = apis.GetApplicationDetailv2(ApplicationSingleton.instace.application!!.applicationId.toString() + "")
            }


            detail_call!!.enqueue(object : Callback<DynamicResponseDAO> {
                override fun onResponse(call: Call<DynamicResponseDAO>, response: Response<DynamicResponseDAO>?) {

                    stop_loading_animation()

                    if (response != null && response.body() != null) {

                        if (ApplicationSingleton.instace.application != null) {
                            ApplicationSingleton.instace.application = null
                        }
                        ApplicationSingleton.instace.application = response.body()

                        if (submit_request != null) {
                            submit_request!!.UpdateCriteriaCommentsList(submit_request!!.view)
                        }

                    }
                }

                override fun onFailure(call: Call<DynamicResponseDAO>, t: Throwable) {
                    stop_loading_animation();
                    Shared.getInstance().showAlertMessage(pref?.getlabels()?.application, getString(R.string.some_thing_went_wrong), context)
                }
            })

        } catch (ex: Exception) {
            stop_loading_animation()
            ex.printStackTrace()
            Shared.getInstance().showAlertMessage(pref?.getlabels()?.application, getString(R.string.some_thing_went_wrong), context)

        }

    }

    fun AddEditComments(post: PostApplicationsCriteriaCommentsDAO) {

        val view = currentFocus
        if (view != null) {
            imm!!.hideSoftInputFromWindow(view.windowToken, 0)
        }

        start_loading_animation()
        try {

            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY
            val httpClient = OkHttpClient.Builder()
            httpClient.addInterceptor(logging)

            httpClient.addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                        .header("locale", Shared.getInstance().getLanguage(context))
                        .header("Authorization", "bearer " + ESPApplication.getInstance().user.loginResponse?.access_token)
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

            val UserComments = RequestBody.create(MediaType.parse("text/plain"), post.comments)

            if (post.id > 0) {
                status_call = apis.EditComments(post.id, post.assessmentId, UserComments)
            } else {
                status_call = apis.addComments(post.assessmentId, UserComments)
            }



            status_call!!.enqueue(object : Callback<Int> {
                override fun onResponse(call: Call<Int>, response: Response<Int>?) {


                    if (response != null && response.body() != null && response.body() > 0) {
                        Constants.isApplicationChagned = true
                        GetApplicationDetail()
                    } else
                        stop_loading_animation()
                }

                override fun onFailure(call: Call<Int>, t: Throwable?) {
                    stop_loading_animation()
                    if (t != null && context != null) {
                        Shared.getInstance().showAlertMessage(pref?.getlabels()?.application, getString(R.string.some_thing_went_wrong), context)
                    }

                }
            })

        } catch (ex: Exception) {
            if (ex != null) {
                stop_loading_animation()
                if (ex != null && context != null) {
                    Shared.getInstance().showAlertMessage(pref?.getlabels()?.application, getString(R.string.some_thing_went_wrong), context)
                }
            }
        }

    }

    private fun UpdateTopView() {
        if (mCriteria != null) {
            category.text = mCriteria!!.name
            definitionName.setText(R.string.criteria_feedback)
        }
    }

    fun AddCriterComments(post: DynamicStagesCriteriaCommentsListDAO?, criteria: DynamicStagesCriteriaListDAO?) {

        val builder = AlertDialog.Builder(context!!)

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_layout, null)
        val input = view.findViewById<EditText>(R.id.reason)
        input.setHint(R.string.please_add_comment)
        builder.setView(view)

        if (post != null) {
            builder.setTitle(R.string.edit_comment)

            if (post.comment != null && post.comment!!.length > 0) {
                input.setText(post.comment)
            }

        } else {
            builder.setTitle(R.string.add_comment)
        }



        builder.setPositiveButton(R.string.save) { dialog, which ->
            val m_Text = input.text.toString()

            if (m_Text != null && m_Text.length > 0) {

                val post_comments = PostApplicationsCriteriaCommentsDAO()
                post_comments.assessmentId = criteria!!.assessmentId
                post_comments.comments = m_Text

                if (post != null) {
                    post_comments.id = post.id
                }

                imm!!.hideSoftInputFromWindow(input.windowToken, 0)

                AddEditComments(post_comments)

                dialog.cancel()


            }

            imm!!.hideSoftInputFromWindow(input.windowToken, 0)
            dialog.cancel()
        }



        builder.setNegativeButton(R.string.cancel) { dialog, which ->
            imm!!.hideSoftInputFromWindow(input.windowToken, 0)
            dialog.cancel()
        }

        val dialog = builder.create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(context!!.getResources().getColor(R.color.black))


        if (post != null && post.comment != null && post.comment!!.length > 0) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
        } else {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
        }


        input.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int,
                                       count: Int) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int,
                                           after: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                // Check if edittext is empty
                if (TextUtils.isEmpty(s)) {
                    //Disable ok button
                    dialog.getButton(
                            AlertDialog.BUTTON_POSITIVE).isEnabled = false
                } else {
                    // Something into edit text. Enable the button.
                    dialog.getButton(
                            AlertDialog.BUTTON_POSITIVE).isEnabled = true
                }

            }
        })
    }

    private fun setGravity() {
        if (pref?.language.equals("ar", ignoreCase = true)) {
            definitionName.gravity = Gravity.RIGHT
            category.gravity = Gravity.RIGHT

        } else {
            definitionName.gravity = Gravity.LEFT
            category.gravity = Gravity.LEFT
        }
    }

    companion object {

        var ACTIVITY_NAME = "controllers.applications.AssessorApplicationStagesCeriteriaCommentsScreenActivity"
    }

}
