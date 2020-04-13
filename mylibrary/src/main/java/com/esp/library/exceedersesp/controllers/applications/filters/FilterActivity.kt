package com.esp.library.exceedersesp.controllers.applications.filters

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.esp.library.R
import com.esp.library.exceedersesp.fragments.applications.AddApplicationCategoryAndDefinationsFragment
import com.esp.library.utilities.common.Shared
import com.esp.library.exceedersesp.BaseActivity
import com.esp.library.exceedersesp.controllers.applications.ApplicationsActivityDrawer
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlinx.android.synthetic.main.activity_filter.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import utilities.adapters.setup.applications.ListApplicationCategoryAdapter
import utilities.data.applicants.addapplication.CategoryAndDefinationsDAO
import utilities.interfaces.CheckFilterSelection

class FilterActivity : BaseActivity(), CheckFilterSelection {


    var mCatLayoutManager: FlexboxLayoutManager? = null
    var context: BaseActivity? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter)
        initailize()


        val isSubmissionFilter = intent.getBooleanExtra("isSubmissionFilter", false)

        if (Shared.getInstance().isWifiConnected(bContext)) {
            if (isSubmissionFilter)
                loadSubDefinations()
            else {


              /*  val actualResponse = intent.getSerializableExtra("actualResponse") as List<DefinationsCategoriesDAO>
                val getCategoryAndDefinationsDAOFilteredList = intent.getSerializableExtra("categoryAndDefinationsDAOFilteredList") as ArrayList<*>
                var categoryAndDefinations: List<CategoryAndDefinationsDAO>? = null
                var categoryAndDefinationsList = ArrayList<List<CategoryAndDefinationsDAO>>()
                for (i in 0 until actualResponse.size) {
                    categoryAndDefinations = actualResponse[i].definitions

                    for (j in 0 until getCategoryAndDefinationsDAOFilteredList.size) {
                        val df = getCategoryAndDefinationsDAOFilteredList.get(j) as CategoryAndDefinationsDAO

                        for (k in 0 until categoryAndDefinations!!.size) {
                            val categoryAndDefinationsDAO = categoryAndDefinations[k]
                            if (categoryAndDefinationsDAO.id == df.id)
                                categoryAndDefinationsDAO.isChecked = true
                        }
                    }
                    categoryAndDefinationsList.add(categoryAndDefinations!!)
                }

                val mCatAdapter = ListApplicationCategoryAdapter(categoryAndDefinations, context!!)
                category_list.adapter = mCatAdapter
                if (categoryAndDefinations != null) {
                    checkFilterSelection(categoryAndDefinations)
                }
                rlacceptapprove.visibility = View.VISIBLE
                llmainlayout.visibility = View.VISIBLE */

                loadCategories()
            }
        } else {
            Shared.getInstance().showAlertMessage(getString(R.string.internet_error_heading), getString(R.string.internet_connection_error), bContext)
        }


        btapplyfilter.setOnClickListener {
            if (isSubmissionFilter)
                ApplicationsActivityDrawer.subDefinationsDAOFilteredList = tempFilterSelectionValues
            else
                AddApplicationCategoryAndDefinationsFragment.categoryAndDefinationsDAOFilteredList = tempFilterSelectionValues
            finish()
        }
        btcancel.setOnClickListener {
            if (isSubmissionFilter)
                ApplicationsActivityDrawer.subDefinationsDAOFilteredList = tempFilterSelectionValues
            else
                AddApplicationCategoryAndDefinationsFragment.categoryAndDefinationsDAOFilteredList.clear()
            finish()
        }


    }

    private fun initailize() {
        context = this@FilterActivity
        setSupportActionBar(toolbar)
        supportActionBar?.setTitle("")
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_nav_back)
        toolbar.navigationIcon?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        toolbarheading.text = getString(R.string.filter)
        toolbar.setNavigationOnClickListener { finish() }


        mCatLayoutManager = FlexboxLayoutManager(this)
        mCatLayoutManager?.flexDirection = FlexDirection.ROW
        mCatLayoutManager?.justifyContent = JustifyContent.FLEX_START


        category_list.tag = getString(R.string.hidden)
        category_list.setHasFixedSize(true)
        category_list.layoutManager = mCatLayoutManager
        category_list.itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()
    }

    fun loadCategories() {
        val getCategoryAndDefinationsDAOFilteredList = intent.getSerializableExtra("categoryAndDefinationsDAOFilteredList") as ArrayList<*>
        start_loading_animation()
        /* APIs Mapping respective Object*/
        val apis = Shared.getInstance().retroFitObject(bContext)
        val cat_call = apis.AllCategories()
        cat_call.enqueue(object : Callback<List<CategoryAndDefinationsDAO>> {
            override fun onResponse(call: Call<List<CategoryAndDefinationsDAO>>, response: Response<List<CategoryAndDefinationsDAO>>) {
                if (response.body() != null && response.body().isNotEmpty()) {
                    val body = response.body()
                    for (i in 0 until body.size) {
                        val categoryAndDefinationsDAO = body[i]

                        for (j in 0 until getCategoryAndDefinationsDAOFilteredList.size) {
                            val df = getCategoryAndDefinationsDAOFilteredList.get(j) as CategoryAndDefinationsDAO
                            if (categoryAndDefinationsDAO.id == df.id)
                                categoryAndDefinationsDAO.isChecked = true
                        }

                    }

                    val mCatAdapter = ListApplicationCategoryAdapter(body, context!!)
                    category_list.adapter = mCatAdapter
                    checkFilterSelection(body)
                    rlacceptapprove.visibility = View.VISIBLE
                    llmainlayout.visibility = View.VISIBLE


                } else
                    Shared.getInstance().messageBox(getString(R.string.some_thing_went_wrong), bContext)

                stop_loading_animation()
            }

            override fun onFailure(call: Call<List<CategoryAndDefinationsDAO>>, t: Throwable) {
                Shared.getInstance().messageBox(t.message, bContext)
                stop_loading_animation()

            }
        })

    }

    fun loadSubDefinations() {
        val getsubDefinitionDAOFilteredList = intent.getSerializableExtra("subDefinationsDAOFilteredList") as ArrayList<*>
        start_loading_animation()
        val apis = Shared.getInstance().retroFitObject(bContext)
        val def_call = apis.getSubDefinitionList()
        def_call.enqueue(object : Callback<List<CategoryAndDefinationsDAO>> {
            override fun onResponse(call: Call<List<CategoryAndDefinationsDAO>>, response: Response<List<CategoryAndDefinationsDAO>>) {
                stop_loading_animation()
                if (response.body() != null && response.body().size > 0) {
                    val body = response.body()
                    val arrayListBody = ArrayList<CategoryAndDefinationsDAO>()//Creating an empty arraylist
                    for (i in 0 until body.size) {
                        val categoryAndDefinationsDAO = body[i]

                        for (j in 0 until getsubDefinitionDAOFilteredList.size) {
                            val df = getsubDefinitionDAOFilteredList.get(j) as CategoryAndDefinationsDAO
                            if (categoryAndDefinationsDAO.id == df.id)
                                categoryAndDefinationsDAO.isChecked = true
                        }


                    }

                    for (i in 0 until body.size) {
                        val getList = body.get(i);
                        val isArrayHasValue = arrayListBody.any { x -> x.id == getList.id }
                        if (!isArrayHasValue) {
                            arrayListBody.add(getList)
                        }
                    }


                    val mCatAdapter = ListApplicationCategoryAdapter(arrayListBody, context!!)
                    category_list.adapter = mCatAdapter
                    checkFilterSelection(body)
                    rlacceptapprove.visibility = View.VISIBLE
                    llmainlayout.visibility = View.VISIBLE


                } else {
                    Shared.getInstance().messageBox(getString(R.string.some_thing_went_wrong), bContext)
                }
            }

            override fun onFailure(call: Call<List<CategoryAndDefinationsDAO>>, t: Throwable) {
                Shared.getInstance().messageBox(getString(R.string.some_thing_went_wrong), bContext)
                stop_loading_animation()

            }
        })

    }//


    private fun start_loading_animation() {
        shimmer_view_container.visibility = View.VISIBLE
        vview.visibility = View.GONE
        shimmer_view_container.startShimmerAnimation()
    }

    private fun stop_loading_animation() {
        shimmer_view_container.visibility = View.GONE
        vview.visibility = View.VISIBLE
        shimmer_view_container.stopShimmerAnimation()
    }

    override fun checkFilterSelection(mApplications: List<CategoryAndDefinationsDAO>) {

        for (i in 0 until mApplications.size) {
            val categoryAndDefinationsDAO = mApplications[i]
            if (categoryAndDefinationsDAO.isChecked) {
                btapplyfilter.isEnabled = true
                btapplyfilter.alpha = 1f
                break
            } else {
                btapplyfilter.isEnabled = false
                btapplyfilter.alpha = 0.5f
            }
        }

    }

    companion object {
        var tempFilterSelectionValues = ArrayList<CategoryAndDefinationsDAO>();
    }

    override fun onBackPressed() {
        super.onBackPressed()
        tempFilterSelectionValues.clear()
    }


}