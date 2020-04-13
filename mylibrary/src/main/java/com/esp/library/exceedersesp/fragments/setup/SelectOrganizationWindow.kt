package com.esp.library.exceedersesp.fragments.setup

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.esp.library.R
import com.esp.library.utilities.common.Constants
import com.esp.library.utilities.common.Shared
import com.esp.library.utilities.common.SharedPreference
import com.esp.library.exceedersesp.BaseActivity
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.alert_select_organization_window.view.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import utilities.adapters.setup.ListofOrganizationSectionsAdapter
import utilities.data.apis.APIs
import utilities.data.setup.OrganizationPersonaDao
import utilities.data.setup.TokenDAO
import java.util.*

//
class SelectOrganizationWindow : androidx.fragment.app.DialogFragment() {

    internal var TAG = javaClass.simpleName
    internal var personas: TokenDAO? = null
    private var mOrganizeAdapter: androidx.recyclerview.widget.RecyclerView.Adapter<*>? = null
    private var mOrgLayoutManager: androidx.recyclerview.widget.RecyclerView.LayoutManager? = null
    internal var context: BaseActivity? = null
    internal var personaDAOList: MutableList<OrganizationPersonaDao.Personas> = ArrayList()
    internal var pDialog: AlertDialog? = null
    internal var pref: SharedPreference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = activity as BaseActivity?
        if (arguments != null) {
            personas = arguments!!.getSerializable(TokenDAO.BUNDLE_KEY) as TokenDAO
        }


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.alert_select_organization_window, container, false)
        initailize(v)
        if (personas != null)
            v.txtemail.text = personas!!.email

        if (Shared.getInstance().isWifiConnected(context)) {
            getOrganizations(v)
        } else {
            Shared.getInstance().showAlertMessage(context?.getString(R.string.internet_error_heading), context?.getString(R.string.internet_connection_error), context)
        }
        return v
    }

    private fun initailize(v: View) {
        mOrgLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
        v.org_list.setHasFixedSize(true)
        v.org_list.layoutManager = mOrgLayoutManager
        v.org_list.itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()
        dialog?.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        pref = SharedPreference(context)
        pDialog = Shared.getInstance().setProgressDialog(context)
        v.close.setOnClickListener { view -> dismiss() }
    }


    private fun getOrganizations(v: View) {
        start_loading_animation(v)

        try {
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY
            val httpClient = OkHttpClient.Builder()
            httpClient.addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                        .header("locale", Shared.getInstance().getLanguageSimpleContext(context))
                        .header("Authorization", "bearer " + personas!!.access_token)
                //
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


            val apis = retrofit.create(APIs::class.java)
            val organization_call = apis.organizations

            organization_call.enqueue(object : Callback<List<OrganizationPersonaDao>> {
                override fun onResponse(call: Call<List<OrganizationPersonaDao>>, response: Response<List<OrganizationPersonaDao>>) {
                    val body = response.body()

                    if (body != null) {


                        for (i in body.indices) {
                            val organizationPersonaDao = body[i]
                            val persoans = organizationPersonaDao.persoans
                            for (j in persoans.indices) {
                                val personas = persoans[j]
                                personaDAOList.add(personas)
                            }
                        }

                        mOrganizeAdapter = context?.let { personas?.let { it1 -> ListofOrganizationSectionsAdapter(body, it, it1) } }
                        v.org_list.adapter = mOrganizeAdapter
                        mOrganizeAdapter!!.notifyDataSetChanged()
                    }
                    stop_loading_animation(v)
                }

                override fun onFailure(call: Call<List<OrganizationPersonaDao>>, t: Throwable) {
                    stop_loading_animation(v)
                    Shared.getInstance().showAlertMessage(getString(R.string.error), getString(R.string.some_thing_went_wrong), context)
                }
            })


        } catch (ex: Exception) {

            ex.printStackTrace()
            stop_loading_animation(v)
            Shared.getInstance().showAlertMessage(getString(R.string.error), getString(R.string.some_thing_went_wrong), context)


        }

    }

    private fun start_loading_animation(v: View) {
        v.rlmainlayout.visibility = View.GONE
        if (!pDialog!!.isShowing)
            pDialog!!.show()
    }

    private fun stop_loading_animation(v: View) {
        v.rlmainlayout.visibility = View.VISIBLE
        if (pDialog!!.isShowing)
            pDialog!!.dismiss()
    }

    companion object {

        fun newInstance(persona: TokenDAO): SelectOrganizationWindow {
            val fragment = SelectOrganizationWindow()
            val args = Bundle()
            args.putSerializable(TokenDAO.BUNDLE_KEY, persona)
            fragment.arguments = args
            return fragment
        }
    }

}
