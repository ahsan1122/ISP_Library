package com.esp.library.exceedersesp.fragments.applications

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.esp.library.R
import com.esp.library.exceedersesp.ESPApplication
import com.esp.library.utilities.common.Constants
import com.esp.library.utilities.common.Shared
import com.esp.library.utilities.common.SharedPreference
import com.esp.library.exceedersesp.BaseActivity
import com.esp.library.exceedersesp.controllers.applications.AddApplicationsActivity
import com.esp.library.exceedersesp.controllers.applications.filters.FilterScreenActivity
import com.esp.library.utilities.common.Enums
import com.facebook.shimmer.ShimmerFrameLayout
import kotlinx.android.synthetic.main.activity_applications_drawer.*
import kotlinx.android.synthetic.main.card_users_applications.*
import kotlinx.android.synthetic.main.card_users_applications.toolbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import utilities.adapters.setup.applications.ListUsersApplicationsAdapter
import utilities.data.applicants.ApplicationsDAO
import utilities.data.applicants.ResponseApplicationsDAO
import utilities.data.filters.FilterDAO
import java.util.*


class UsersCardApplications : BaseActivity() {


    internal var TAG = javaClass.simpleName

    internal var context: BaseActivity? = null
    private var mApplicationAdapter: androidx.recyclerview.widget.RecyclerView.Adapter<*>? = null
    private var mApplicationLayoutManager: androidx.recyclerview.widget.RecyclerView.LayoutManager? = null
    internal var call: Call<ResponseApplicationsDAO>? = null
    internal var profile_call: Call<String>? = null
    internal var app_actual_list: MutableList<ApplicationsDAO>? = null
    internal var imm: InputMethodManager? = null
    internal var PAGE_NO = 1
    internal var PER_PAGE_RECORDS = 12
    internal var IN_LIST_RECORDS = 0
    internal var SCROLL_TO = 0
    internal var TOTAL_RECORDS_AVAILABLE = 0
    internal var mHSListener: HideShowPlus? = null
    internal var pref: SharedPreference? = null
    internal var shimmer_view_container: ShimmerFrameLayout? = null


    interface HideShowPlus {
        fun mAction(IsShown: Boolean)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.card_users_applications)
        initailize()

        tempDefinitionListInitailization()
        add_btn.text = context?.getString(R.string.submit) + " " + pref?.getlabels()?.application

        add_btn.setOnClickListener { view ->
            if (ESPApplication.getInstance().user.profileStatus == null || ESPApplication.getInstance().user.profileStatus.equals(context?.getString(R.string.profile_complete), ignoreCase = true)) {
                Shared.getInstance().callIntentWithResult(AddApplicationsActivity::class.java, context, null, 2)
            } else if (ESPApplication.getInstance().user.profileStatus == context?.getString(R.string.profile_incomplete)) {
                Shared.getInstance().showAlertProfileMessage(context?.getString(R.string.profile_error_heading), context?.getString(R.string.profile_error_desc), context)

            } else if (ESPApplication.getInstance().user.profileStatus == getString(R.string.profile_incomplete_admin)) {
                Shared.getInstance().showAlertProfileMessage(context?.getString(R.string.profile_error_heading), context?.getString(R.string.profile_error_desc_admin), context)
            }
        }




        nestedscrollview.getViewTreeObserver().addOnScrollChangedListener(ViewTreeObserver.OnScrollChangedListener {
            val view = nestedscrollview.getChildAt(nestedscrollview.getChildCount() - 1) as View

            val diff = view.bottom - (nestedscrollview.getHeight() + nestedscrollview
                    .getScrollY())

            if (diff == 0) {
                if (IN_LIST_RECORDS < TOTAL_RECORDS_AVAILABLE) {
                    loadApplications(true)

                }
            }
        })


        ivfilter.setOnClickListener { v -> Shared.getInstance().callIntentWithResult(FilterScreenActivity::class.java, context, null, 2) }


    }


    private fun initailize() {
        context = this
        pref = SharedPreference(context)

        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowHomeEnabled(false)
        supportActionBar!!.setTitle("")
        toolbar.navigationIcon = ContextCompat.getDrawable(context!!, R.drawable.ic_nav_back)
        toolbar.setNavigationOnClickListener(View.OnClickListener { onBackPressed() })
        toolbar.navigationIcon?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)

        toolbarheading.text = getString(R.string.actioncenter)

        mApplicationLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        app_list.setHasFixedSize(true)
        app_list.layoutManager = mApplicationLayoutManager
        app_list.itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()



        if (ESPApplication.getInstance()?.user?.loginResponse?.role?.toLowerCase(Locale.getDefault()).equals(Enums.applicant.toString(), ignoreCase = true)) {
            txtnoapplicationadded?.text = context?.getString(R.string.no) + " " + pref?.getlabels()?.application + " " + context?.getString(R.string.added)
            txtnoapplicationadded?.text = context?.getString(R.string.startsubmittingapp) + " " + pref?.getlabels()?.application + " " + context?.getString(R.string.itseasy)


        } else {
            txtnoapplicationadded?.text = context?.getString(R.string.norecord)
        }
        shimmer_view_container = findViewById(R.id.shimmer_view_container)
    }


    private fun refreshListCall() {
        when (Shared.getInstance().isWifiConnected(context)) {
            true -> {
                PAGE_NO = 1
                PER_PAGE_RECORDS = 12
                IN_LIST_RECORDS = 0
                TOTAL_RECORDS_AVAILABLE = 0
                app_actual_list?.clear()
                mApplicationAdapter?.notifyDataSetChanged()
                txtrequestcount?.text = ""
                loadApplications(false)
            }
            false -> {
                Shared.getInstance().showAlertMessage(context?.getString(R.string.internet_error_heading), context?.getString(R.string.internet_connection_error), context)
                swipeRefreshLayout?.isRefreshing = false
            }
        }
    }

    fun reLoadApplications() {
        refreshListCall()
    }


    fun loadApplications(isLoadMore: Boolean) {

        if (isLoadMore) {
            load_more_div?.visibility = View.VISIBLE
        } else {
            start_loading_animation()
        }

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
        ESPApplication.getInstance().filter.search = ""

        var dao: FilterDAO? = null
        val list = ArrayList<String>()


        dao = Shared.getInstance().CloneFilter(ESPApplication.getInstance().filter)


        dao.isMySpace = true
        dao.isFilterApplied = true
        dao.myApplications = false



        if (ESPApplication.getInstance().filter.statuses!!.size < 5) {
            if (!dao.statuses.isNullOrEmpty())
                dao.statuses = ArrayList<String>()
            dao.statuses = ESPApplication.getInstance().filter.statuses
        } else {
            list.add("2")
            dao.statuses = list
        }

        call = apis.getUserApplicationsV3(dao)



        call!!.enqueue(
                object : Callback<ResponseApplicationsDAO> {
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

                                        //val app_actual_list_temp = filterData(response)
                                        if (app_actual_list != null)
                                            app_actual_list?.addAll(response.body().applications!!)
                                        else
                                            app_actual_list = response.body().applications as MutableList<ApplicationsDAO>?


                                    }



                                    PAGE_NO++
                                    IN_LIST_RECORDS = removeDuplication(app_actual_list).size
                                    TOTAL_RECORDS_AVAILABLE = response.body().totalRecords
                                    SCROLL_TO += PER_PAGE_RECORDS

                                    mApplicationAdapter = context?.let { ListUsersApplicationsAdapter(removeDuplication(app_actual_list), it, "", false) }
                                    // (mApplicationAdapter as ListUsersApplicationsAdapter?)?.getFragmentContext(this@UsersCardApplicationsFragment)

                                    app_list?.adapter = mApplicationAdapter
                                    mApplicationAdapter!!.notifyDataSetChanged()
                                    app_list?.scrollToPosition(SCROLL_TO - 3)

                                } else {

                                    // val app_actual_list_temp = filterData(response)
                                    if (app_actual_list != null)
                                        app_actual_list?.addAll(response.body().applications!!)
                                    else
                                        app_actual_list = response.body().applications as MutableList<ApplicationsDAO>?

                                    mApplicationAdapter = context?.let { ListUsersApplicationsAdapter(removeDuplication(app_actual_list), it, "", false) }
                                    //  (mApplicationAdapter as ListUsersApplicationsAdapter?)?.getFragmentContext(this@UsersCardApplicationsFragment)
                                    app_list?.adapter = mApplicationAdapter
                                    PAGE_NO++
                                    IN_LIST_RECORDS = removeDuplication(app_actual_list).size
                                    TOTAL_RECORDS_AVAILABLE = response.body().totalRecords
                                    SCROLL_TO = 0


                                }

                                txtrequestcount?.text = TOTAL_RECORDS_AVAILABLE.toString() + " " + getString(R.string.requests)



                                SuccessResponse()

                                stop_loading_animation()

                            } else {

                                stop_loading_animation()
                                if (app_actual_list == null || app_actual_list?.size == 0)
                                    UnSuccessResponse()
                            }
                        } else {

                            stop_loading_animation()
                            if (app_actual_list == null || app_actual_list?.size == 0)
                                UnSuccessResponse()
                        }


                    }


                    override fun onFailure(call: Call<ResponseApplicationsDAO>, t: Throwable) {
                        //  Shared.getInstance().messageBox(t.message, context)
                        stop_loading_animation()
                        UnSuccessResponse()


                    }
                })


    }//LoggedInUser end

    fun removeDuplication(appActualList: MutableList<ApplicationsDAO>?): ArrayList<ApplicationsDAO> {
        val criteriaListCollections = ArrayList<ApplicationsDAO>()
        for (i in 0 until appActualList!!.size) {
            val getList = appActualList.get(i);
            val isArrayHasValue = criteriaListCollections.any { x -> x.id == getList.id }
            if (!isArrayHasValue) {
                criteriaListCollections.add(getList)
            }
        }

        return criteriaListCollections
    }

    override fun onDestroy() {
        if (call != null) {
            call!!.cancel()
        }

        super.onDestroy()
    }

    private fun start_loading_animation() {

        shimmer_view_container?.visibility = View.VISIBLE
        shimmer_view_container?.startShimmerAnimation();
        app_list?.visibility = View.GONE
    }

    private fun stop_loading_animation() {
        swipeRefreshLayout?.isRefreshing = false
        shimmer_view_container?.visibility = View.GONE
        shimmer_view_container?.stopShimmerAnimation();
        app_list?.visibility = View.VISIBLE


    }

    private fun SuccessResponse() {
        app_list?.visibility = View.VISIBLE
        no_application_available_div?.visibility = View.GONE

        if (app_actual_list != null && app_actual_list!!.size > 0) {

            if (mHSListener != null) {
                mHSListener!!.mAction(false)
            }

        }
    }

    private fun UnSuccessResponse() {
        app_list?.visibility = View.GONE
        no_application_available_div?.visibility = View.VISIBLE
        add_btn?.visibility = View.GONE
        detail_text?.visibility = View.GONE

    }


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


    override fun onResume() {
        super.onResume()

        when (Shared.getInstance().isWifiConnected(context)) {
            true -> reLoadApplications()
            false -> Shared.getInstance().showAlertMessage(context?.getString(R.string.internet_error_heading), context?.getString(R.string.internet_connection_error), context)
        }

    }

    private fun tempDefinitionListInitailization()
    {
        val tempList = ArrayList<Int>()
        //tempList.add(0)
        ESPApplication.getInstance().filter.definitionIds  = null
    }

    companion object {

        private val PERMISSION_REQUEST_CODE = 200
        val REQUEST_ID_MULTIPLE_PERMISSIONS = 1


    }

    override fun onBackPressed() {
        super.onBackPressed()
        tempDefinitionListInitailization()
    }




}// Required empty public constructor
