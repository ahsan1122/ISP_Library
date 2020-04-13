package com.esp.library.exceedersesp.controllers.applications

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.esp.library.R
import com.esp.library.exceedersesp.ESPApplication
import com.esp.library.utilities.common.*
import com.esp.library.exceedersesp.BaseActivity
import kotlinx.android.synthetic.main.fragment_subusers_applications.*
import kotlinx.android.synthetic.main.fragment_users_applications.*
import kotlinx.android.synthetic.main.fragment_users_applications.app_list
import kotlinx.android.synthetic.main.fragment_users_applications.load_more_div
import kotlinx.android.synthetic.main.fragment_users_applications.swipeRefreshLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import utilities.adapters.setup.applications.ListUsersApplicationsAdapter
import utilities.common.*
import utilities.data.applicants.ApplicationsDAO
import utilities.data.applicants.ResponseApplicationsDAO
import utilities.data.applicants.dynamics.DynamicResponseDAO
import utilities.data.filters.FilterDAO
import java.util.*

class UserSubApplicationsActivity : BaseActivity() {

    internal var TAG = javaClass.simpleName

    internal var context: BaseActivity? = null
    private var mApplicationAdapter: androidx.recyclerview.widget.RecyclerView.Adapter<*>? = null
    private var mApplicationLayoutManager: androidx.recyclerview.widget.RecyclerView.LayoutManager? = null
    internal var call: Call<ResponseApplicationsDAO>? = null
    internal var profile_call: Call<String>? = null
    internal var app_actual_list: MutableList<ApplicationsDAO>? = null
    internal var anim: ProgressBarAnimation? = null

    internal var PAGE_NO = 1
    internal var PER_PAGE_RECORDS = 12
    internal var IN_LIST_RECORDS = 0
    internal var SCROLL_TO = 0
    internal var TOTAL_RECORDS_AVAILABLE = 0
    internal var mHSListener: HideShowPlus? = null
    internal var pref: SharedPreference? = null

    internal var curvetoolbar: View? = null
    internal var toolbar: Toolbar? = null
    internal var toolbarheading: TextView? = null

    interface HideShowPlus {
        fun mAction(IsShown: Boolean)
    }

    private fun AddScroller() {

        app_list?.addOnScrollListener(object : EndlessRecyclerViewScrollListener(mApplicationLayoutManager as androidx.recyclerview.widget.LinearLayoutManager?) {
            override fun onHide() {
                //  Shared.getInstance().setToolbarHeight(toolbar, false)
            }

            override fun onShow() {
                // Shared.getInstance().setToolbarHeight(toolbar, true)
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
        setContentView(R.layout.fragment_subusers_applications)
        initailize()


        app_list.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
            }

            override fun onScrollStateChanged(recyclerView: androidx.recyclerview.widget.RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
            }
        })

        try {
            mHSListener = context as HideShowPlus
        } catch (e: Exception) {

        }

        if (Shared.getInstance().isWifiConnected(context)) {

            LoadApplications(false)
        } else {
            Shared.getInstance().showAlertMessage(context?.getString(R.string.internet_error_heading), context?.getString(R.string.internet_connection_error), context)
        }

        swipeRefreshLayout?.setOnRefreshListener {
            if (Shared.getInstance().isWifiConnected(context)) {

                PAGE_NO = 1
                PER_PAGE_RECORDS = 12
                IN_LIST_RECORDS = 0
                TOTAL_RECORDS_AVAILABLE = 0

                LoadApplications(false)
            } else {
                Shared.getInstance().showAlertMessage(context?.getString(R.string.internet_error_heading), context?.getString(R.string.internet_connection_error), context)
                swipeRefreshLayout?.isRefreshing = false
            }
        }

        if (!checkPermission()) {
            requestPermission()
        }

    }

    private fun initailize() {
        context = this@UserSubApplicationsActivity
        pref = SharedPreference(context)
        supportActionBar?.title = ""
        supportActionBar?.setDisplayShowHomeEnabled(true)
        curvetoolbar = findViewById(R.id.curvetoolbar)

        toolbar = curvetoolbar?.findViewById(R.id.toolbar)

        mApplicationLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        app_list.setHasFixedSize(true)
        app_list.layoutManager = mApplicationLayoutManager
        app_list.itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()
        txtnoapplicationadded?.text = context?.getString(R.string.no) + " " + pref?.getlabels()?.application + " " + context?.getString(R.string.added)
        txtnoapplicationadded?.text = context?.getString(R.string.startsubmittingapp) + " " + pref?.getlabels()?.application + " " + context?.getString(R.string.itseasy)

        setupToolbar(toolbar)

    }

    private fun setupToolbar(toolbar: Toolbar?) {
        toolbar?.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_nav_back)
        toolbar?.navigationIcon?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        toolbar?.setNavigationOnClickListener { finish() }
        toolbarheading = toolbar?.findViewById(R.id.toolbarheading)
        toolbarheading?.text = getString(R.string.submissions)
    }


    fun LoadApplications(isLoadMore: Boolean) {

        if (isLoadMore) {
            load_more_div?.visibility = View.VISIBLE
        } else {
            start_loading_animation()
        }

        /* APIs Mapping respective Object*/
        val apis = Shared.getInstance().retroFitObject(context)

        if (isLoadMore) {

        } else {
            PAGE_NO = 1
            PER_PAGE_RECORDS = 12
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


        val getDynamicStagesDAO = intent.getSerializableExtra("dynamicResponseDAO") as DynamicResponseDAO
        dao.parentApplicationId = getDynamicStagesDAO.applicationId.toString()
        dao.applicantId = "0"
        dao.definationId = null
        dao.search = ""
        // call = apis.GetUserSubApplicationsList("", 0, PAGE_NO, PER_PAGE_RECORDS, false, 1, "", getDynamicStagesDAO.linkDefinitionId);
        call = apis.getUserApplicationsV3(dao)



        call!!.enqueue(object : Callback<ResponseApplicationsDAO> {
            override fun onResponse(call: Call<ResponseApplicationsDAO>, response: Response<ResponseApplicationsDAO>?) {

                if (isLoadMore) {
                    load_more_div?.visibility = View.GONE
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

                            try {
                                mApplicationAdapter = context?.let { ListUsersApplicationsAdapter(app_actual_list, it, "", true) }
                                app_list?.adapter = mApplicationAdapter
                                mApplicationAdapter!!.notifyDataSetChanged()
                                app_list?.scrollToPosition(SCROLL_TO - 5)
                            }
                            catch (e:java.lang.Exception){}

                        } else {
                            try{
                            app_actual_list = response.body().applications as MutableList<ApplicationsDAO>?
                            mApplicationAdapter = context?.let { ListUsersApplicationsAdapter(app_actual_list, it, "", true) }
                            app_list?.adapter = mApplicationAdapter
                            PAGE_NO++
                            IN_LIST_RECORDS = app_actual_list!!.size
                            TOTAL_RECORDS_AVAILABLE = response.body().totalRecords
                            SCROLL_TO = 0
                            AddScroller()
                            }
                            catch (e:java.lang.Exception){}
                        }

                        txtrequests.text = TOTAL_RECORDS_AVAILABLE.toString() + " " + getString(R.string.submissions)
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

    }//LoggedInUser end


    override fun onDestroy() {
        super.onDestroy()
        if (call != null) {
            call!!.cancel()
        }

    }

    private fun start_loading_animation() {

        shimmerview_container.visibility = View.VISIBLE
        shimmerview_container.startShimmerAnimation();
        app_list?.visibility = View.GONE


    }

    private fun stop_loading_animation() {


        swipeRefreshLayout?.isRefreshing = false
        shimmerview_container.visibility = View.GONE
        shimmerview_container.stopShimmerAnimation();
        app_list?.visibility = View.VISIBLE


    }

    private fun SuccessResponse() {
        app_list?.visibility = View.VISIBLE
        no_subapplication_available_div?.visibility = View.GONE

        if (app_actual_list != null && app_actual_list!!.size > 0) {

            if (mHSListener != null) {
                mHSListener!!.mAction(false)
            }

        }
    }

    private fun UnSuccessResponse() {
        app_list?.visibility = View.GONE
        no_subapplication_available_div?.visibility = View.VISIBLE

        if (ESPApplication.getInstance().user.loginResponse?.role?.toLowerCase(Locale.getDefault()).equals(Enums.assessor.toString(), ignoreCase = true)) {
            add_btn?.visibility = View.VISIBLE
            detail_text?.visibility = View.VISIBLE
        } else {
            add_btn?.visibility = View.GONE
            detail_text?.visibility = View.GONE
        }

    }

    fun GetProfileStatus() {

        if (!ESPApplication.getInstance().user.loginResponse?.role?.toLowerCase(Locale.getDefault()).equals(Enums.assessor.toString(), ignoreCase = true)) {
            stop_loading_animation()
            return
        }
        start_loading_animation()

        val apis = Shared.getInstance().retroFitObject(context)

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


    }


    private fun checkPermission(): Boolean {

        val permissionInternal = context?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.READ_EXTERNAL_STORAGE) }
        val permissionExternal = context?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.WRITE_EXTERNAL_STORAGE) }

        val listPermissionsNeeded = ArrayList<String>()

        if (permissionInternal != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (permissionExternal != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (!listPermissionsNeeded.isEmpty()) {
            context?.let { ActivityCompat.requestPermissions(it, listPermissionsNeeded.toTypedArray(), REQUEST_ID_MULTIPLE_PERMISSIONS) }
            return false
        }
        return true
    }

    private fun requestPermission() {
        context?.let { ActivityCompat.requestPermissions(it, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE) }
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

    }

    override fun onBackPressed() {
        super.onBackPressed()
        ListUsersApplicationsAdapter.isSubApplications = false
    }

}