package com.esp.library.exceedersesp.fragments

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.esp.library.R
import com.esp.library.exceedersesp.ESPApplication
import com.esp.library.utilities.common.*
import com.esp.library.exceedersesp.BaseActivity
import com.esp.library.exceedersesp.controllers.Profile.ProfileMainActivity
import com.esp.library.exceedersesp.controllers.SplashScreenActivity
import com.esp.library.exceedersesp.controllers.lookupinfo.adapter.ListLookupInfoDAOAdapter
import com.esp.library.exceedersesp.controllers.setup.LoginScreenActivity
import com.esp.library.exceedersesp.fragments.setup.SelectOrganizationFragment
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import me.leolin.shortcutbadger.ShortcutBadger
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import utilities.adapters.setup.ListofOrganizationSectionsAdapter
import utilities.data.apis.APIs
import utilities.data.applicants.FirebaseTokenDAO
import utilities.data.applicants.profile.ApplicationProfileDAO
import utilities.data.lookup.LookupInfoListDAO
import utilities.data.setup.*
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * A simple [Fragment] subclass.
 */
class NavigationDrawerFragment : androidx.fragment.app.Fragment() {

    internal var TAG = javaClass.simpleName
    private var mDrawerToggle: ActionBarDrawerToggle? = null
    private var mDrawerLayout: androidx.drawerlayout.widget.DrawerLayout? = null
    private var mFragmentContainerView: View? = null
    private var mCurrentSelectedPosition = 0
    internal var fm: androidx.fragment.app.FragmentManager? = null
    internal var context: BaseActivity? = null
    internal var login_call: Call<TokenDAO>? = null
    internal var bManager: androidx.localbroadcastmanager.content.LocalBroadcastManager? = null
    internal var submit_request: SelectOrganizationFragment? = null
    internal var pref: SharedPreference? = null
    internal var arrayLanguages = ArrayList<String>()
    internal var isClick = false


    internal var profile_photo: ImageView? = null
    internal var name: TextView? = null
    internal var txtVersionName: TextView? = null
    internal var email: TextView? = null
    internal var org_name: TextView? = null
    internal var backBtn: ImageView? = null
    internal var app_nav_div: LinearLayout? = null
    internal var applications: TextView? = null
    internal var profile_nav_div: LinearLayout? = null
    internal var subbmisiion_nav_div: LinearLayout? = null
    internal var switchUser: LinearLayout? = null
    internal var role: TextView? = null
    internal var switchUserImg: ImageView? = null
    internal var splanguage: Spinner? = null
    internal var progressbar: ProgressBar? = null
    internal var menu_items: LinearLayout? = null
    internal var mToolbar: Toolbar? = null
    internal var org_list: androidx.recyclerview.widget.RecyclerView? = null
    internal var lookupList: androidx.recyclerview.widget.RecyclerView? = null
    internal var btsignout: Button? = null

    private var mOrganizeAdapter: androidx.recyclerview.widget.RecyclerView.Adapter<*>? = null
    private var orgLayoutManager: androidx.recyclerview.widget.RecyclerView.LayoutManager? = null
    private var loohupLayoutManager: androidx.recyclerview.widget.RecyclerView.LayoutManager? = null
    internal var personaDAOList: MutableList<OrganizationPersonaDao.Personas> = ArrayList()
    internal var lookupInfoListDAOArrayList: MutableList<LookupInfoListDAO> = ArrayList()

    private val bReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {}
    }


    val isDrawerOpen: Boolean
        get() = mDrawerLayout != null && mDrawerLayout!!.isDrawerOpen(mFragmentContainerView!!)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        bManager?.unregisterReceiver(bReceiver)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // the fragment has menu items to contribute
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_navigation_drawer, container, false)
        //   holder = new ViewHolder(v);
        initailize(v)

        UpdateView()
     //   populateSpinner();
        fm = context?.supportFragmentManager
       /* submit_request = ESPApplication.getInstance()?.user?.loginResponse?.let { SelectOrganizationFragment.newInstance(it) }
        val ft = fm?.beginTransaction()
        ft?.add(R.id.request_fragment_org, submit_request!!)
        ft?.commit()*/


        bManager = androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(context!!)
        val intentFilter = IntentFilter()
        // intentFilter.addAction(Constants.RECEIVE_MENU_BADGES);
        bManager?.registerReceiver(bReceiver, intentFilter)

        if (ESPApplication.getInstance().user != null) {
            name?.text = ESPApplication.getInstance()?.user?.loginResponse?.name
            email?.text = ESPApplication.getInstance()?.user?.loginResponse?.email
            email?.visibility = View.GONE
        }

        if (ESPApplication.getInstance().user != null && ESPApplication.getInstance()?.user?.loginResponse?.imageUrl != null && ESPApplication.getInstance()?.user?.loginResponse?.imageUrl!!.length > 0) {
            Picasso.with(context)
                    .load(ESPApplication.getInstance()?.user?.loginResponse?.imageUrl)
                    .placeholder(R.drawable.ic_contact_default)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .transform(RoundedPicasso())
                    .resize(Constants.PROFILE_PHOTO_SIZE, Constants.PROFILE_PHOTO_SIZE)
                    .into(profile_photo)


        }

        getLookupInfoList()


        backBtn?.setOnClickListener { view ->
            DrawerClose()
        }
        app_nav_div?.setOnClickListener { view -> Shared.getInstance().DrawermenuAction(0, "", context) }

        btsignout?.setOnClickListener {

            if (Shared.getInstance().isWifiConnected(context)) {
                deleteFirebaseToken()
            } else {
                Shared.getInstance().showAlertMessage(getString(R.string.internet_error_heading), getString(R.string.internet_connection_error), context)

            }


        }

        profile_nav_div?.setOnClickListener { view -> getApplicant() }

        /*subbmisiion_nav_div?.setOnClickListener { view -> }*/

        splanguage?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                //changeLanguage(arrayLanguages.get(i));

            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {

            }
        }


        return v
        //selectItem(position);

    }

    private fun initailize(v: View) {
        context = activity as BaseActivity?
        pref = SharedPreference(context)

        mToolbar = v.findViewById(R.id.toolbar)
        mToolbar?.navigationIcon?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)


        txtVersionName = v.findViewById(R.id.txtVersionName)
        btsignout = v.findViewById(R.id.btsignout)
        lookupList = v.findViewById(R.id.rclist)
        org_list = v.findViewById(R.id.org_list)
        menu_items = v.findViewById(R.id.menu_items)
        progressbar = v.findViewById(R.id.progressbar)
        switchUserImg = v.findViewById(R.id.switchUserImg)
        splanguage = v.findViewById(R.id.splanguage)
        switchUser = v.findViewById(R.id.switchUser)
        role = v.findViewById(R.id.role)
        profile_nav_div = v.findViewById(R.id.profile_nav_div)
        subbmisiion_nav_div = v.findViewById(R.id.subbmisiion_nav_div)
        applications = v.findViewById(R.id.applications)
        app_nav_div = v.findViewById(R.id.app_nav_div)
        org_name = v.findViewById(R.id.org_name)
        backBtn = v.findViewById(R.id.back_btn)
        name = v.findViewById(R.id.user_name)
        email = v.findViewById(R.id.user_email)
        profile_photo = v.findViewById(R.id.profile_photo)


        mToolbar?.visibility = View.GONE
        switchUser?.tag = getString(R.string.hidden)

        orgLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
        loohupLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
        org_list?.setHasFixedSize(true)
        org_list?.layoutManager = orgLayoutManager
        org_list?.itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()

        lookupList?.setHasFixedSize(true)
        lookupList?.layoutManager = loohupLayoutManager
        lookupList?.itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()

        if (pref?.getlabels() != null)
            applications?.text = pref?.getlabels()?.applications

        if (!Shared.getInstance().getVersionName(context).toString().isNullOrEmpty())
            txtVersionName?.text = getString(R.string.version) + " " + Shared.getInstance().getVersionName(context)
    }

    fun getLookupInfoList() {

        try {

            start_loading_animation()
            val apis = Shared.getInstance().retroFitObject(context)
            val labels_call = apis.lookupInfoList

            labels_call.enqueue(object : Callback<List<LookupInfoListDAO>> {
                override fun onResponse(call: Call<List<LookupInfoListDAO>>, response: Response<List<LookupInfoListDAO>>) {
                    stop_loading_animation()
                    if (response.body() != null) {
                        val body = response.body()
                        lookupInfoListDAOArrayList.clear()
                        for (i in body.indices) {
                            val lookupInfoListDAO = body[i]
                            if (lookupInfoListDAO.isShowToApplicant && lookupInfoListDAO.isVariable &&
                                    lookupInfoListDAO.isVisible) {
                                lookupInfoListDAOArrayList.add(lookupInfoListDAO)
                            }
                        }
                        val adapter = ListLookupInfoDAOAdapter(lookupInfoListDAOArrayList, context!!)
                        lookupList?.adapter = adapter
                    }
                }

                override fun onFailure(call: Call<List<LookupInfoListDAO>>, t: Throwable) {
                    stop_loading_animation()
                    t.printStackTrace()
                    //  Shared.getInstance().messageBox(getString(R.string.some_thing_went_wrong), context)
                }
            })


        } catch (ex: Exception) {
            ex.printStackTrace()
            stop_loading_animation()
            //  Shared.getInstance().messageBox(getString(R.string.some_thing_went_wrong), context)
        }

    }


    fun getApplicant() {

        try {

            start_loading_animation()
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY
            val httpClient = OkHttpClient.Builder()
            httpClient.addInterceptor(logging)

            httpClient.addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                        .header("Authorization", "bearer " + ESPApplication.getInstance()?.user?.loginResponse?.access_token)
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
                    applicationDAO = response.body()

                    val mainIntent = Intent(context, ProfileMainActivity::class.java)
                    mainIntent.putExtra("dataapplicant", body)
                    mainIntent.putExtra("ischeckerror", true)
                    mainIntent.putExtra("isprofile", true)
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

    }

    private fun UpdateView() {

        if (ESPApplication.getInstance()?.user == null) {
            val intentToBeNewRoot = Intent(context, LoginScreenActivity::class.java)
            val cn = intentToBeNewRoot.component
            val mainIntent = Intent.makeRestartActivityTask(cn)
            startActivity(mainIntent)
        } else {
            if (ESPApplication.getInstance()?.user?.loginResponse?.role?.toLowerCase(Locale.getDefault()).equals(Enums.applicant.toString(), ignoreCase = true)) {
                //   role?.text = ESPApplication.getInstance()?.user?.loginResponse?.role
            } else {
                // role?.text = context?.resources?.getString(R.string.assessor)
                profile_nav_div?.visibility = View.GONE
            }

            role?.text = pref?.selectedUserRole


            org_name?.text = Shared.getInstance()?.toSubStr(Shared.getInstance()?.GetOrgName(), 20)


            val list: List<PersonaDAO>?
            val personas = ESPApplication.getInstance()?.user?.loginResponse?.personas
            val gson = Gson()
            list = gson.fromJson<List<PersonaDAO>>(personas, object : TypeToken<List<PersonaDAO>>() {

            }.type)
            var app_list: List<PersonaDAO>? = null
            if (list != null && list.size > 0) {
                app_list = Shared.getInstance()?.GetApplicantOrganization(list,context)
                val organizationId = ESPApplication.getInstance()?.user?.loginResponse?.organizationId
                for (i in app_list!!.indices) {
                    val orgId = app_list[i].orgId
                    if (organizationId!!.equals(orgId, ignoreCase = true)) {
                        pref?.saveLocales(app_list[i].locales)
                    }
                }

            }


            if (app_list != null && app_list.size > 1) {
                switchUser?.isEnabled = true
                switchUserImg?.visibility = View.VISIBLE

                switchUser?.setOnClickListener { v -> showHideOrganiztions() }
                switchUserImg?.setOnClickListener { v -> showHideOrganiztions() }


            } else {
                switchUser?.isEnabled = false
                switchUserImg?.visibility = View.GONE
            }
        }
    }

    fun DrawerOpen() {

        mDrawerLayout?.openDrawer(GravityCompat.START)

    }

    private fun populateSpinner() {
        val localesArray = ArrayList(Arrays.asList(*pref!!.locales.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))
        arrayLanguages.clear()
        if (localesArray.size > 1) {
            splanguage?.visibility = View.VISIBLE

            for (i in localesArray.indices) {
                if (localesArray[i].equals("en", ignoreCase = true))
                    arrayLanguages.add(getString(R.string.english))
                else
                    arrayLanguages.add(getString(R.string.arabic))
            }

            val adapter = ArrayAdapter(context!!, android.R.layout.simple_list_item_1, arrayLanguages)
            splanguage?.adapter = adapter
        } else {
            splanguage?.visibility = View.GONE
        }

        if (pref?.language.equals("en", ignoreCase = true))
            splanguage?.setSelection(0)
        else if (pref?.language.equals("ar", ignoreCase = true))
            splanguage?.setSelection(1)


    }

    private fun changeLanguage(languageType: String) {
        val language: String

        if (languageType.equals(getString(R.string.arabic), ignoreCase = true))
            language = "ar"
        else
            language = "en"


        pref?.savelanguage(language)
        CustomLogs.displayLogs(TAG + " language: " + language + " getLanguage: " + pref?.language)
        if (isDrawerOpen)
            isClick = true
        DrawerClose()

    }

    fun setApplicationlanguage() {
        val res = resources
        val dm = res.displayMetrics
        val conf = res.configuration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            conf.setLocale(Locale(pref?.language)) // API 17+ only.
        } else {
            conf.locale = Locale(pref?.language)
        }
        res.updateConfiguration(conf, dm)
        isClick = false

        val intentToBeNewRoot = Intent(context, SplashScreenActivity::class.java)
        val cn = intentToBeNewRoot.component
        val mainIntent = Intent.makeRestartActivityTask(cn)
        startActivity(mainIntent)

    }

    fun DrawerClose() {
        if (mDrawerLayout?.isDrawerOpen(GravityCompat.START)!!) {
            mDrawerLayout?.closeDrawer(GravityCompat.START)
        }
    }

    fun setUp(fragmentId: Int, drawerLayout: androidx.drawerlayout.widget.DrawerLayout) {
        mFragmentContainerView = activity?.findViewById(fragmentId)
        mDrawerLayout = drawerLayout

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout?.setDrawerShadow(R.drawable.draw_shadow, GravityCompat.START)

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = object : ActionBarDrawerToggle(activity, mDrawerLayout, mToolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close) {
            override fun onDrawerOpened(drawerView: View) {
                if (drawerView != null) {
                    super.onDrawerOpened(drawerView)
                }

            }

            override fun onDrawerClosed(drawerView: View) {
                if (drawerView != null) {
                    super.onDrawerClosed(drawerView)
                }
                if (isClick)
                    setApplicationlanguage()
            }
        }

        mDrawerLayout?.post { mDrawerToggle?.syncState() }
        mDrawerLayout?.addDrawerListener(mDrawerToggle!!)
        mDrawerToggle?.isDrawerIndicatorEnabled = true
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mDrawerToggle?.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (mDrawerToggle!!.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)
    }


    private fun getOrganizations() {

        start_loading_animation()

        try {
            val apis = Shared.getInstance().retroFitObject(context)
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

                        var tokenDAO = TokenDAO()
                        tokenDAO.access_token = ESPApplication.getInstance()?.user?.loginResponse?.access_token
                        tokenDAO.refresh_token = ESPApplication.getInstance()?.user?.loginResponse?.refresh_token

                        mOrganizeAdapter = ListofOrganizationSectionsAdapter(body, context!!, tokenDAO)
                        org_list?.adapter = mOrganizeAdapter
                        mOrganizeAdapter?.notifyDataSetChanged()

                        org_list?.visibility = View.VISIBLE
                        menu_items?.visibility = View.GONE
                        lookupList?.visibility = View.GONE

                    }
                    stop_loading_animation()
                }

                override fun onFailure(call: Call<List<OrganizationPersonaDao>>, t: Throwable) {
                    stop_loading_animation()
                    Shared.getInstance().showAlertMessage(getString(R.string.login_label), getString(R.string.some_thing_went_wrong), context)
                }
            })


        } catch (ex: Exception) {

            ex.printStackTrace()
            stop_loading_animation()
            Shared.getInstance().showAlertMessage(getString(R.string.login_label), getString(R.string.some_thing_went_wrong), context)


        }

    }

    private fun showHideOrganiztions() {

        val tag = switchUser?.tag as String

        if (tag != null) {

            if (tag.equals(getString(R.string.hidden), ignoreCase = true)) {

                switchUser?.tag = getString(R.string.shown)
                switchUserImg?.setImageDrawable(context?.resources?.getDrawable(R.drawable.ic_arrow_up))

                //  request_fragment_org.setVisibility(View.VISIBLE);


                val scopeId = Shared.getInstance().ReadPref("scropId", "login_info", context)
                val pt = PostTokenDAO()
                pt.client_id = "ESPMobile"
                pt.grant_type = "refresh_token"
                pt.password = ESPApplication.getInstance()?.user?.loginResponse?.password
                pt.username = ESPApplication.getInstance()?.user?.loginResponse?.email
                pt.scope = scopeId
                pt.refresh_token = ESPApplication.getInstance()?.user?.loginResponse?.refresh_token
                GetRefreshToken(pt, true)


            } else {

                switchUser?.tag = getString(R.string.hidden)
                switchUserImg?.setImageDrawable(context?.resources?.getDrawable(R.drawable.ic_arrow_down))

                // request_fragment_org.setVisibility(View.GONE);
                org_list?.visibility = View.GONE
                menu_items?.visibility = View.VISIBLE
                lookupList?.visibility = View.VISIBLE
            }
        }

    }

    //If multiple Organation for Applicant
    fun RefreshToken(personaDAO: PersonaDAO) {


        if (Shared.getInstance().isWifiConnected(context)) {

            val pt = PostTokenDAO()
            pt.client_id = "ESPMobile"
            pt.grant_type = "refresh_token"
            pt.password = ESPApplication.getInstance()?.user?.loginResponse?.password
            pt.username = ESPApplication.getInstance()?.user?.loginResponse?.email
            pt.scope = personaDAO.id
            pt.refresh_token = personaDAO.refresh_token

            Shared.getInstance().WritePref("scropId", personaDAO.id, "login_info", context)


            GetRefreshToken(pt, false)

        } else {
            Shared.getInstance().showAlertMessage(getString(R.string.internet_error_heading), getString(R.string.internet_connection_error), context)

        }
    }

    private fun GetRefreshToken(postTokenDAO: PostTokenDAO, isOrganizationCall: Boolean) {

        start_loading_animation()

        try {
            val apis = Shared.getInstance().retroFitObject(context)

            login_call = apis.getRefreshToken(postTokenDAO.scope, postTokenDAO.grant_type, postTokenDAO.username, postTokenDAO.password, postTokenDAO.client_id, postTokenDAO.scope, postTokenDAO.refresh_token)


            login_call?.enqueue(object : Callback<TokenDAO> {
                override fun onResponse(call: Call<TokenDAO>, response: Response<TokenDAO>?) {

                    stop_loading_animation()

                    if (response != null && response.body() != null) {

                        if (response.body().personas != null && response.body().personas.length > 0) {

                            if (ESPApplication.getInstance().user != null) {
                                ESPApplication.getInstance().user = null
                            }

                            val userDAO = UserDAO()
                            response.body().password = postTokenDAO.password
                            userDAO.loginResponse = response.body()
                            ESPApplication.getInstance().user = userDAO
                            ESPApplication.getInstance().filter = Shared.getInstance().ResetApplicationFilter(context)


                            ESPApplication.getInstance().filter.isMySpace = !response.body().role?.toLowerCase(Locale.getDefault()).equals(Enums.applicant.toString(), ignoreCase = true)


                            Shared.getInstance().WritePref("Uname", postTokenDAO.username, "login_info", context)
                            Shared.getInstance().WritePref("Pass", postTokenDAO.password, "login_info", context)

                            if (isOrganizationCall)
                                getOrganizations()
                            else {
                                val intentToBeNewRoot = Intent(context, LoginScreenActivity::class.java)
                                val cn = intentToBeNewRoot.component
                                val mainIntent = Intent.makeRestartActivityTask(cn)
                                startActivity(mainIntent)
                            }


                        } else {

                            Shared.getInstance().messageBox(getString(R.string.expired), context)
                            Shared.getInstance().SignOutNotClear(context, true)
                        }


                    } else {

                        Shared.getInstance().messageBox(getString(R.string.expired), context)
                        Shared.getInstance().SignOutNotClear(context, true)

                    }
                }

                override fun onFailure(call: Call<TokenDAO>, t: Throwable) {

                    stop_loading_animation()
                    Shared.getInstance().messageBox(getString(R.string.expired), context)
                    Shared.getInstance().SignOutNotClear(context, true)

                }
            })

        } catch (ex: Exception) {
            stop_loading_animation()
            Shared.getInstance().messageBox(getString(R.string.expired), context)
            Shared.getInstance().SignOutNotClear(context, true)

        }

    }

    private fun deleteFirebaseToken() {

        start_loading_animation()


        try {

            val apis = Shared.getInstance().retroFitObject(context)
            val firebase_call = apis.deleteFirebaseToken(Shared.getInstance().getDeviceId(context))

            firebase_call.enqueue(object : Callback<FirebaseTokenDAO> {
                override fun onResponse(call: Call<FirebaseTokenDAO>, response: Response<FirebaseTokenDAO>?) {
                    stop_loading_animation()
                    val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancel(1000);
                    ShortcutBadger.applyCount(context, 0); //for 1.1.4+
                    Shared.getInstance().setBadge(context, 0)
                    Shared.getInstance().DrawermenuAction(2, "", context)
                    if (response?.body() != null) {
                        /*if (response.body().status) {
                            Shared.getInstance().DrawermenuAction(2, "", context)
                        } else {
                            Shared.getInstance().showAlertMessage(context?.getString(R.string.error), response.body().errorMessage, context)
                        }*/

                    } else
                        Shared.getInstance().showAlertMessage(context?.getString(R.string.error), context?.getString(R.string.some_thing_went_wrong), context)

                }

                override fun onFailure(call: Call<FirebaseTokenDAO>, t: Throwable) {
                    CustomLogs.displayLogs("$TAG ${t.printStackTrace()}")
                    stop_loading_animation()
                    Shared.getInstance().showAlertMessage(context?.getString(R.string.error), context?.getString(R.string.some_thing_went_wrong), context)

                }
            })

        } catch (ex: Exception) {

            ex.printStackTrace()
            stop_loading_animation()
            Shared.getInstance().showAlertMessage(context?.getString(R.string.error), context?.getString(R.string.some_thing_went_wrong), context)

        }

    }

    private fun start_loading_animation() {
        progressbar?.visibility = View.VISIBLE
    }

    private fun stop_loading_animation() {
        progressbar?.visibility = View.GONE
    }

    companion object {

        private val STATE_SELECTED_POSITION = "selected_navigation_drawer_position"

        var applicationDAO: ApplicationProfileDAO? = null
    }

    override fun onResume() {
        super.onResume()
        DrawerClose()
    }

}
