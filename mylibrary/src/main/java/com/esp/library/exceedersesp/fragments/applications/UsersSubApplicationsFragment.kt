package com.esp.library.exceedersesp.fragments.applications

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.esp.library.R
import com.esp.library.exceedersesp.ESPApplication
import com.esp.library.utilities.common.*
import com.esp.library.exceedersesp.BaseActivity
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.fragment_subusers_applications.view.*
import kotlinx.android.synthetic.main.fragment_subusers_applications.view.app_list
import kotlinx.android.synthetic.main.fragment_subusers_applications.view.load_more_div
import kotlinx.android.synthetic.main.fragment_subusers_applications.view.swipeRefreshLayout
import kotlinx.android.synthetic.main.fragment_users_applications.view.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import utilities.adapters.setup.applications.ListUsersApplicationsAdapter
import utilities.common.*
import utilities.data.apis.APIs
import utilities.data.applicants.ApplicationsDAO
import utilities.data.applicants.ResponseApplicationsDAO
import utilities.data.applicants.dynamics.DynamicResponseDAO
import utilities.data.filters.FilterDAO
import java.util.*
import java.util.concurrent.TimeUnit

class UsersSubApplicationsFragment : Fragment() {

    internal var TAG = javaClass.simpleName

    internal var context: BaseActivity? = null
    private var mApplicationAdapter: RecyclerView.Adapter<*>? = null
    private var mApplicationLayoutManager: RecyclerView.LayoutManager? = null
    internal var retrofit: Retrofit? = null
    internal var call: Call<ResponseApplicationsDAO>? = null
    internal var profile_call: Call<String>? = null
    internal var app_actual_list: MutableList<ApplicationsDAO>? = null
    internal var imm: InputMethodManager? = null
    internal var anim: ProgressBarAnimation? = null

    internal var PAGE_NO = 1
    internal var PER_PAGE_RECORDS = 10
    internal var IN_LIST_RECORDS = 0
    internal var SCROLL_TO = 0
    internal var TOTAL_RECORDS_AVAILABLE = 0
    internal var mHSListener: HideShowPlus? = null
    internal var pref: SharedPreference? = null

    internal var pDialog: AlertDialog? = null
    internal var toolbar: Toolbar? = null

    interface HideShowPlus {
        fun mAction(IsShown: Boolean)
    }

    private fun AddScroller() {

        view?.app_list?.addOnScrollListener(object : EndlessRecyclerViewScrollListener(mApplicationLayoutManager as LinearLayoutManager?) {
            override fun onHide() {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onShow() {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun getFooterViewType(defaultNoFooterViewType: Int): Int {
                return defaultNoFooterViewType
            }

            override fun onLoadMore(page: Int, totalItemsCount: Int) {

                if (IN_LIST_RECORDS < TOTAL_RECORDS_AVAILABLE) {
                    LoadApplications(true)
                }


            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = activity as BaseActivity?
        pref = SharedPreference(context)


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_subusers_applications, container, false)
        initailize(v)
        v.app_list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                imm!!.hideSoftInputFromWindow(view!!.windowToken, 0)
            }
        })


        /*v.ivbackarrow.setOnClickListener {
            activity?.onBackPressed()
        }*/

        return v
    }

    private fun initailize(v: View) {


        pDialog = Shared.getInstance().setProgressDialog(context)
        mApplicationLayoutManager = LinearLayoutManager(activity)
        v.app_list.setHasFixedSize(true)
        v.app_list.layoutManager = mApplicationLayoutManager
        v.app_list.itemAnimator = DefaultItemAnimator()
        imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        v.txtnoapplicationadded?.text = context?.getString(R.string.no) + " " + pref?.getlabels()?.application + " " + context?.getString(R.string.added)
        v.txtnoapplicationadded?.text = context?.getString(R.string.startsubmittingapp) + " " + pref?.getlabels()?.application + " " + context?.getString(R.string.itseasy)

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        try {
            mHSListener = context as HideShowPlus
        } catch (e: Exception) {

        }

        if (Shared.getInstance().isWifiConnected(context)) {

            LoadApplications(false)
        } else {
            Shared.getInstance().showAlertMessage(context?.getString(R.string.internet_error_heading), context?.getString(R.string.internet_connection_error), context)
        }

        view?.swipeRefreshLayout?.setOnRefreshListener {
            if (Shared.getInstance().isWifiConnected(context)) {

                PAGE_NO = 1
                PER_PAGE_RECORDS = 10
                IN_LIST_RECORDS = 0
                TOTAL_RECORDS_AVAILABLE = 0

                LoadApplications(false)
            } else {
                Shared.getInstance().showAlertMessage(context?.getString(R.string.internet_error_heading), context?.getString(R.string.internet_connection_error), context)
                view?.swipeRefreshLayout?.isRefreshing = false
            }
        }

        if (!checkPermission()) {
            requestPermission()
        }


    }


    fun ReLoadApplications() {
        if (Shared.getInstance().isWifiConnected(context)) {

            PAGE_NO = 1
            PER_PAGE_RECORDS = 10
            IN_LIST_RECORDS = 0
            TOTAL_RECORDS_AVAILABLE = 0
            LoadApplications(false)
        } else {
            Shared.getInstance().showAlertMessage(context?.getString(R.string.internet_error_heading), context?.getString(R.string.internet_connection_error), context)
            view?.swipeRefreshLayout?.isRefreshing = false
        }
    }

    fun LoadApplications(isLoadMore: Boolean) {

        if (isLoadMore) {
            view?.load_more_div?.visibility = View.VISIBLE
        } else {
            start_loading_animation()
        }


        try {

            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY
            val httpClient = OkHttpClient.Builder()


            httpClient.addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                        .header("Authorization", "bearer " + ESPApplication.getInstance().user.loginResponse?.access_token)
                        .header("locale", Shared.getInstance().getLanguage(context))
                val request = requestBuilder.build()
                chain.proceed(request)
            }

            if (Constants.WRITE_LOG) {
                httpClient.addInterceptor(logging)
            }

            httpClient.connectTimeout(2, TimeUnit.MINUTES)
            httpClient.readTimeout(2, TimeUnit.MINUTES)
            httpClient.writeTimeout(2, TimeUnit.MINUTES)

            /*Gson object for custom field types*/
            val gson = GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                    .create()

            /* retrofit builder and call web service*/
            retrofit = Retrofit.Builder()
                    .baseUrl(Constants.base_url)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(httpClient.build())
                    .build()

            /* APIs Mapping respective Object*/
            val apis = retrofit!!.create(APIs::class.java)

            if (isLoadMore) {

            } else {
                PAGE_NO = 1
                PER_PAGE_RECORDS = 10
                IN_LIST_RECORDS = 0
                TOTAL_RECORDS_AVAILABLE = 0
            }

            ESPApplication.getInstance().filter.pageNo = PAGE_NO
            ESPApplication.getInstance().filter.recordPerPage = PER_PAGE_RECORDS


            var dao: FilterDAO? = null
            if (ESPApplication.getInstance().filter.statuses?.size == 4) {
                dao = Shared.getInstance().CloneFilter(ESPApplication.getInstance().filter)
                dao!!.statuses = null
                val empty_fitler = ArrayList<String>()
                empty_fitler.add("0")

            } else {
                dao = Shared.getInstance().CloneFilter(ESPApplication.getInstance().filter)
            }


            var getDynamicStagesDAO = arguments?.getSerializable("dynamicResponseDAO") as DynamicResponseDAO
            dao.parentApplicationId = getDynamicStagesDAO.applicationId?.toString()
            // call = apis.GetUserSubApplicationsList("", 0, PAGE_NO, PER_PAGE_RECORDS, false, 1, "", getDynamicStagesDAO.linkDefinitionId);
            call = apis.getUserApplicationsV3(dao)



            call!!.enqueue(object : Callback<ResponseApplicationsDAO> {
                override fun onResponse(call: Call<ResponseApplicationsDAO>, response: Response<ResponseApplicationsDAO>?) {

                    if (isLoadMore) {
                        view?.load_more_div?.visibility = View.GONE
                    }

                    if (response != null && response.body() != null && response.body().totalRecords > 0) {
                        if (response.body().applications != null && response.body().applications!!.size > 0) {


                            if (isLoadMore) {
                                if (app_actual_list == null) {
                                    app_actual_list = response.body().applications as MutableList<ApplicationsDAO>?
                                } else if (app_actual_list != null && app_actual_list!!.size > 0) {
                                    app_actual_list!!.addAll(response.body().applications!!)
                                }

                                PAGE_NO++
                                IN_LIST_RECORDS = app_actual_list!!.size
                                TOTAL_RECORDS_AVAILABLE = response.body().totalRecords
                                SCROLL_TO += PER_PAGE_RECORDS

                                mApplicationAdapter = context?.let { ListUsersApplicationsAdapter(app_actual_list, it, "", true) }
                                view?.app_list?.adapter = mApplicationAdapter
                                mApplicationAdapter!!.notifyDataSetChanged()
                                view?.app_list?.scrollToPosition(SCROLL_TO - 5)

                            } else {
                                app_actual_list = response.body().applications as MutableList<ApplicationsDAO>?
                                mApplicationAdapter = context?.let { ListUsersApplicationsAdapter(app_actual_list, it, "", true) }
                                view?.app_list?.adapter = mApplicationAdapter
                                PAGE_NO++
                                IN_LIST_RECORDS = app_actual_list!!.size
                                TOTAL_RECORDS_AVAILABLE = response.body().totalRecords
                                SCROLL_TO = 0
                                AddScroller()
                            }

                            SuccessResponse()

                            if (!isLoadMore) {
                                GetProfileStatus()
                            } else
                                stop_loading_animation()

                        } else {
                            if (!isLoadMore) {
                                GetProfileStatus()
                            } else
                                stop_loading_animation()
                            UnSuccessResponse()
                        }
                    } else {
                        if (!isLoadMore) {
                            GetProfileStatus()
                        } else
                            stop_loading_animation()
                        UnSuccessResponse()
                    }


                }


                override fun onFailure(call: Call<ResponseApplicationsDAO>, t: Throwable) {
                    Shared.getInstance().messageBox(t.message, context)
                    stop_loading_animation()
                    UnSuccessResponse()
                    if (!isLoadMore) {
                        GetProfileStatus()
                    }

                }
            })

        } catch (ex: Exception) {

            Shared.getInstance().messageBox(ex.message, context)
            if (!isLoadMore) {
                GetProfileStatus()
            } else
                stop_loading_animation()
        }

    }//LoggedInUser end

    override fun onDestroyView() {
        if (call != null) {
            call!!.cancel()
        }

        super.onDestroyView()
    }

    private fun start_loading_animation() {
        if (activity != null) {
            (activity as AppCompatActivity).supportActionBar!!.hide()
        }

        try {
            if (!pDialog!!.isShowing)
                pDialog!!.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun stop_loading_animation() {
        if (activity != null) {
            (activity as AppCompatActivity).supportActionBar!!.show()
        }

        view?.swipeRefreshLayout?.isRefreshing = false
        try {
            if (pDialog!!.isShowing)
                pDialog!!.dismiss()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun SuccessResponse() {
        view?.app_list?.visibility = View.VISIBLE
        view?.no_subapplication_available_div?.visibility = View.GONE

        if (app_actual_list != null && app_actual_list!!.size > 0) {

            if (mHSListener != null) {
                mHSListener!!.mAction(false)
            }

        }
    }

    private fun UnSuccessResponse() {
        view?.app_list?.visibility = View.GONE
        view?.no_subapplication_available_div?.visibility = View.VISIBLE

        if (ESPApplication.getInstance().user.loginResponse?.role?.toLowerCase(Locale.getDefault()).equals(Enums.applicant.toString(), ignoreCase = true)) {
            view?.add_btn?.visibility = View.VISIBLE
            view?.detail_text?.visibility = View.VISIBLE
        } else {
            view?.add_btn?.visibility = View.GONE
            view?.detail_text?.visibility = View.GONE
        }

    }

    fun GetProfileStatus() {

        if (!ESPApplication.getInstance().user.loginResponse?.role?.toLowerCase(Locale.getDefault()).equals(Enums.applicant.toString(), ignoreCase = true)) {
            stop_loading_animation()
            return
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

            httpClient.connectTimeout(2, TimeUnit.MINUTES)
            httpClient.readTimeout(2, TimeUnit.MINUTES)
            httpClient.writeTimeout(2, TimeUnit.MINUTES)

            val gson = GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                    .create()

            val retrofit = Retrofit.Builder()
                    .baseUrl(Constants.base_url)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(httpClient.build())

                    .build()


            val apis = retrofit.create(APIs::class.java)


            profile_call = apis.userProfileStatus
            profile_call!!.enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>?) {

                    stop_loading_animation()

                    if (response != null && response.body() != null) {

                        val response_text = response.body()
                        ESPApplication.getInstance().user.profileStatus = response_text

                        if (response_text.equals(context?.getString(R.string.profile_complete), ignoreCase = true)) {
                        } else if (response_text.equals(context?.getString(R.string.profile_incomplete), ignoreCase = true)) {
                            Shared.getInstance().showAlertProfileMessage(context?.getString(R.string.profile_error_heading), context?.getString(R.string.profile_error_desc), context)

                        } else if (response_text.equals(context?.getString(R.string.profile_incomplete_admin), ignoreCase = true)) {
                            Shared.getInstance().showAlertProfileMessage(context?.getString(R.string.profile_error_heading), context?.getString(R.string.profile_error_desc_admin), context)
                        }

                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    stop_loading_animation()
                }
            })

        } catch (ex: Exception) {
            if (ex != null) {
                stop_loading_animation()

            }
        }

    }

    /*fun getApplicant() {

        try {

            start_loading_animation()
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY
            val httpClient = OkHttpClient.Builder()
            httpClient.addInterceptor(logging)

            httpClient.addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                        .header("Authorization", "bearer " + ESPApplication.getInstance().user.loginResponse?.access_token)
                        .header("locale", Shared.getInstance().getLanguage(context))

                val request = requestBuilder.build()
                chain.proceed(request)
            }

            httpClient.connectTimeout(2, TimeUnit.MINUTES)
            httpClient.readTimeout(2, TimeUnit.MINUTES)
            httpClient.writeTimeout(2, TimeUnit.MINUTES)

            val gson = GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                    .create()

            val retrofit = Retrofit.Builder()
                    .baseUrl(Constants.base_url)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(httpClient.build())
                    .build()


            val apis = retrofit.create(APIs::class.java)


            val labels_call = apis.Getapplicant()

            labels_call.enqueue(object : Callback<ApplicationProfileDAO> {
                override fun onResponse(call: Call<ApplicationProfileDAO>, response: Response<ApplicationProfileDAO>) {
                    stop_loading_animation()
                    val body = response.body()
                    val profileSubmitted = response.body().applicant.isProfileSubmitted
                    CustomLogs.displayLogs("$TAG response profileSubmitted: $profileSubmitted")


                    val mainIntent = Intent(context, ProfileMainActivity::class.java)
                    mainIntent.putExtra("dataapplicant", body)
                    mainIntent.putExtra("ischeckerror", true)
                    startActivity(mainIntent)

                }

                override fun onFailure(call: Call<ApplicationProfileDAO>, t: Throwable) {
                    stop_loading_animation()
                    t.printStackTrace()
                    Shared.getInstance().messageBox(getString(R.string.some_thing_went_wrong), context)
                }
            })


        } catch (ex: Exception) {
            ex.printStackTrace()
            stop_loading_animation()
            Shared.getInstance().messageBox(getString(R.string.some_thing_went_wrong), context)
        }

    }*/

    private fun checkPermission(): Boolean {

        val permissionInternal = context?.let { ContextCompat.checkSelfPermission(it, READ_EXTERNAL_STORAGE) }
        val permissionExternal = context?.let { ContextCompat.checkSelfPermission(it, WRITE_EXTERNAL_STORAGE) }

        val listPermissionsNeeded = ArrayList<String>()

        if (permissionInternal != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(READ_EXTERNAL_STORAGE)
        }

        if (permissionExternal != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(WRITE_EXTERNAL_STORAGE)
        }

        if (!listPermissionsNeeded.isEmpty()) {
            context?.let { ActivityCompat.requestPermissions(it, listPermissionsNeeded.toTypedArray(), REQUEST_ID_MULTIPLE_PERMISSIONS) }
            return false
        }
        return true
    }

    private fun requestPermission() {
        context?.let { ActivityCompat.requestPermissions(it, arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE) }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.size > 0) {
                val locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                if (locationAccepted) {
                    //"Permission Granted, Now you can access location data."

                    try {
                        Shared.getInstance().createFolder(Constants.FOLDER_PATH, Constants.FOLDER_NAME, context)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }


                }
            }
        }
    }

    companion object {

        private val PERMISSION_REQUEST_CODE = 200
        val REQUEST_ID_MULTIPLE_PERMISSIONS = 1

        fun newInstance(): UsersSubApplicationsFragment {
            val fragment = UsersSubApplicationsFragment()
            val args = Bundle()

            fragment.arguments = args
            return fragment
        }
    }

}// Required empty public constructor
