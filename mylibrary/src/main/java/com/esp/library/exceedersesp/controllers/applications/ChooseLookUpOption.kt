package com.esp.library.exceedersesp.controllers.applications

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import com.esp.library.R
import com.esp.library.utilities.common.ProgressBarAnimation
import com.esp.library.utilities.common.Shared
import com.esp.library.utilities.common.SharedPreference
import com.esp.library.exceedersesp.BaseActivity
import com.esp.library.exceedersesp.fragments.applications.AddApplicationFragment
import com.esp.library.utilities.common.CustomLogs
import com.esp.library.utilities.customevents.EventOptions
import kotlinx.android.synthetic.main.activity_choose_lookup.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import utilities.adapters.setup.applications.LookUpAdapter
import utilities.data.applicants.addapplication.LookUpDAO
import utilities.data.applicants.dynamics.DynamicFormSectionFieldDAO


class ChooseLookUpOption : BaseActivity() {

    var TAG = "ChooseLookUpOption"

    internal var context: BaseActivity? = null
    private var compAdapter: LookUpAdapter? = null
    private var mCompLayout: androidx.recyclerview.widget.RecyclerView.LayoutManager? = null
    internal var retrofit: Retrofit? = null
    internal var call: Call<List<LookUpDAO>>? = null
    var actualResponse: List<LookUpDAO>? = null


    internal var myActionMenuItem: MenuItem? = null
    internal var searchView: SearchView? = null
    internal var imm: InputMethodManager? = null
    internal var fieldDAO: DynamicFormSectionFieldDAO? = null
    internal var anim: ProgressBarAnimation? = null
    // var lookUpItems: List<LookUpDAO>?=null;
    internal var pDialog: AlertDialog? = null

    internal var IN_LIST_RECORDS = 0
    internal var TOTAL_RECORDS_AVAILABLE = 0
    var listLoadCount: Int = 25
    internal var PER_PAGE_RECORDS = 25
    var sublist = ArrayList<LookUpDAO>()
    override fun onDestroy() {
        super.onDestroy()
        val view = currentFocus
        if (view != null) {
            imm!!.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_lookup)
        initialize()
        setGravity()
        populateData()



        etxtsearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                val usersList = ArrayList<LookUpDAO>()

                //   if (s.length > 0) {

                val textlength = s.length
                val searched_text = s



                if (actualResponse != null && actualResponse!!.size > 0) {
                    for (st in actualResponse!!) {
                        if (textlength <= st.name!!.length) {
                            if (st.name!!.toLowerCase().contains(searched_text)) {
                                usersList.add(st)
                            }
                        }
                    }
                }

                if (usersList.size > 0) {
                    compAdapter = context?.let { LookUpAdapter(usersList, it, searched_text.toString(), fieldDAO) }
                    search_list.adapter = compAdapter
                    //  compAdapter!!.notifyDataSetChanged()
                    SuccessResponse()
                } else {
                    UnSuccessResponse()
                }


                /*} else {
                    *//* IN_LIST_RECORDS = 0;
                     listLoadCount = PER_PAGE_RECORDS
                     sublist.clear()*//*

                    SuccessResponse()

                    if (IN_LIST_RECORDS < TOTAL_RECORDS_AVAILABLE) {
                        if (actualResponse != null && actualResponse!!.size > 0) {
                            listLoadCount += PER_PAGE_RECORDS
                            sublist.addAll(actualResponse?.subList(IN_LIST_RECORDS, listLoadCount)!!)
                            IN_LIST_RECORDS = listLoadCount
                            compAdapter = context?.let { LookUpAdapter(sublist, it, "", fieldDAO) }
                            search_list.adapter = compAdapter
                        }
                    }
                }*/


            }

            override fun afterTextChanged(s: Editable) {

            }
        })


    }

    private fun initialize() {
        context = this@ChooseLookUpOption
        pDialog = Shared.getInstance().setProgressDialog(context)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(false)
        supportActionBar?.title = ""
        toolbar.setNavigationIcon(R.drawable.ic_nav_back)
        toolbar.navigationIcon?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        toolbar.setNavigationOnClickListener { v -> finish() }

        imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        mCompLayout = androidx.recyclerview.widget.LinearLayoutManager(context)
        search_list.setHasFixedSize(false)
        search_list.isNestedScrollingEnabled = false
        search_list.layoutManager = mCompLayout

    }

    fun populateData() {
        if (intent != null) {
            val bundle = intent?.extras
            if (bundle != null) {
                fieldDAO = bundle.getSerializable(DynamicFormSectionFieldDAO.BUNDLE_KEY) as DynamicFormSectionFieldDAO
                if (fieldDAO != null) {
                    toolbar_heading.text = fieldDAO!!.label

                    actualResponse = Shared.getInstance().getLookUpItems(fieldDAO!!.sectionCustomFieldId)
                    if (fieldDAO!!.allowedValuesCriteria != null && fieldDAO!!.allowedValuesCriteria!!.isNotEmpty()) {
                        val jsonArray = JSONArray(fieldDAO?.allowedValuesCriteria)
                        if (jsonArray.length() > 0) {
                            if (actualResponse != null && actualResponse!!.isNotEmpty()) {
                                compAdapter = LookUpAdapter(actualResponse!!, context!!, "", fieldDAO)
                                search_list.adapter = compAdapter
                                SuccessResponse()
                            }
                        } else {
                            getLookups()
                        }
                    } else {
                        getLookups()
                    }

                }
            }

        }
    }

    fun refreshData() {
        if (fieldDAO != null) {
            //toolbar_heading.text = fieldDAO!!.label
            actualResponse = ArrayList<LookUpDAO>()
            actualResponse = Shared.getInstance().getLookUpItems(fieldDAO!!.sectionCustomFieldId)
            if (fieldDAO!!.allowedValuesCriteria != null && fieldDAO!!.allowedValuesCriteria!!.isNotEmpty()) {
                val jsonArray = JSONArray(fieldDAO?.allowedValuesCriteria)
                if (jsonArray.length() > 0) {
                    if (actualResponse != null && actualResponse!!.isNotEmpty()) {
                        compAdapter = LookUpAdapter(actualResponse!!, context!!, "", fieldDAO)
                        search_list.adapter = compAdapter
                        SuccessResponse()
                    }
                } else {
                    getLookups()
                }
            } else {
                getLookups()
            }
        }
    }

    fun getLookups() {
        if (Shared.getInstance().isWifiConnected(context)) {
            GetLoadLookUps(fieldDAO!!.lookUpId)
        } else {
            Shared.getInstance().showAlertMessage(getString(R.string.internet_error_heading), getString(R.string.internet_connection_error), bContext)
        }
    }

    private fun updateData() {
        if (IN_LIST_RECORDS < TOTAL_RECORDS_AVAILABLE) {
            if (actualResponse != null && actualResponse!!.size > 0) {
                listLoadCount += PER_PAGE_RECORDS
                if (actualResponse!!.size < listLoadCount)
                    listLoadCount = actualResponse!!.size
                sublist.addAll(actualResponse?.subList(IN_LIST_RECORDS, listLoadCount)!!)
                IN_LIST_RECORDS = listLoadCount
                compAdapter?.notifyDataSetChanged()
            }
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

    private fun SuccessResponse() {
        search_list.visibility = View.VISIBLE
        etxtsearch.visibility = View.VISIBLE
        no_results_available_div.visibility = View.GONE
    }

    private fun UnSuccessResponse() {
        search_list.visibility = View.GONE
        no_results_available_div.visibility = View.VISIBLE
    }

    fun GetLoadLookUps(id: Int?) {


        start_loading_animation()

        try {

            call = Shared.getInstance().retroFitObject(context).Lookups(id)

            call!!.enqueue(object : Callback<List<LookUpDAO>> {
                override fun onResponse(call: Call<List<LookUpDAO>>, response: Response<List<LookUpDAO>>) {

                    stop_loading_animation()
                    if (response.body() != null && response.body().size > 0) {
                        actualResponse = response.body()

                        if (actualResponse!!.size < PER_PAGE_RECORDS)
                            PER_PAGE_RECORDS = actualResponse!!.size

                        sublist.addAll(actualResponse?.subList(0, PER_PAGE_RECORDS)!!)


                        if (actualResponse != null && actualResponse!!.size > 0) {


                            //    compAdapter!!.notifyDataSetChanged()
                            IN_LIST_RECORDS = sublist.size
                            TOTAL_RECORDS_AVAILABLE = response.body().size

                            compAdapter = LookUpAdapter(actualResponse!!, context!!, "", fieldDAO)
                            search_list.adapter = compAdapter
                            etxtsearch.visibility = View.VISIBLE
                            SuccessResponse()
                            //addScroller()

                        } else {
                            etxtsearch.visibility = View.GONE
                            UnSuccessResponse()
                        }

                    } else {
                        etxtsearch.visibility = View.GONE
                        UnSuccessResponse()
                    }

                }

                override fun onFailure(call: Call<List<LookUpDAO>>, t: Throwable) {
                    etxtsearch.visibility = View.GONE
                    stop_loading_animation()
                    UnSuccessResponse()
                }
            })

        } catch (ex: Exception) {
            etxtsearch.visibility = View.GONE
            stop_loading_animation()
            UnSuccessResponse()

        }

    }//LoggedInUser end

    private fun setGravity() {
        val pref = SharedPreference(context)
        if (pref.language.equals("ar", ignoreCase = true)) {
            toolbar_heading.gravity = Gravity.RIGHT
            card_error_text.gravity = Gravity.RIGHT

        } else {
            toolbar_heading.gravity = Gravity.LEFT
            card_error_text.gravity = Gravity.LEFT
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    companion object {

        var isOpen: Boolean = false
        var ACTIVITY_NAME = "controllers.applications.ChooseLookUpOption"
    }

    override fun onResume() {
        super.onResume()
        isOpen = true


    }

    override fun onPause() {
        super.onPause()
        isOpen = false
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun dataRefreshEvent(eventTriggerController: EventOptions.EventTriggerController) {
        stop_loading_animation()
        refreshData()
    }



    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }


}
