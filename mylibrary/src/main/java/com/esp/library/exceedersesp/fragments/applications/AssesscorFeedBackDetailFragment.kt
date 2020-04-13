package com.esp.library.exceedersesp.fragments.applications

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.esp.library.R
import com.esp.library.exceedersesp.ESPApplication
import com.esp.library.utilities.common.*
import com.esp.library.exceedersesp.BaseActivity
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.fragment_assessor_feedback_detail.view.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import utilities.adapters.setup.applications.ListApplicationFeedBackAdapter
import utilities.common.*
import utilities.data.apis.APIs
import utilities.data.applicants.ApplicationSingleton
import utilities.data.applicants.feedback.ApplicationsFeedbackDAO
import java.util.concurrent.TimeUnit

class AssesscorFeedBackDetailFragment : Fragment() {

    internal var context: BaseActivity? = null
    internal var detail_call: Call<List<ApplicationsFeedbackDAO>>? = null
    internal var anim: ProgressBarAnimation? = null
    private var mApplicationAdapter: RecyclerView.Adapter<*>? = null
    internal var pref: SharedPreference? = null
    internal var pDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = activity as BaseActivity?
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_assessor_feedback_detail, container, false)

        initailize(v)

        if (Shared.getInstance().isWifiConnected(context)) {
            if (ApplicationSingleton.instace.application != null) {
                GetApplicationFeedBack(ApplicationSingleton.instace.application!!.applicationId.toString() + "",v)
            }

        } else {
            val alertMesasgeWindow = AlertMesasgeWindow.newInstance(getString(R.string.internet_error_heading), getString(R.string.internet_connection_error), getString(R.string.alert), getString(R.string.ok))
            alertMesasgeWindow.show(fragmentManager!!, getString(R.string.alert))
            alertMesasgeWindow.isCancelable = false

        }


        return v
    }

    private fun initailize(v: View) {
        pDialog = Shared.getInstance().setProgressDialog(context)
        val mApplicationLayoutManager = LinearLayoutManager(context)
        v.app_list.setHasFixedSize(true)
        v.app_list.layoutManager = mApplicationLayoutManager
        v.app_list.itemAnimator = DefaultItemAnimator()
        pref = SharedPreference(context)
    }

    override fun onDestroyView() {

        if (detail_call != null) {
            detail_call!!.cancel()
        }
        super.onDestroyView()
    }

    fun GetApplicationFeedBack(id: String, v: View) {

        start_loading_animation(v)
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


            detail_call = apis.GetApplicationFeedBack(id)
            detail_call!!.enqueue(object : Callback<List<ApplicationsFeedbackDAO>> {
                override fun onResponse(call: Call<List<ApplicationsFeedbackDAO>>, response: Response<List<ApplicationsFeedbackDAO>>?) {

                    stop_loading_animation(v)

                    if (response != null && response.body() != null && response.body().size > 0) {

                        mApplicationAdapter = context?.let { ListApplicationFeedBackAdapter(response.body(), it, "") }
                        v.app_list.adapter = mApplicationAdapter
                        mApplicationAdapter!!.notifyDataSetChanged()

                        v.no_record.visibility = View.GONE
                        v.detail_view.visibility = View.VISIBLE
                    } else {
                        v.no_record.visibility = View.VISIBLE
                        v.detail_view.visibility = View.GONE
                    }
                }

                override fun onFailure(call: Call<List<ApplicationsFeedbackDAO>>, t: Throwable) {
                    stop_loading_animation(v)


                    if (isAdded) {
                        val alertMesasgeWindow = AlertMesasgeWindow.newInstance(pref?.getlabels()?.application, t.message, getString(R.string.alert), getString(R.string.ok))
                        alertMesasgeWindow.show(fragmentManager!!, getString(R.string.alert))
                        alertMesasgeWindow.isCancelable = false
                    }

                }
            })

        } catch (ex: Exception) {

            stop_loading_animation(v)

            if (isAdded) {
                val alertMesasgeWindow = AlertMesasgeWindow.newInstance(pref?.getlabels()?.application, ex.message, getString(R.string.alert), getString(R.string.ok))
                alertMesasgeWindow.show(fragmentManager!!, getString(R.string.alert))
                alertMesasgeWindow.isCancelable = false
            }

        }

    }

    private fun start_loading_animation(v: View) {
        v.detail_view.visibility = View.GONE
        if (!pDialog!!.isShowing)
            pDialog!!.show()
    }

    private fun stop_loading_animation(v: View) {
        v.detail_view.visibility = View.VISIBLE
        if (pDialog!!.isShowing)
            pDialog!!.dismiss()
    }

    companion object {

        fun newInstance(): AssesscorFeedBackDetailFragment {
            val fragment = AssesscorFeedBackDetailFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

}// Required empty public constructor
