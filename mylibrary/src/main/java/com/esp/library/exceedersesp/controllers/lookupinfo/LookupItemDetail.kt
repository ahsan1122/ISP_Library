package com.esp.library.exceedersesp.controllers.lookupinfo

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.text.TextUtils
import com.esp.library.R
import com.esp.library.exceedersesp.ESPApplication
import com.esp.library.utilities.common.Constants
import com.esp.library.utilities.common.Shared
import com.esp.library.utilities.common.SharedPreference
import com.esp.library.exceedersesp.BaseActivity
import com.esp.library.exceedersesp.controllers.lookupinfo.adapter.ListLookupInfoItemsDetailAdapter
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.lookup_item_detail.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import utilities.data.apis.APIs
import utilities.data.applicants.dynamics.DyanmicFormSectionFieldDetailsDAO
import utilities.data.applicants.dynamics.DynamicFormSectionFieldDAO
import utilities.data.applicants.dynamics.DynamicFormSectionFieldLookupValuesDAO
import utilities.data.lookup.LookupItemDetailDAO
import java.util.*
import java.util.concurrent.TimeUnit

class LookupItemDetail : BaseActivity() {
    internal var context: BaseActivity? = null
    internal var TAG = javaClass.simpleName
    private var lookupItemDetailLayoutManager: androidx.recyclerview.widget.RecyclerView.LayoutManager? = null
    internal var pDialog: AlertDialog? = null
    internal var pref: SharedPreference? = null

    internal var lookupInfoDetailItemArray = ArrayList<DynamicFormSectionFieldDAO>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lookup_item_detail)
        initialize()
        getData()

        swipeRefreshLayout!!.setOnRefreshListener { getData() }

     /*   lookup_item_detail_list?.addOnScrollListener(object : EndlessRecyclerViewScrollListener(lookupItemDetailLayoutManager as LinearLayoutManager?) {
            override fun onHide() {
                Shared.getInstance().setToolbarHeight(toolbar, false)
            }

            override fun onShow() {
                Shared.getInstance().setToolbarHeight(toolbar, true)
            }

            override fun getFooterViewType(defaultNoFooterViewType: Int): Int {
                return defaultNoFooterViewType
            }

            override fun onLoadMore(page: Int, totalItemsCount: Int) {


            }

        })*/


    }

    private fun initialize() {
        context = this@LookupItemDetail
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(false)
        supportActionBar?.title = ""
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_nav_back)
        toolbar.navigationIcon?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        toolbar.setNavigationOnClickListener { v -> onBackPressed() }
        toolbar_heading.text = intent.getStringExtra("toolbar_heading")

        pDialog = Shared.getInstance().setProgressDialog(context)
        pref = SharedPreference(context)

        lookupItemDetailLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        lookup_item_detail_list.setHasFixedSize(true)
        lookup_item_detail_list.layoutManager = lookupItemDetailLayoutManager
        lookup_item_detail_list.itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()

    }

    private fun getData() {
        if (Shared.getInstance().isWifiConnected(context)) {
            getLookupItemDetail()
        } else {
            Shared.getInstance().showAlertMessage(getString(R.string.internet_error_heading), getString(R.string.internet_connection_error), bContext)
        }
    }

    fun getLookupItemDetail() {
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


            val detail_call = apis.getLookupItemDetail(intent.getIntExtra("id", 0))
            detail_call.enqueue(object : Callback<LookupItemDetailDAO> {
                override fun onResponse(call: Call<LookupItemDetailDAO>, response: Response<LookupItemDetailDAO>) {
                    lookupInfoDetailItemArray.clear()
                    stop_loading_animation()
                    var value: String
                    val body = response.body()

                    if (body != null) {
                        var sections = body.lookup.form.sections

                        for (i in sections.indices) {
                            val dynamicFormSectionDAO = sections[i]
                            val fields = dynamicFormSectionDAO.fields
                            fields?.let { lookupInfoDetailItemArray.addAll(it) }
                            for (j in fields!!.indices) {
                                val dynamicFormSectionFieldDAO = fields?.get(j)

                                val sectionCustomFieldId = dynamicFormSectionFieldDAO.sectionCustomFieldId
                                val label = dynamicFormSectionFieldDAO.label
                                value = dynamicFormSectionFieldDAO.value!!
                                val lookupValues = dynamicFormSectionFieldDAO.lookupValues

                                val values = body.values

                                for (k in values.indices) {
                                    val dynamicFormValuesDAO = values[k]

                                    val sectionCustomValueFieldId = dynamicFormValuesDAO.sectionCustomFieldId
                                    if (sectionCustomValueFieldId == sectionCustomFieldId) {

                                        val customFieldLookupId = dynamicFormValuesDAO.customFieldLookupId
                                        val type = dynamicFormValuesDAO.type

                                        if (customFieldLookupId < 0 && type == 13) {
                                         if(dynamicFormValuesDAO.selectedLookupText != null)
                                            value = dynamicFormValuesDAO.selectedLookupText!!
                                        }
                                        else
                                            value = dynamicFormValuesDAO.value!!

                                        if (type == 5) {
                                            value = getSingleSelectionValue(value, lookupValues!!)
                                        } else if (type == 6) {
                                            value = getMultiSectionText(value, lookupValues!!)
                                        } else if (type == 7) {
                                            val details = dynamicFormValuesDAO.details
                                            if (details != null) {
                                                val fieldDetails = DyanmicFormSectionFieldDetailsDAO()
                                                fieldDetails.name = details.name
                                                fieldDetails.mimeType = details.mimeType
                                                fieldDetails.createdOn = details.createdOn
                                                fieldDetails.downloadUrl = details.downloadUrl
                                                dynamicFormSectionFieldDAO.details = fieldDetails
                                            }
                                        } else if (type == 13) {
                                            dynamicFormValuesDAO.selectedLookupText?.let {
                                                value = it
                                            }
                                        }
                                        dynamicFormSectionFieldDAO.value = value
                                        lookupInfoDetailItemArray[j] = dynamicFormSectionFieldDAO

                                    }
                                }
                            }
                        }

                        val dynamicFormSectionFieldDAOS = invisibleList(lookupInfoDetailItemArray)
                        val adapter = ListLookupInfoItemsDetailAdapter(dynamicFormSectionFieldDAOS, context!!)
                        lookup_item_detail_list.adapter = adapter
                    } else {
                        stop_loading_animation()
                        Shared.getInstance().showAlertMessage(pref?.getlabels()?.application, getString(R.string.some_thing_went_wrong), context)
                    }

                }

                override fun onFailure(call: Call<LookupItemDetailDAO>, t: Throwable) {
                    stop_loading_animation()
                    Shared.getInstance().showAlertMessage(pref?.getlabels()?.application, getString(R.string.some_thing_went_wrong), context)
                    return
                }
            })

        } catch (ex: Exception) {
            ex.printStackTrace()
            stop_loading_animation()
            Shared.getInstance().showAlertMessage(pref?.getlabels()?.application, getString(R.string.some_thing_went_wrong), context)


        }

    }

    private fun invisibleList(fieldsList: ArrayList<DynamicFormSectionFieldDAO>): List<DynamicFormSectionFieldDAO> {
        val tempFields = ArrayList<DynamicFormSectionFieldDAO>()
        for (h in fieldsList.indices) {
            if (fieldsList[h].isVisible && !fieldsList[h].label.equals(ESPApplication.getInstance().user.loginResponse?.role?.toLowerCase(), ignoreCase = true)) {
                tempFields.add(fieldsList[h])
            }
        }
        return tempFields
    }

    private fun getSingleSelectionValue(value: String, lookupValues: List<DynamicFormSectionFieldLookupValuesDAO>): String {
        var value = value
        val digitsOnly = TextUtils.isDigitsOnly(value)
        if (digitsOnly) {
            for (h in lookupValues.indices) {
                val dynamicFormSectionFieldLookupValuesDAO = lookupValues[h]
                try {
                    if (dynamicFormSectionFieldLookupValuesDAO.id == Integer.parseInt(value))
                        value = dynamicFormSectionFieldLookupValuesDAO.label!!
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }
        return value
    }

    private fun getMultiSectionText(value: String, lookupValues: List<DynamicFormSectionFieldLookupValuesDAO>): String {
        val sb = StringBuilder()
        val split = value.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (m in split.indices) {
            for (h in lookupValues.indices) {
                val dynamicFormSectionFieldLookupValuesDAO = lookupValues[h]
                try {
                    if (dynamicFormSectionFieldLookupValuesDAO.id == Integer.parseInt(split[m])) {
                        val label1 = dynamicFormSectionFieldLookupValuesDAO.label

                        val getsb = getJSONObjectValue(label1!!, split, m)

                        if (getsb.replace("\\s".toRegex(), "").length == 0) {
                            sb.append(label1)
                            if (split.size - 1 != m)
                                sb.append(", ")
                        }

                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }
        return sb.toString()
    }

    private fun getJSONObjectValue(label: String, split: Array<String>, m: Int): String {
        val sb = StringBuilder()
        if (Shared.getInstance().isJSONValid(label)) {
            try {
                val jsonObject = JSONObject(label)
                if (jsonObject.has(pref?.language)) {
                    val text = jsonObject.getString(pref?.language)
                    sb.append(text)
                    if (split.size - 1 != m)
                        sb.append(",")
                }

            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }
        return sb.toString()
    }

    private fun start_loading_animation() {
        swipeRefreshLayout!!.isRefreshing = true
        if (!pDialog!!.isShowing)
            pDialog!!.show()
    }

    private fun stop_loading_animation() {
        swipeRefreshLayout!!.isRefreshing = false
        if (pDialog!!.isShowing)
            pDialog!!.dismiss()
    }

}
