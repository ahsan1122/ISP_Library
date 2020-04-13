package com.esp.library.exceedersesp.controllers.Profile

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import com.esp.library.R
import com.esp.library.exceedersesp.ESPApplication
import com.esp.library.utilities.common.Constants
import com.esp.library.utilities.common.Shared
import com.esp.library.exceedersesp.controllers.Profile.adapters.ListofSectionsAdapter
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.section_detail_screen.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import utilities.data.apis.APIs
import utilities.data.applicants.addapplication.CurrencyDAO
import utilities.data.applicants.dynamics.DynamicFormSectionDAO
import utilities.data.applicants.profile.ApplicationProfileDAO

class SectionDetailScreen : AppCompatActivity() {

    internal var TAG = javaClass.simpleName

    internal var ischeckerror: Boolean = false
    internal var context: Context? = null
    internal var dynamicFormSectionDAO: DynamicFormSectionDAO? = null
    internal var adapter: ListofSectionsAdapter? = null
    internal var pDialog: AlertDialog? = null
    var mApplicationLayoutManager: androidx.recyclerview.widget.RecyclerView.LayoutManager?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.section_detail_screen)
        initialize()

        ischeckerror = intent.getBooleanExtra("ischeckerror", false)

        if (Shared.getInstance().isWifiConnected(context)) {
            loadCurrencies()
        } else {
            Shared.getInstance().showAlertMessage(context?.getString(R.string.internet_error_heading), context?.getString(R.string.internet_connection_error), context)
        }

        add_more.setOnClickListener { addCard() }

        txtcancel.setOnClickListener { onBackPressed() }

    }


    private fun initialize() {
        context = this@SectionDetailScreen
        pDialog = Shared.getInstance().setProgressDialog(context)
        mApplicationLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        app_list.setHasFixedSize(true)
        app_list.layoutManager = mApplicationLayoutManager
        app_list.itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()
        dynamicFormSectionDAO = intent.getSerializableExtra("data") as DynamicFormSectionDAO
        txtheader.text = dynamicFormSectionDAO?.defaultName

        if (dynamicFormSectionDAO!!.isMultipule)
            lladdmore.visibility = View.VISIBLE
        else
            lladdmore.visibility = View.GONE

    }

    private fun getSections() {
        val dataapplicant = intent.getSerializableExtra("dataapplicant") as ApplicationProfileDAO
        val sectionsFields = dynamicFormSectionDAO!!.fields

        adapter = dynamicFormSectionDAO?.let { context?.let { it1 -> sectionsFields?.let { it2 ->
            ListofSectionsAdapter(it, dataapplicant, it2, it1, ischeckerror) } } }
        app_list.adapter = adapter
    }

    private fun addCard() {
        val dataapplicant = intent.getSerializableExtra("dataapplicant") as ApplicationProfileDAO
        val i = Intent(context, EditSectionDetails::class.java)
        i.putExtra("data", dynamicFormSectionDAO)
        i.putExtra("dataapplicant", dataapplicant)
        i.putExtra("ischeckerror", ischeckerror)
        i.putExtra("isprofile", intent.getBooleanExtra("isprofile", false))
        i.putExtra("isaddmore", true)
        startActivity(i)
    }

    fun loadCurrencies() {
        start_loading_animation()
        try {
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY
            val httpClient = OkHttpClient.Builder()
            httpClient.addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                        .header("locale", Shared.getInstance().getLanguageSimpleContext(context))
                        .header("Authorization", "bearer " + ESPApplication.getInstance().user.loginResponse?.access_token)

                val request = requestBuilder.build()
                chain.proceed(request)
            }
            if (Constants.WRITE_LOG) {
                httpClient.addInterceptor(logging)
            }

            /*Gson object for custom field types*/
            val gson = GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                    .create()

            /* retrofit builder and call web service*/
            val retrofit = Retrofit.Builder()
                    .baseUrl(Constants.base_url)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(httpClient.build())
                    .build()

            /* APIs Mapping respective Object*/
            val apis = retrofit.create(APIs::class.java)

            val call = apis.getCurrency()

            call.enqueue(object : Callback<List<CurrencyDAO>> {
                override fun onResponse(call: Call<List<CurrencyDAO>>, response: Response<List<CurrencyDAO>>) {

                    if (response.body() != null && response.body().size > 0) {
                        ESPApplication.getInstance().currencies = response.body()
                    }
                    stop_loading_animation()
                    getSections()
                }

                override fun onFailure(call: Call<List<CurrencyDAO>>, t: Throwable) {
                    stop_loading_animation()
                }
            })

        } catch (ex: Exception) {
            ex.printStackTrace()
            stop_loading_animation()
        }

    }

    private fun start_loading_animation() {
        if (!pDialog!!.isShowing)
            pDialog!!.show()
    }

    private fun stop_loading_animation() {
        if (pDialog!!.isShowing)
            pDialog!!.dismiss()
    }
}
