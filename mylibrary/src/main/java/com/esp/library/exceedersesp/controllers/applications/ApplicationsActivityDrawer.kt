package com.esp.library.exceedersesp.controllers.applications

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.SearchAutoComplete
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.esp.library.R
import com.esp.library.exceedersesp.BaseActivity
import com.esp.library.exceedersesp.ESPApplication
import com.esp.library.exceedersesp.controllers.WebViewScreenActivity
import com.esp.library.exceedersesp.controllers.applications.filters.FilterActivity
import com.esp.library.exceedersesp.controllers.applications.filters.FilterScreenActivity
import com.esp.library.exceedersesp.fragments.NavigationDrawerFragment
import com.esp.library.exceedersesp.fragments.applications.UsersApplicationsFragment
import com.esp.library.exceedersesp.fragments.applications.UsersSearchApplicationsFragment
import com.esp.library.utilities.common.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_applications_drawer.*
import kotlinx.android.synthetic.main.bottom_sheet.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import utilities.adapters.setup.FilterItemsAdapter
import utilities.adapters.setup.ListPersonaDAOAdapter
import utilities.adapters.setup.applications.ListApplicationSubDefinationAdapter
import utilities.data.applicants.addapplication.CategoryAndDefinationsDAO
import utilities.data.setup.PersonaDAO
import utilities.interfaces.DeleteFilterListener
import utilities.interfaces.RefreshSubDefinitionListener
import java.util.*
import kotlin.collections.ArrayList


class ApplicationsActivityDrawer : BaseActivity(), UsersApplicationsFragment.HideShowPlus, AlertActionWindow.ActionInterface,
        ListPersonaDAOAdapter.RefreshToken, DeleteFilterListener, RefreshSubDefinitionListener {


    internal var context: BaseActivity? = null
    internal var wasSheetShowing: Boolean = false
    internal var searchView: SearchView? = null
    internal var fm: androidx.fragment.app.FragmentManager? = null
    internal var submit_request: UsersApplicationsFragment? = null
    internal var submit_request_search: UsersSearchApplicationsFragment? = null
    internal var myActionMenuItem: MenuItem? = null
    internal var pref: SharedPreference? = null
    internal var pDialog: AlertDialog? = null
    private var mNavigationDrawerFragment: NavigationDrawerFragment? = null
    private var mDefAdapter: ListApplicationSubDefinationAdapter? = null
    internal var definition_list: ArrayList<CategoryAndDefinationsDAO> = ArrayList()
    internal var filter_definition_list: ArrayList<CategoryAndDefinationsDAO> = ArrayList()
    var sheetBehavior: BottomSheetBehavior<*>? = null
    internal var filter_adapter: FilterItemsAdapter? = null

    var TAG:String="ApplicationsActivityDrawer"

    @SuppressLint("RestrictedApi")
    override fun mAction(IsShown: Boolean) {
        if (IsShown) {
            add_account.visibility = View.GONE
        } else {
            // addAccount.setVisibility(View.VISIBLE);
            if (ESPApplication.getInstance()?.user?.loginResponse?.role?.toLowerCase(Locale.getDefault()) == Enums.applicant.toString()) {
                if (sheetBehavior?.state != BottomSheetBehavior.STATE_EXPANDED && request_fragment_search.visibility == View.GONE) {
                    add_account.visibility = View.VISIBLE
                }

            } else {
                add_account.visibility = View.GONE
            }

        }
    }

    override fun mActionTo(whattodo: String) {
        if (whattodo == "profile") {
            val bn = Bundle()
            bn.putString("heading", getString(R.string.profile))
            //  bn.putString("url", "https://qaesp.azurewebsites.net/profile")
            //   bn.putString("url", "https://esp.exceeders.com/profile")
            bn.putString("url", Constants.base_url.replace("webapi/", "") + "profile")
            Shared.getInstance().callIntentWithResult(WebViewScreenActivity::class.java, context, bn, 5)

        }
    }

    override fun StatusChange(update: PersonaDAO) {
        if (mNavigationDrawerFragment != null) {
            mNavigationDrawerFragment?.RefreshToken(update)
        }
    }


    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_applications_drawer)

        initailize()
        setGravity()


        sheetBehavior = BottomSheetBehavior.from(bottom_sheet);
        if (ESPApplication.getInstance().isComponent || ESPApplication.getInstance()?.user?.loginResponse?.role?.toLowerCase(Locale.getDefault()) == Enums.applicant.toString()) {
            if (Shared.getInstance().isWifiConnected(bContext)) {
                loadDefinations()
            } else {
                Shared.getInstance().showAlertMessage(getString(R.string.internet_error_heading), getString(R.string.internet_connection_error), bContext)
            }
        }

        rlbottomSheetHeader.setOnClickListener {
            if (sheetBehavior?.state != BottomSheetBehavior.STATE_EXPANDED) {
                sheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED;
                add_account.visibility = View.GONE
                ivarrow.setImageResource(R.drawable.ic_arrow_down)
                rlbottomSheetHeader.setBackgroundResource(R.drawable.draw_bg_simple_green)
            } else {
                sheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED;
                ivarrow.setImageResource(R.drawable.ic_arrow_up)
                add_account.visibility = View.VISIBLE
                rlbottomSheetHeader.setBackgroundResource(R.drawable.draw_bg_submission_request)
            }
        }



        if (sub_defination_list?.scrollState == RecyclerView.SCROLL_STATE_DRAGGING)
            isClickEnable=false
        else if (sub_defination_list?.scrollState == RecyclerView.SCROLL_STATE_IDLE)
            isClickEnable=true

        sheetBehavior?.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            @SuppressLint("RestrictedApi")
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_DRAGGING -> {
                        CustomLogs.displayLogs("$ACTIVITY_NAME STATE_DRAGGING")
                        rlbottomSheetHeader.setBackgroundResource(R.drawable.draw_bg_simple_green)
                        add_account.visibility = View.GONE
                        sheetBehavior?.setState(BottomSheetBehavior.STATE_EXPANDED);
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }
        })

        ivfilter.setOnClickListener { view ->
            val i = Intent(this, FilterActivity::class.java)
            i.putExtra("subDefinationsDAOFilteredList", subDefinationsDAOFilteredList)
            i.putExtra("isSubmissionFilter", true)
            startActivity(i)
        }

        etxtsearch?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                if (s.isEmpty()) {
                    mDefAdapter = ListApplicationSubDefinationAdapter(filter_definition_list, context!!,sub_defination_list)
                    sub_defination_list.adapter = mDefAdapter

                } else {
                    mDefAdapter?.filter?.filter(s)
                }


            }

            override fun afterTextChanged(s: Editable) {
                listcount.text = mDefAdapter?.itemCount.toString() + " " + pref?.getlabels()?.submissionRequests
            }
        })


        add_account.setOnClickListener { v ->
            val manager = supportFragmentManager
            if (ESPApplication.getInstance()?.user?.profileStatus == null || ESPApplication.getInstance()?.user?.profileStatus == getString(R.string.profile_complete)) {
                Shared.getInstance().callIntentWithResult(AddApplicationsActivity::class.java, context, null, 2)
            } else if (ESPApplication.getInstance().user.profileStatus == getString(R.string.profile_incomplete)) {
                Shared.getInstance().showAlertProfileMessage(getString(R.string.profile_error_heading), getString(R.string.profile_error_desc), context)
            } else if (ESPApplication.getInstance().user.profileStatus == getString(R.string.profile_incomplete_admin)) {
                Shared.getInstance().showAlertProfileMessage(getString(R.string.profile_error_heading), getString(R.string.profile_error_desc_admin), context)
            }
        }

        filiter.setOnClickListener { v ->
            /*if (submit_request?.app_actual_list.isNullOrEmpty())
                Shared.getInstance().showAlertMessage(getString(R.string.error), getString(R.string.norecordstofilter), context)
            else*/
            Shared.getInstance().callIntentWithResult(FilterScreenActivity::class.java, context, null, 2)
        }

        toolbar_heading.setText(pref?.getlabels()?.applications)

        /*if (ESPApplication.getInstance()?.user?.loginResponse?.role?.toLowerCase() == getString(R.string.applicantsmall)) {
            filiter.visibility = View.GONE
            toolbar_heading.setText(pref?.getlabels()?.applications)
        } else {
            toolbar_heading.setText(context?.getResources()?.getString(R.string.my_space))
        }*/

        fm = this.context?.getSupportFragmentManager()

        request_fragment_search.visibility = View.GONE


        val submit_request_tabs = ApplicationActivityTabs.newInstance()
        val ft = fm!!.beginTransaction()
        ft.add(R.id.request_fragment, submit_request_tabs)
        ft.commit()

        /* if (ESPApplication.getInstance()?.user?.loginResponse?.role?.toLowerCase() == getString(R.string.applicantsmall)) {
             val submit_request = ApplicationActivityTabs.newInstance()
             val ft = fm!!.beginTransaction()
             ft.add(R.id.request_fragment, submit_request)
             ft.commit()
         } else {
             submit_request = UsersApplicationsFragment.newInstance()
             val ft = fm!!.beginTransaction()
             ft.add(R.id.request_fragment, submit_request!!)
             ft.commit()
         }
 */




        submit_request_search = UsersSearchApplicationsFragment.newInstance()
        val ft_search = fm!!.beginTransaction()
        ft_search.add(R.id.request_fragment_search, submit_request_search!!)
        ft_search.commit()

        mNavigationDrawerFragment = supportFragmentManager.findFragmentById(R.id.navigation_drawer) as NavigationDrawerFragment?
        mNavigationDrawerFragment?.setUp(R.id.navigation_drawer, findViewById<View>(R.id.drawer_layout) as androidx.drawerlayout.widget.DrawerLayout)


    }

    private fun initailize() {
        context = this@ApplicationsActivityDrawer
        pref = SharedPreference(context)
        pDialog = Shared.getInstance().setProgressDialog(bContext)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowHomeEnabled(false)
        supportActionBar!!.title = ""
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_nav_menu)
        toolbar.navigationIcon?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        toolbar.setNavigationOnClickListener { mNavigationDrawerFragment?.DrawerOpen() }

        val mDefLayoutManager = LinearLayoutManager(this)
        sub_defination_list?.setHasFixedSize(true)
        sub_defination_list?.layoutManager = mDefLayoutManager
        sub_defination_list?.itemAnimator = DefaultItemAnimator()
        sub_defination_list?.isNestedScrollingEnabled = true

        sub_defination_list?.setOnTouchListener { v, event ->

            v.parent.requestDisallowInterceptTouchEvent(true)
            v.onTouchEvent(event)
            true
        }

        txtsubmissionrequest.text = pref?.getlabels()?.submissionRequests
        filter_horizontal_list?.setHasFixedSize(true)
        filter_horizontal_list.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        filter_horizontal_list.itemAnimator = DefaultItemAnimator()


    }




    fun loadDefinations() {

        //start_loading_animation()
        /* APIs Mapping respective Object*/
        val apis = Shared.getInstance().retroFitObject(bContext)
        //  def_call = apis.AllDefincations(categoryId);
        val def_call = apis.getSubDefinitionList()
        def_call.enqueue(object : Callback<List<CategoryAndDefinationsDAO>> {
            @SuppressLint("RestrictedApi")
            override fun onResponse(call: Call<List<CategoryAndDefinationsDAO>>, response: Response<List<CategoryAndDefinationsDAO>>) {
                //stop_loading_animation()
                if (response.body() != null && response.body().size > 0) {
                    definition_list.clear()
                    filter_definition_list.clear()
                    val body = response.body()
                    definition_list.addAll(body)

                    filter_definition_list.addAll(definition_list)
                    mDefAdapter = ListApplicationSubDefinationAdapter(filter_definition_list, context!!, sub_defination_list)
                    sub_defination_list.adapter = mDefAdapter
                    //   ViewCompat.setNestedScrollingEnabled(sub_defination_list, true);
                    listcount.text = mDefAdapter?.getItemCount().toString() + " " + pref?.getlabels()?.submissionRequests
                    txtsubmissionrequest.text = pref?.getlabels()?.submissionRequests + " (" + body.size + ")"


                    if (ESPApplication.getInstance().isComponent || ESPApplication.getInstance()?.user?.loginResponse?.role?.toLowerCase(Locale.getDefault()) == Enums.applicant.toString()) {

                        setLayoutMargin(resources.getDimensionPixelSize(R.dimen._55sdp), resources.getDimensionPixelSize(R.dimen._50sdp))

                        bottom_sheet.visibility = View.VISIBLE
                    }


                } else {
                    bottom_sheet.visibility = View.GONE
                    setLayoutMargin(resources.getDimensionPixelSize(R.dimen._15sdp), 0)


                }
            }

            override fun onFailure(call: Call<List<CategoryAndDefinationsDAO>>, t: Throwable) {
                Shared.getInstance().messageBox(getString(R.string.some_thing_went_wrong), bContext)
                // stop_loading_animation()
                // UnSuccessResponse()
            }
        })

    }//

    private fun setLayoutMargin(floatingButtonMargin: Int, LayoutMargin: Int) {
        val lp = add_account.layoutParams as CoordinatorLayout.LayoutParams
        lp.setMargins(0, 0, 20, floatingButtonMargin)
        add_account.layoutParams = lp
        val lpp = request_fragment.layoutParams as LinearLayout.LayoutParams
        lpp.setMargins(0, 0, 0, LayoutMargin)
        request_fragment.layoutParams = lpp
    }

    private fun start_loading_animation() {
        if (pDialog != null) {
            if (!pDialog!!.isShowing())
                pDialog?.show()
        }

    }

    private fun stop_loading_animation() {

        if (pDialog != null) {
            if (pDialog!!.isShowing())
                pDialog?.dismiss()
        }

    }

    override fun onResume() {

        try {
            if (ESPApplication.getInstance().filter.isFilterApplied) {
                filter_count_view.visibility = View.GONE
            } else {
                filter_count_view.visibility = View.GONE
            }
        } catch (e: java.lang.Exception) {
        }

        if (Constants.isApplicationChagned) {
            if (submit_request != null) {
                submit_request?.reLoadApplications()
                Constants.isApplicationChagned = false
            }
        }





        filter_adapter = FilterItemsAdapter(subDefinationsDAOFilteredList, context!!)
        filter_adapter?.setActivitContext(this)
        filter_horizontal_list.adapter = filter_adapter
        populateFilters()
        etxtsearch.setText("")

        super.onResume()
    }


    private fun populateFilters() {
        filter_definition_list.clear()
        if (subDefinationsDAOFilteredList.size > 0) {
            ivfilter.setColorFilter(ContextCompat.getColor(context!!, R.color.green), android.graphics.PorterDuff.Mode.MULTIPLY)
            filter_horizontal_list.visibility = View.VISIBLE

        } else {
            ivfilter.setColorFilter(ContextCompat.getColor(context!!, R.color.grey), android.graphics.PorterDuff.Mode.MULTIPLY)
            filter_horizontal_list.visibility = View.GONE
            filter_definition_list.addAll(definition_list)
        }

        val categoriesIds = ArrayList<Int>()
        for (i in subDefinationsDAOFilteredList.indices) {
            val df = subDefinationsDAOFilteredList.get(i) as CategoryAndDefinationsDAO
            categoriesIds.add(df.id)

            for (h in definition_list.indices) {
                if (definition_list.get(h).id == df.id) {
                    filter_definition_list.add(definition_list.get(h))
                }
            }

        }

        mDefAdapter = ListApplicationSubDefinationAdapter(filter_definition_list, context!!, sub_defination_list)
        sub_defination_list.adapter = mDefAdapter
        listcount.text = mDefAdapter?.itemCount.toString() + " " + pref?.getlabels()?.submissionRequests

    }

    override fun onBackPressed() {
        super.onBackPressed()
        subDefinationsDAOFilteredList.clear()
    }

    override fun onPause() {
        super.onPause()

    }




    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        //Filter applied
        if (requestCode == 2) {

            if (data != null) {
                val bnd = data.extras

                if (bnd != null) {
                    if (bnd.getBoolean("whatodo")) {

                        if (submit_request != null) {
                            submit_request?.reLoadApplications()
                        }

                        if (ESPApplication.getInstance().filter.isFilterApplied) {
                            filter_count_view.visibility = View.GONE
                        } else {
                            filter_count_view.visibility = View.GONE
                        }

                    }
                }
            }
        } else if (requestCode == 5) {

            if (submit_request != null) {
                submit_request?.GetProfileStatus()
            }
        }


        super.onActivityResult(requestCode, resultCode, data)

    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_my_applications, menu)
        myActionMenuItem = menu.findItem(R.id.action_search)



        searchView = myActionMenuItem?.actionView as SearchView
        val editText = searchView?.findViewById<View>(androidx.appcompat.R.id.search_src_text) as SearchAutoComplete
        editText.hint = resources.getString(R.string.search_) + " " + pref?.getlabels()?.applications
        editText.setHintTextColor(ContextCompat.getColor(context!!, R.color.white))
        editText.setTextColor(ContextCompat.getColor(context!!, R.color.white))


        /* try {

             val mCursorDrawableRes = TextView::class.java.getDeclaredField("mCursorDrawableRes")
             mCursorDrawableRes.isAccessible = true
             mCursorDrawableRes.set(editText, 0) //This sets the cursor resource ID to 0 or @null which will make it visible on white background
         } catch (ex: java.lang.Exception) {
         }*/

        val searchClose = searchView?.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
        searchClose?.setImageResource(R.drawable.ic_icons_close_white)
        searchClose?.setOnClickListener {
            if (myActionMenuItem != null) {
                myActionMenuItem?.collapseActionView()
            }
            // submit_request.ResetLoadApplications();
        }


        //ImageView searchBack = (ImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);


        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String): Boolean {
                if (!searchView!!.isIconified) {
                    searchView?.isIconified = false
                }

                /* if (myActionMenuItem != null) {
                     myActionMenuItem!!.collapseActionView()
                 }*/
                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                if (query != null) {

                    try {

                        if (submit_request_search != null) {
                            submit_request_search?.UpdateViewAutoSearched(query)
                        }

                    } catch (e: Exception) {
                    }
                }
                return false
            }
        })

        myActionMenuItem?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {

            @SuppressLint("RestrictedApi")
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {

                submit_request_search?.app_search_list?.stopScroll()

                Handler().postDelayed({
                    submit_request_search?.app_actual_list?.clear()
                    submit_request_search?.search_result?.text = ""
                    submit_request_search?.txtrequestcount?.text = ""
                    submit_request_search?.mApplicationAdapter = null
                    request_fragment_search.visibility = View.VISIBLE
                    if (ESPApplication.getInstance()?.user?.loginResponse?.role?.toLowerCase(Locale.getDefault()) == Enums.applicant.toString()) {
                        if (bottom_sheet.visibility == View.VISIBLE) {
                            wasSheetShowing = true
                            bottom_sheet.visibility = View.GONE
                            add_account.visibility = View.GONE
                        }
                    }
                }, 300)


                return true
            }

            @SuppressLint("RestrictedApi")
            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                request_fragment_search.visibility = View.GONE
                if (ESPApplication.getInstance()?.user?.loginResponse?.role?.toLowerCase(Locale.getDefault()) == Enums.applicant.toString()) {


                    /* submit_request = UsersApplicationsFragment.newInstance()
                     if (!submit_request?.app_actual_list.isNullOrEmpty()) {*/
                    if (wasSheetShowing) {
                        wasSheetShowing = false
                        add_account.visibility = View.VISIBLE
                        bottom_sheet.visibility = View.VISIBLE
                    }
                    //  }
                }
                return true
            }
        })

        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_filter -> {
                Shared.getInstance().callIntentWithResult(FilterScreenActivity::class.java, context, null, 2)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setGravity() {
        if (pref?.language.equals("ar", ignoreCase = true)) {
            toolbar_heading.gravity = Gravity.RIGHT
            filter_count_value.gravity = Gravity.RIGHT

        } else {
            toolbar_heading.gravity = Gravity.LEFT
            filter_count_value.gravity = Gravity.LEFT
        }
    }

    companion object {

        var isClickEnable=false
        var ACTIVITY_NAME = "controllers.applications.ApplicationsActivityDrawer"
        var subDefinationsDAOFilteredList: ArrayList<CategoryAndDefinationsDAO> = ArrayList()
    }

    override fun deleteFilters(filtersList: CategoryAndDefinationsDAO) {
        if (Shared.getInstance().isWifiConnected(bContext)) {
            if (filter_adapter != null) {
                subDefinationsDAOFilteredList.remove(filtersList)
                filter_adapter?.notifyDataSetChanged()
                populateFilters()
            }
        } else {
            Shared.getInstance().showAlertMessage(getString(R.string.internet_error_heading), getString(R.string.internet_connection_error), bContext)
        }
    }

    override fun refreshSubDefinition() {

        if (filter_horizontal_list.visibility == View.GONE && request_fragment_search.visibility == View.GONE) {
            loadDefinations()
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




}
