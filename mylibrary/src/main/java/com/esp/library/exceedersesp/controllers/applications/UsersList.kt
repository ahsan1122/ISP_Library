package com.esp.library.exceedersesp.controllers.applications

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import com.esp.library.R
import com.esp.library.exceedersesp.ESPApplication
import com.esp.library.utilities.common.Constants
import com.esp.library.utilities.common.Shared
import com.esp.library.utilities.common.SharedPreference
import com.esp.library.exceedersesp.BaseActivity
import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_users_list.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import utilities.adapters.setup.applications.ListUsersAdapter
import utilities.data.apis.APIs
import utilities.data.applicants.ApplicationSingleton
import utilities.data.applicants.UsersListDAO
import utilities.data.applicants.dynamics.DynamicStagesCriteriaListDAO
import utilities.interfaces.UserListClickListener
import java.util.concurrent.TimeUnit

class UsersList : BaseActivity(), UserListClickListener {


    internal var context: BaseActivity? = null
    internal var pDialog: AlertDialog? = null
    var pref: SharedPreference? = null
    var dynamicStagesCriteriaListDAO: DynamicStagesCriteriaListDAO? = null
    var userAdapter: ListUsersAdapter? = null
    val userList = ArrayList<UsersListDAO>()
    internal var searchView: SearchView? = null
    internal var myActionMenuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users_list)
        initailize()


        if (Shared.getInstance().isWifiConnected(context)) {
            loadUsersList()
        } else {
            Shared.getInstance().showAlertMessage(context?.getString(R.string.internet_error_heading), context?.getString(R.string.internet_connection_error), context)
        }

    }

    private fun initailize() {
        context = this@UsersList
        pref = SharedPreference(context)
        pDialog = Shared.getInstance().setProgressDialog(context)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowHomeEnabled(false)
        supportActionBar!!.title = ""
        toolbar.navigationIcon = ContextCompat.getDrawable(context!!, R.drawable.ic_nav_back)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        toolbar.navigationIcon!!.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)

        dynamicStagesCriteriaListDAO = intent.getSerializableExtra("criteriaListDAO") as DynamicStagesCriteriaListDAO

        rvUsersList.setHasFixedSize(true)
        rvUsersList.isNestedScrollingEnabled = false
        val linearLayoutManagerCrteria = androidx.recyclerview.widget.LinearLayoutManager(context, androidx.recyclerview.widget.LinearLayoutManager.VERTICAL, false)
        rvUsersList.layoutManager = linearLayoutManagerCrteria
        val dividerItemDecoration = androidx.recyclerview.widget.DividerItemDecoration(rvUsersList.getContext(),
                linearLayoutManagerCrteria.getOrientation())
        rvUsersList.addItemDecoration(dividerItemDecoration)


    }

    fun loadUsersList() {

        start_loading_animation()
        try {

            var call = Shared.getInstance().retroFitObject(context).getUser()

            call.enqueue(object : Callback<List<UsersListDAO>> {
                override fun onResponse(call: Call<List<UsersListDAO>>, response: Response<List<UsersListDAO>>) {


                    if (response.body() != null && response.body().size > 0) {

                        txtnorecords.visibility = View.GONE
                        rvUsersList.visibility = View.VISIBLE
                        val body = response.body()

                        for (i in 0 until body.size) {
                            if (dynamicStagesCriteriaListDAO?.ownerId != body.get(i).id)
                                userList.add(body.get(i))
                        }

                        userAdapter = ListUsersAdapter(userList, context!!, "")
                        rvUsersList.adapter = userAdapter


                    } else {

                        txtnorecords.visibility = View.VISIBLE
                        rvUsersList.visibility = View.GONE
                    }
                    stop_loading_animation()

                }


                override fun onFailure(call: Call<List<UsersListDAO>>, t: Throwable) {
                    Shared.getInstance().messageBox(t.message, context)
                    stop_loading_animation()

                }
            })

        } catch (ex: Exception) {
            stop_loading_animation()

            Shared.getInstance().messageBox(ex.message, context)

        }

    }

    fun sendReAssignData(post: DynamicStagesCriteriaListDAO?, ownerId: Int?) {

        start_loading_animation()

        try {

            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY
            val httpClient = OkHttpClient.Builder()


            httpClient.addInterceptor { chain ->
                val original = chain.request()
                val originalHttpUrl = original.url()

                val url = originalHttpUrl.newBuilder()
                        .addQueryParameter("applicationId", ApplicationSingleton.instace.application?.applicationId.toString())
                        .addQueryParameter("newOwnerId", ownerId.toString())
                        .build()

                val requestBuilder = original.newBuilder()
                        .url(url)
                        .header("locale", Shared.getInstance().getLanguage(context))
                        .header("Authorization", "bearer " + ESPApplication.getInstance().user.loginResponse!!.access_token!!)
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
            /*  val gson = GsonBuilder()
                      .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                      .create()*/

            val gson = GsonBuilder()
                    .setExclusionStrategies(object : ExclusionStrategy {
                        override fun shouldSkipField(f: FieldAttributes): Boolean {
                            return f.getDeclaredClass().equals(DynamicStagesCriteriaListDAO::class.java)
                        }

                        override fun shouldSkipClass(clazz: Class<*>): Boolean {
                            return false
                        }
                    })
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                    .create()

            /* retrofit builder and call web service*/
            var retrofit = Retrofit.Builder()
                    .baseUrl(Constants.base_url)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(httpClient.build())
                    .build()

            /* APIs Mapping respective Object*/
            val apis = retrofit.create(APIs::class.java)


            //  var dynamicStagesCriteriaListDAO=setValues(post)


            var call = apis.reAssignData(post)

            call.enqueue(object : Callback<Any> {
                override fun onResponse(call: Call<Any>, response: Response<Any>) {


                    if (response.body() != null) {


                        ActivityStageDetails.isGoBAck = true
                        finish()

                    } else {


                    }
                    stop_loading_animation()

                }


                override fun onFailure(call: Call<Any>, t: Throwable) {
                    Shared.getInstance().messageBox(t.message, context)
                    stop_loading_animation()

                }
            })

        } catch (ex: Exception) {
            stop_loading_animation()
            Shared.getInstance().messageBox(ex.message, context)

        }

    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        myActionMenuItem = menu.findItem(R.id.action_search)

        searchView = myActionMenuItem!!.actionView as SearchView

        (searchView?.findViewById<View>(androidx.appcompat.R.id.search_src_text) as EditText).hint = resources.getString(R.string.search_)
        (searchView?.findViewById<View>(androidx.appcompat.R.id.search_src_text) as EditText).setHintTextColor(ContextCompat.getColor(context!!, R.color.white))
        (searchView?.findViewById<View>(androidx.appcompat.R.id.search_src_text) as EditText).setTextColor(ContextCompat.getColor(context!!, R.color.white))
        val searchClose = searchView?.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
        searchClose?.setImageResource(R.drawable.ic_nav_close)
        searchClose?.setOnClickListener {
            if (myActionMenuItem != null) {
                myActionMenuItem!!.collapseActionView()
            }
            // submit_request.ResetLoadApplications();
        }

        //ImageView searchBack = (ImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String): Boolean {
                if (!searchView!!.isIconified) {
                    searchView?.isIconified = false
                }


                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                if (query?.length == 0) {
                    userAdapter = ListUsersAdapter(userList, context!!, "")
                    rvUsersList.adapter = userAdapter
                } else
                    userAdapter?.filter?.filter(query);
                return false
            }
        })

        myActionMenuItem!!.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                return true
            }
        })

        return true
    }


    private fun start_loading_animation() {
        try {
            if (!pDialog!!.isShowing())
                pDialog?.show()
        } catch (e: java.lang.Exception) {
        }
    }

    private fun stop_loading_animation() {

        try {
            if (pDialog!!.isShowing())
                pDialog?.dismiss()
        } catch (e: java.lang.Exception) {
        }
    }

    override fun userClick(userslistDAO: UsersListDAO?) {
        if (Shared.getInstance().isWifiConnected(context)) {
            sendReAssignData(dynamicStagesCriteriaListDAO, userslistDAO?.id)
        } else {
            Shared.getInstance().showAlertMessage(context?.getString(R.string.internet_error_heading), context?.getString(R.string.internet_connection_error), context)
        }
    }

    override fun onBackPressed() {
        ActivityStageDetails.isGoBAck = false
        super.onBackPressed()
    }

}
