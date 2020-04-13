package com.esp.library.exceedersesp.controllers.lookupinfo

import android.app.Activity
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import com.esp.library.R
import com.esp.library.exceedersesp.ESPApplication
import com.esp.library.utilities.common.CustomLogs
import com.esp.library.utilities.common.EndlessRecyclerViewScrollListener
import com.esp.library.utilities.common.Shared
import com.esp.library.exceedersesp.BaseActivity
import com.esp.library.exceedersesp.controllers.lookupinfo.adapter.ListLookupInfoItemsAdapter
import kotlinx.android.synthetic.main.lookup_item_detail_list.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import utilities.data.applicants.dynamics.DynamicFormSectionDAO
import utilities.data.applicants.dynamics.DynamicFormValuesDAO
import utilities.data.lookup.LookupInfoListDetailDAO
import utilities.data.lookup.LookupInfoSearchDAO
import utilities.data.lookup.LookupValuesDAO
import java.util.*

class LoopUpItemDetailList : BaseActivity() {

    internal var TAG = javaClass.simpleName
    internal var context: BaseActivity? = null
    private var lookupItemLayoutManager: androidx.recyclerview.widget.RecyclerView.LayoutManager? = null
    internal var pageNo = 1
    internal var scrollTo = 0
    internal var TOTAL_RECORDS_AVAILABLE = 0

    internal var dynamicFormValuesArray: MutableList<DynamicFormValuesDAO> = ArrayList()
    internal var dynamicFormSectionDAOTempArray: MutableList<DynamicFormSectionDAO> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lookup_item_detail_list)
        initialize()

        CustomLogs.displayLogs(TAG + " lookupid: " + intent.getIntExtra("lookupid", 0))

        getData("", false)

        ivsearch.setOnClickListener { v ->
            pageNo = 1
            CustomLogs.displayLogs(TAG + " etxtsearch: " + etxtsearch.text.toString())
            getData(etxtsearch.text.toString(), false)
        }


        swipeRefreshLayout.setOnRefreshListener {
            if (Shared.getInstance().isWifiConnected(context)) {
                TOTAL_RECORDS_AVAILABLE = 0
                pageNo = 1
                etxtsearch.text?.clear()
                getData("", false)
            } else {
                Shared.getInstance().showAlertMessage(context?.getString(R.string.internet_error_heading), context?.getString(R.string.internet_connection_error), context)
                swipeRefreshLayout.isRefreshing = false
            }
        }


    }

    private fun addScroller() {
        lookup_item_list.addOnScrollListener(object : EndlessRecyclerViewScrollListener(lookupItemLayoutManager as androidx.recyclerview.widget.LinearLayoutManager?) {
            override fun onHide() {
              //  Shared.getInstance().setToolbarHeight(toolbar, false)
            }

            override fun onShow() {
             //   Shared.getInstance().setToolbarHeight(toolbar, true)
            }

            override fun getFooterViewType(defaultNoFooterViewType: Int): Int {
                return defaultNoFooterViewType
            }

            override fun onLoadMore(page: Int, totalItemsCount: Int) {

                if (totalItemsCount < TOTAL_RECORDS_AVAILABLE) {
                    scrollTo = totalItemsCount

                    var searchText = etxtsearch.text.toString()
                    if (searchText.replace("\\s".toRegex(), "").length == 0)
                        searchText = ""

                    getData(searchText, true)

                    CustomLogs.displayLogs("$TAG onLoadMore totalItemsCount: $totalItemsCount")
                }

            }
        })
    }

    private fun initialize() {
        context = this as BaseActivity
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(false)
        supportActionBar?.setTitle("")
        toolbar.navigationIcon = ContextCompat.getDrawable(this,R.drawable.ic_nav_back)
        toolbar.navigationIcon?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        toolbar.setNavigationOnClickListener { v -> onBackPressed() }
        toolbar_heading.text = intent.getStringExtra("toolbar_heading")

        lookupItemLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        lookup_item_list.setHasFixedSize(true)
        lookup_item_list.layoutManager = lookupItemLayoutManager
        lookup_item_list.itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()

    }

    private fun getData(searchString: String, isLoadMore: Boolean) {
        var searchString = searchString
        if (Shared.getInstance().isWifiConnected(context)) {
            if (searchString.replace("\\s".toRegex(), "").length == 0)
                searchString = ""

            getLookupItemList(searchString, isLoadMore)
        } else {
            Shared.getInstance().showAlertMessage(getString(R.string.internet_error_heading), getString(R.string.internet_connection_error), bContext)
        }

    }

    fun getLookupItemList(searchString: String, isLoadMore: Boolean) {
        context?.let { hideKeyboard(it) }
        try {
            if (isLoadMore) {
                pageNo = pageNo + 1
                load_more_div.visibility = View.VISIBLE
            } else
                start_loading_animation()

            val lookupInfoSearchDAO = LookupInfoSearchDAO(0, intent.getIntExtra("lookupid",
                    0), pageNo, 12, searchString)


            val labels_call = Shared.getInstance().retroFitObject(context).postLookUpItems(lookupInfoSearchDAO)

            labels_call.enqueue(object : Callback<LookupInfoListDetailDAO> {
                override fun onResponse(call: Call<LookupInfoListDetailDAO>, response: Response<LookupInfoListDetailDAO>) {

                    if (isLoadMore) {
                        load_more_div.visibility = View.GONE
                    } else {
                        stop_loading_animation()
                        addScroller()
                    }


                    val body = response.body()
                    TOTAL_RECORDS_AVAILABLE = body.totalCount
                    populateData(body, isLoadMore)


                }

                override fun onFailure(call: Call<LookupInfoListDetailDAO>, t: Throwable) {
                    if (isLoadMore) {
                        load_more_div.visibility = View.GONE
                    } else
                        stop_loading_animation()
                    t.printStackTrace()
                    Shared.getInstance().messageBox(getString(R.string.some_thing_went_wrong), context)
                }
            })


        } catch (ex: Exception) {
            ex.printStackTrace()
            if (isLoadMore) {
                load_more_div.visibility = View.GONE
            } else
                stop_loading_animation()
            Shared.getInstance().messageBox(getString(R.string.some_thing_went_wrong), context)
        }

    }

    private fun populateData(body: LookupInfoListDetailDAO, isLoadMore: Boolean) {
        if (!isLoadMore) {
            dynamicFormValuesArray.clear()
            dynamicFormSectionDAOTempArray.clear()
        }

        var employeeName = ""

        val titleCustomFieldId = body.lookupTemplate.titleCustomFieldId
        val isShowEmployeeName = body.lookupTemplate.isVariable // if true the show employee name else hide it
        val sections = body.lookupTemplate.form.sections
        for (i in sections.indices) {
            var dynamicFormValuesDAOGlobal = DynamicFormValuesDAO()
            val dynamicFormSectionDAO = sections[i]
            val fields = dynamicFormSectionDAO.fields

            for (k in fields!!.indices) {
                val dynamicFormSectionFieldDAO = fields[k]
                val id = dynamicFormSectionFieldDAO.id

                if (id == titleCustomFieldId) {
                    val sectionCustomFieldId = dynamicFormSectionFieldDAO.sectionCustomFieldId
                    val items = body.items


                    for (j in items.indices) {
                        val itemsList = items[j]

                        val values = itemsList.values

                        for (h in values!!.indices) {
                            val dynamicFormValuesDAO = values[h]


                            val customFieldLookupId = dynamicFormValuesDAO.customFieldLookupId
                            val type = dynamicFormValuesDAO.type
                            if (type == 13 && customFieldLookupId == -1) {
                                employeeName = dynamicFormValuesDAO.selectedLookupText!!
                            } else
                                employeeName = ESPApplication.getInstance().user.loginResponse?.name!!
                            val valueSectionCustomFieldId = dynamicFormValuesDAO.sectionCustomFieldId

                            if (sectionCustomFieldId == valueSectionCustomFieldId) {
                                val value = dynamicFormValuesDAO.value
                                dynamicFormValuesDAO.itemid = itemsList.id
                                dynamicFormValuesDAOGlobal = dynamicFormValuesDAO
                                dynamicFormValuesArray.add(dynamicFormValuesDAOGlobal)
                                dynamicFormSectionDAOTempArray.add(dynamicFormSectionDAO)
                            }

                        }
                    }

                }
            }
        }

        try {
            var lookupValuesDAO: LookupValuesDAO? = null
            for (i in dynamicFormValuesArray.indices) {
                val formSectionValues = body.items[i].formSectionValues
                if (formSectionValues != null) {
                    val sb = StringBuilder()
                    for (j in formSectionValues.indices) {
                        val label = formSectionValues[j].label
                        val value = formSectionValues[j].value
                        sb.append(label)
                        sb.append("<:>")
                        sb.append(value)
                        sb.append("_:_")
                        lookupValuesDAO = LookupValuesDAO()
                        lookupValuesDAO.label = label
                        lookupValuesDAO.value = value
                    }
                    if (lookupValuesDAO != null)
                        dynamicFormValuesArray[i].filterLookup = sb.toString()
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }


        body.lookupTemplate.form.sections = dynamicFormSectionDAOTempArray


        if (dynamicFormValuesArray.size == 0) {

            llempty.visibility = View.VISIBLE
            rlsearch.visibility = View.GONE
            swipeRefreshLayout.visibility = View.GONE

        } else {
            llempty.visibility = View.GONE
            rlsearch.visibility = View.VISIBLE
            swipeRefreshLayout.visibility = View.VISIBLE
            val listLookupInfoItemsAdapter = context?.let {
                ListLookupInfoItemsAdapter(dynamicFormValuesArray, body,
                        isShowEmployeeName, employeeName, it)
            }
            lookup_item_list.adapter = listLookupInfoItemsAdapter
        }

        if (isLoadMore)
            lookup_item_list.scrollToPosition(scrollTo - 3)


    }

    fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun start_loading_animation() {
        swipeRefreshLayout.isRefreshing = true

        shimmer_view_container?.visibility=View.VISIBLE
        shimmer_view_container?.startShimmerAnimation();
        lookup_item_list?.visibility=View.GONE

    }

    private fun stop_loading_animation() {
        swipeRefreshLayout.isRefreshing = false

        shimmer_view_container?.visibility=View.GONE
        shimmer_view_container?.stopShimmerAnimation();
        lookup_item_list?.visibility=View.VISIBLE

    }

    override fun onBackPressed() {
        super.onBackPressed()
        context?.let { hideKeyboard(it) }
    }
}
