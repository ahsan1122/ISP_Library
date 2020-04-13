package com.esp.library.exceedersesp.controllers.applications

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.esp.library.R
import com.esp.library.exceedersesp.ESPApplication
import com.esp.library.utilities.common.*
import com.esp.library.exceedersesp.BaseActivity
import com.esp.library.exceedersesp.fragments.applications.AssesscorApplicationDetailFragment
import com.esp.library.exceedersesp.fragments.applications.AssesscorFeedBackDetailFragment
import com.esp.library.exceedersesp.fragments.applications.AssesscorStagesDetailFragment
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_assessor_application_detail.*
import kotlinx.android.synthetic.main.custom_alert_view.view.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import utilities.common.*
import utilities.data.apis.APIs
import utilities.data.applicants.ApplicationSingleton
import utilities.data.applicants.ApplicationsDAO
import utilities.data.applicants.dynamics.DynamicResponseDAO
import java.util.*
import java.util.concurrent.TimeUnit

class AssessorApplicationDetailScreenActivity : BaseActivity() {
    internal var context: BaseActivity? = null
    internal var detail_call: Call<DynamicResponseDAO>? = null
    internal var anim: ProgressBarAnimation? = null
    internal var mApplication: ApplicationsDAO? = null
    internal var ViewPosition = 0
    internal var pref: SharedPreference? = null
    internal var pDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assessor_application_detail)
        initailize()
        setGravity()
        val bnd = intent.extras

        if (bnd != null) {
            mApplication = bnd.getSerializable(ApplicationsDAO.BUNDLE_KEY) as ApplicationsDAO

        }

        try {
            Shared.getInstance().createFolder(Constants.FOLDER_PATH, Constants.FOLDER_NAME, context)
        } catch (e: Exception) {
        }

    }

    private fun initailize() {
        context = this@AssessorApplicationDetailScreenActivity
        pDialog = Shared.getInstance().setProgressDialog(context)
        pref = SharedPreference(context)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowHomeEnabled(false)
        supportActionBar!!.setTitle("")
        toolbar.navigationIcon = ContextCompat.getDrawable(this,R.drawable.ic_nav_back)
        toolbar.navigationIcon?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        toolbar.setNavigationOnClickListener { v -> finish() }

        tabs.setupWithViewPager(viewpager)
    }

    override fun onResume() {

        if (ApplicationSingleton.instace.application != null) {
            UpdateTopView()
            setupViewPager()
        } else {

            if (mApplication != null) {
                GetApplicationDetail(mApplication!!.id.toString() + "")
                UpdateTopView()
            }

        }
        super.onResume()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
    }


    private fun start_loading_animation() {
        appbar.visibility = View.GONE
        viewpager.visibility = View.GONE
        if (!pDialog!!.isShowing)
            pDialog!!.show()
    }

    private fun stop_loading_animation() {
        appbar.visibility = View.VISIBLE
        viewpager.visibility = View.VISIBLE
        if (pDialog!!.isShowing)
            pDialog!!.dismiss()
    }

    fun GetApplicationDetail(id: String) {

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


            //detail_call = apis.GetApplicationDetail(id);
            detail_call = apis.GetApplicationDetailv2(id)
            detail_call!!.enqueue(object : Callback<DynamicResponseDAO> {
                override fun onResponse(call: Call<DynamicResponseDAO>, response: Response<DynamicResponseDAO>?) {

                    stop_loading_animation()

                    if (response != null && response.body() != null) {


                        ApplicationSingleton.instace.application = response.body()

                        setupViewPager()
                    }
                }

                override fun onFailure(call: Call<DynamicResponseDAO>, t: Throwable) {
                    stop_loading_animation()
                    val alertMesasgeWindow = AlertMesasgeWindow.newInstance(pref?.getlabels()?.application, t.message, getString(R.string.alert), getString(R.string.ok))
                    alertMesasgeWindow.show(supportFragmentManager, getString(R.string.alert))
                    alertMesasgeWindow.isCancelable = false
                    return
                }
            })

        } catch (ex: Exception) {
            if (ex != null) {
                stop_loading_animation()
                val alertMesasgeWindow = AlertMesasgeWindow.newInstance(pref?.getlabels()?.application, ex.message, getString(R.string.alert), getString(R.string.ok))
                alertMesasgeWindow.show(supportFragmentManager, getString(R.string.alert))
                alertMesasgeWindow.isCancelable = false
                return
            }
        }

    }

    private fun UpdateTopView() {
        if (mApplication != null) {
            if (mApplication!!.applicantName != null && mApplication!!.applicantName!!.length > 0) {
                applicant_name.text = mApplication!!.applicantName
            } else {
                applicant_name.text = ""
            }

            definitionName.text = mApplication!!.definitionName
        }
    }

    private fun setupViewPager() {

        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(AssesscorApplicationDetailFragment.newInstance(), pref?.getlabels()?.application!!)
        adapter.addFragment(AssesscorStagesDetailFragment.newInstance(), getString(R.string.stages))
        adapter.addFragment(AssesscorFeedBackDetailFragment.newInstance(), getString(R.string.feedback))
        viewpager.adapter = adapter
        viewpager.currentItem = ViewPosition


        viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                Shared.getInstance().errorLogWrite("POSITION", position.toString() + "")
                ViewPosition = position
            }

            override fun onPageSelected(position: Int) {

            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

    }

    internal inner class ViewPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {
        private val mFragmentList = ArrayList<Fragment>()
        private val mFragmentTitleList = ArrayList<String>()

        override fun getItem(position: Int): Fragment {

            return mFragmentList[position]
        }

        override fun getCount(): Int {
            return mFragmentList.size
        }

        fun addFragment(fragment: Fragment, title: String) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence? {

            return mFragmentTitleList[position]
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == 2 && data != null) {

        }
        super.onActivityResult(requestCode, resultCode, data)

    }

    private fun setGravity() {
        if (pref?.language.equals("ar", ignoreCase = true)) {
            definitionName.gravity = Gravity.RIGHT
            applicant_name.gravity = Gravity.RIGHT

        } else {
            definitionName.gravity = Gravity.LEFT
            applicant_name.gravity = Gravity.LEFT
        }
    }

    companion object {

        var ACTIVITY_NAME = "controllers.applications.AssessorApplicationDetailScreenActivity"
    }
}
