package com.esp.library.exceedersesp.fragments.applications

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.esp.library.R
import com.esp.library.exceedersesp.ESPApplication
import com.esp.library.exceedersesp.controllers.tindercard.*
import com.esp.library.utilities.common.*
import com.esp.library.exceedersesp.BaseActivity
import com.esp.library.exceedersesp.controllers.applications.AddApplicationsActivity
import com.esp.library.utilities.common.ServiceGenerator.builder
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_users_applications.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import utilities.adapters.setup.applications.ListCardsApplicationsAdapter
import utilities.adapters.setup.applications.ListUsersApplicationsAdapter
import utilities.data.applicants.ApplicationsDAO
import utilities.data.applicants.FirebaseTokenDAO
import utilities.data.applicants.ResponseApplicationsDAO
import utilities.data.filters.FilterDAO
import utilities.interfaces.DeleteDraftListener
import utilities.interfaces.RefreshSubDefinitionListener
import java.util.*


class UsersApplicationsFragment : androidx.fragment.app.Fragment(), CardStackListener, DeleteDraftListener {


    internal var TAG = javaClass.simpleName

    internal var context: BaseActivity? = null
    internal var mApplicationAdapter: androidx.recyclerview.widget.RecyclerView.Adapter<*>? = null
    private var mApplicationCardAdapter: androidx.recyclerview.widget.RecyclerView.Adapter<*>? = null
    private var mApplicationLayoutManager: androidx.recyclerview.widget.RecyclerView.LayoutManager? = null
    internal var call: Call<ResponseApplicationsDAO>? = null
    internal var callCards: Call<ResponseApplicationsDAO>? = null
    internal var profile_call: Call<String>? = null
    internal var app_actual_list: MutableList<ApplicationsDAO>? = null
    internal var app_actual_card_list: MutableList<ApplicationsDAO>? = null
    internal var imm: InputMethodManager? = null
    internal var cardStackView: CardStackView? = null
    internal var cardManager: CardStackLayoutManager? = null
    internal var cardAdapter: ListCardsApplicationsAdapter? = null
    internal var PAGE_NO = 1
    internal var PER_PAGE_RECORDS = 12
    internal var IN_LIST_RECORDS = 0
    internal var SCROLL_TO = 0
    internal var TOTAL_RECORDS_AVAILABLE = 0


    internal var PAGE_NO_CARD = 1
    internal var PER_PAGE_CARD_RECORDS = 12
    internal var IN_LIST_CARD_RECORDS = 0
    internal var TOTAL_CARD_RECORDS_AVAILABLE = 0

    internal var mHSListener: HideShowPlus? = null
    internal var pref: SharedPreference? = null
    internal var shimmer_view_container: ShimmerFrameLayout? = null
    internal var shimmer_view_container_cards: ShimmerFrameLayout? = null
     var refreshSubDefinitionListener: RefreshSubDefinitionListener? = null

    interface HideShowPlus {
        fun mAction(IsShown: Boolean)
    }


    private fun AddScroller() {

        //    val toolbar = activity?.findViewById(R.id.toolbar) as Toolbar
        view?.app_list?.addOnScrollListener(object : EndlessRecyclerViewScrollListener(mApplicationLayoutManager as androidx.recyclerview.widget.LinearLayoutManager?) {
            override fun onHide() {
                // Shared.getInstance().setToolbarHeight(toolbar, false)
            }

            override fun onShow() {
                // Shared.getInstance().setToolbarHeight(toolbar, true)
            }

            override fun getFooterViewType(defaultNoFooterViewType: Int): Int {
                return defaultNoFooterViewType
            }

            override fun onLoadMore(page: Int, totalItemsCount: Int) {

                if (IN_LIST_RECORDS < TOTAL_RECORDS_AVAILABLE) {
                    loadApplications(true)
                    // loadData(true)
                }

            }

        })

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = activity as BaseActivity?
        pref = SharedPreference(context)
        if (!ESPApplication.getInstance().isComponent)
            refreshSubDefinitionListener = context as RefreshSubDefinitionListener

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_users_applications, container, false)
        initailize(v)
        initializeCards()
        setGravity(v)


        v.app_list.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
            }

            override fun onScrollStateChanged(recyclerView: androidx.recyclerview.widget.RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                imm?.hideSoftInputFromWindow(view!!.windowToken, 0)
            }
        })


        v.txtseeall.setOnClickListener {

            startActivity(Intent(activity, UsersCardApplications::class.java))

        }

        v.add_btn.text = context?.getString(R.string.submit) + " " + pref?.getlabels()?.application

        v.add_btn.setOnClickListener { view ->
            if (ESPApplication.getInstance().user.profileStatus == null || ESPApplication.getInstance().user.profileStatus.equals(context?.getString(R.string.profile_complete), ignoreCase = true)) {
                Shared.getInstance().callIntentWithResult(AddApplicationsActivity::class.java, context, null, 2)
            } else if (ESPApplication.getInstance().user.profileStatus == context?.getString(R.string.profile_incomplete)) {
                Shared.getInstance().showAlertProfileMessage(context?.getString(R.string.profile_error_heading), context?.getString(R.string.profile_error_desc), context)

            } else if (ESPApplication.getInstance().user.profileStatus == getString(R.string.profile_incomplete_admin)) {
                Shared.getInstance().showAlertProfileMessage(context?.getString(R.string.profile_error_heading), context?.getString(R.string.profile_error_desc_admin), context)
            }
        }

        /*when (Shared.getInstance().isWifiConnected(context)) {
            true -> reLoadApplications()
            false -> Shared.getInstance().showAlertMessage(context?.getString(R.string.internet_error_heading), context?.getString(R.string.internet_connection_error), context)
        }*/

        when (Shared.getInstance().isWifiConnected(context)) {
            true -> postFirebaseToken()
            false -> Shared.getInstance().showAlertMessage(context?.getString(R.string.internet_error_heading), context?.getString(R.string.internet_connection_error), context)
        }



        return v
    }

    private fun initailize(v: View) {
        mApplicationLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
        v.app_list.setHasFixedSize(true)
        v.app_list.layoutManager = mApplicationLayoutManager
        v.app_list.itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()


        imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager


        if (ESPApplication.getInstance()?.user?.loginResponse?.role?.toLowerCase(Locale.getDefault()).equals(Enums.applicant.toString(), ignoreCase = true)) {
            v.txtnoapplicationadded?.text = context?.getString(R.string.no) + " " + pref?.getlabels()?.application + " " + context?.getString(R.string.added)
            v.txtnoapplicationadded?.text = context?.getString(R.string.startsubmittingapp) + " " + pref?.getlabels()?.application + " " + context?.getString(R.string.itseasy)


        } else {
            v.txtnoapplicationadded?.text = context?.getString(R.string.norecord)
        }
        shimmer_view_container = v.findViewById(R.id.shimmer_view_container);
        shimmer_view_container_cards = v.findViewById(R.id.shimmer_view_container_cards);
        cardStackView = v.findViewById<CardStackView>(R.id.card_stack_view)
        cardManager = CardStackLayoutManager(activity, this)
    }

    private fun initializeCards() {
        cardManager?.setStackFrom(StackFrom.Bottom)
        cardManager?.setVisibleCount(3)
        cardManager?.setTranslationInterval(8.0f)
        cardManager?.setScaleInterval(0.95f)
        cardManager?.setSwipeThreshold(0.3f)
        cardManager?.setMaxDegree(0.0f)
        cardManager?.setDirections(Direction.HORIZONTAL)
        cardManager?.setCanScrollHorizontal(true)
        cardManager?.setCanScrollVertical(false)
        cardManager?.setSwipeableMethod(SwipeableMethod.AutomaticAndManual)
        cardManager?.setOverlayInterpolator(LinearInterpolator())
        cardStackView?.layoutManager = cardManager


    }

    private fun setGravity(v: View) {
        val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        if (pref!!.language.equals("ar", ignoreCase = true)) {
            params.weight = 0.8f

        } else {
            params.weight = 0.9f
        }
        v.rlcardstack.layoutParams = params
    }

    override fun onCardDragging(direction: Direction, ratio: Float) {
        Log.d("CardStackView", "onCardDragging: d = ${direction.name}, r = $ratio")
    }

    override fun onCardSwiped(direction: Direction) {
        Log.d("CardStackView", "onCardSwiped: p = ${cardManager?.topPosition}, d = $direction")
        if (cardManager?.topPosition == cardAdapter?.itemCount) {
            paginate()
        }


    }

    override fun onCardRewound() {
        Log.d("CardStackView", "onCardRewound: ${cardManager?.topPosition}")
    }

    override fun onCardCanceled() {
        Log.d("CardStackView", "onCardCanceled: ${cardManager?.topPosition}")
    }

    override fun onCardAppeared(view: View, position: Int) {
        Log.d("CardStackView", "onCardAppeared")
    }

    override fun onCardDisappeared(view: View, position: Int) {

        Log.d("CardStackView", "onCardDisappeared")
    }

    private fun paginate() {
        if (IN_LIST_CARD_RECORDS < TOTAL_CARD_RECORDS_AVAILABLE) {
            loadCardApplications(true)
        } else {
            cardAdapter?.setSpots(cardAdapter?.getSpots()!!)
            cardAdapter?.notifyDataSetChanged()
        }

        /*    val old = cardAdapter?.getSpots()
            val new = old?.plus(cardAdapter?.getSpots()!!)
            val callback = SpotDiffCallback(old!!, new!!)
            val result = DiffUtil.calculateDiff(callback)
            CustomLogs.displayLogs(TAG + "result: " + result)
            cardAdapter?.setSpots(new)
            result.dispatchUpdatesTo(cardAdapter!!)*/

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        try {
            mHSListener = context as HideShowPlus
        } catch (e: Exception) {

        }

        view?.swipeRefreshLayout?.setOnRefreshListener {
            refreshListCall()
            start_loading_animation_cards()
        }

        if (!checkPermission()) {
            requestPermission()
        }


    }


    private fun refreshListCall() {
        when (Shared.getInstance().isWifiConnected(context)) {
            true -> {
                PAGE_NO = 1
                PER_PAGE_RECORDS = 12
                IN_LIST_RECORDS = 0
                TOTAL_RECORDS_AVAILABLE = 0
                app_actual_list?.clear()
                app_actual_card_list?.clear()
                mApplicationAdapter?.notifyDataSetChanged()
                mApplicationCardAdapter?.notifyDataSetChanged()
                view?.txtrequestcount?.text = ""
                loadApplications(false)

            }
            false -> {
                Shared.getInstance().showAlertMessage(context?.getString(R.string.internet_error_heading), context?.getString(R.string.internet_connection_error), context)
                view?.swipeRefreshLayout?.isRefreshing = false
            }
        }
    }

    fun reLoadApplications() {
        refreshListCall()
    }


    fun loadApplications(isLoadMore: Boolean) {


        if (isLoadMore) {
            view?.load_more_div?.visibility = View.VISIBLE
        } else {
            start_loading_animation()
            if (ESPApplication.getInstance().isComponent || ESPApplication.getInstance()?.user?.loginResponse?.role?.toLowerCase(Locale.getDefault()) == Enums.applicant.toString())
                refreshSubDefinitionListener?.refreshSubDefinition()
        }

        val apis = Shared.getInstance().retroFitObject(context)

        if (isLoadMore) {

        } else {
            PAGE_NO = 1
            PER_PAGE_RECORDS = 12
            IN_LIST_RECORDS = 0
            TOTAL_RECORDS_AVAILABLE = 0
        }

        if (ESPApplication.getInstance().filter == null) {
            ESPApplication.getInstance().filter = FilterDAO()
        }

        ESPApplication.getInstance().filter.pageNo = PAGE_NO
        ESPApplication.getInstance().filter.recordPerPage = PER_PAGE_RECORDS
        ESPApplication.getInstance().filter.search = ""

        var dao: FilterDAO? = null
        val list = ArrayList<String>()
        val title = arguments?.getString("title")
        if (title != null) {

            dao = Shared.getInstance().CloneFilter(ESPApplication.getInstance().filter)

            if (ESPApplication.getInstance()?.user?.loginResponse?.role?.toLowerCase(Locale.getDefault()) == Enums.applicant.toString()) {
                dao.isMySpace = false
                dao.isFilterApplied = true
                // dao.myApplications = true
                dao.myApplications = false


            } else {
                dao.isMySpace = false
                dao.isFilterApplied = true
                dao.myApplications = false

            }

            if (title.equals(getString(R.string.open), ignoreCase = true)) {
                list.add("1")
                list.add("2")
                dao.statuses = list
                if (!isLoadMore)
                    loadCardApplications(false)
            } else {


                if (ESPApplication.getInstance()?.filter?.statuses != null && ESPApplication.getInstance()?.filter?.statuses!!.size < 5) {
                    if (!dao.statuses.isNullOrEmpty())
                        dao.statuses = ArrayList<String>()
                    val tempList = ArrayList<String>()

                    for (i in 0 until ESPApplication.getInstance().filter.statuses!!.size) {
                        val get = ESPApplication.getInstance().filter.statuses?.get(i)
                        if (get == "1" || get == "2") {
                        } else {
                            tempList.add(get!!)
                        }
                    }

                    dao.statuses = tempList
                } else {
                    list.add("3")
                    list.add("4")
                    list.add("5")
                    dao.statuses = list
                }

                view?.rlcardstack?.visibility = View.GONE

            }


            /*if (ESPApplication.getInstance().filter.statuses!!.size < 4) {
                if (!dao.statuses.isNullOrEmpty())
                    dao.statuses = ArrayList<String>()
                dao.statuses = ESPApplication.getInstance().filter.statuses
            } else {
                if (title.equals("open", ignoreCase = true) ||
                        title.equals("pending", ignoreCase = true)) {

                    dao.isMySpace = false
                    dao.isFilterApplied = true
                    dao.myApplications = true

                    list.add("1")
                    list.add("2")
                    dao.statuses = list

                    if (!isLoadMore)
                        loadCardApplications(false)

                } else {

                    view?.rlcardstack?.visibility = View.GONE
                    dao.isMySpace = false
                    dao.isFilterApplied = true
                    dao.myApplications = false

                    if (title.equals("all", ignoreCase = true))
                        list.add("0")
                    else {
                        list.add("3")
                        list.add("4")
                    }
                    dao.statuses = list
                }
            }*/
        } else {

            if (ESPApplication.getInstance().filter.statuses!!.size == 4) {
                dao = Shared.getInstance().CloneFilter(ESPApplication.getInstance().filter)
                dao!!.statuses = null
                val empty_fitler = ArrayList<String>()
                empty_fitler.add("0")
                dao.statuses = empty_fitler
            } else {
                dao = Shared.getInstance().CloneFilter(ESPApplication.getInstance().filter)
            }
        }
        call = apis.getUserApplicationsV3(dao)



        call!!.enqueue(
                object : Callback<ResponseApplicationsDAO> {
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
                                    (mApplicationAdapter as ListUsersApplicationsAdapter?)?.getFragmentContext(this@UsersApplicationsFragment)
                                    view?.app_list?.adapter = mApplicationAdapter
                                    mApplicationAdapter!!.notifyDataSetChanged()

                                    view?.app_list?.scrollToPosition(SCROLL_TO - 3)

                                } else {

                                    // val app_actual_list_temp = filterData(response)
                                    if (app_actual_list != null)
                                        app_actual_list?.addAll(response.body().applications!!)
                                    else
                                        app_actual_list = response.body().applications as MutableList<ApplicationsDAO>?

                                    mApplicationAdapter = context?.let { ListUsersApplicationsAdapter(removeDuplication(app_actual_list), it, "", false) }
                                    (mApplicationAdapter as ListUsersApplicationsAdapter?)?.getFragmentContext(this@UsersApplicationsFragment)
                                    view?.app_list?.adapter = mApplicationAdapter

                                    PAGE_NO++
                                    IN_LIST_RECORDS = removeDuplication(app_actual_list).size
                                    TOTAL_RECORDS_AVAILABLE = response.body().totalRecords
                                    SCROLL_TO = 0
                                    AddScroller()


                                }

                                view?.txtrequestcount?.text = TOTAL_RECORDS_AVAILABLE.toString() + " " + activity?.getString(R.string.requests)

                                /*if (ESPApplication.getInstance()?.user?.loginResponse?.role?.toLowerCase() != getString(R.string.applicantsmall)) {
                                    if (!pref?.notificationThrow!!) {
                                        pref?.saveNotificationThrow(true)
                                        localNotification(TOTAL_RECORDS_AVAILABLE)
                                    }
                                    ShortcutBadger.applyCount(context, TOTAL_RECORDS_AVAILABLE); //for 1.1.4+
                                    Shared.getInstance().setBadge(context, TOTAL_RECORDS_AVAILABLE)
                                } else {
                                    pref?.saveNotificationThrow(false)
                                    val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                                    notificationManager.cancel(1000);
                                    ShortcutBadger.applyCount(context, 0); //for 1.1.4+
                                    Shared.getInstance().setBadge(context, 0)
                                }*/

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
                                if (app_actual_list == null || app_actual_list?.size == 0)
                                    UnSuccessResponse()
                            }
                        } else {
                            if (!isLoadMore) {
                                GetProfileStatus()
                            } else
                                stop_loading_animation()
                            if (app_actual_list == null || app_actual_list?.size == 0)
                                UnSuccessResponse()
                        }


                    }


                    override fun onFailure(call: Call<ResponseApplicationsDAO>, t: Throwable) {
                        //  Shared.getInstance().messageBox(t.message, context)
                        stop_loading_animation()
                        UnSuccessResponse()
                        if (!isLoadMore) {
                            GetProfileStatus()
                        }

                    }
                })


    }//LoggedInUser end

    fun loadCardApplications(isLoadMore: Boolean) {

        if (isLoadMore) {
            view?.txtloadmorecards?.visibility = View.VISIBLE
        } else {
            start_loading_animation_cards()

        }

        val apis = Shared.getInstance().retroFitObject(context)

        if (isLoadMore) {

        } else {
            PAGE_NO_CARD = 1
            PER_PAGE_CARD_RECORDS = 12
            IN_LIST_CARD_RECORDS = 0
            TOTAL_CARD_RECORDS_AVAILABLE = 0
        }

        ESPApplication.getInstance().filter.pageNo = PAGE_NO_CARD
        ESPApplication.getInstance().filter.recordPerPage = PER_PAGE_CARD_RECORDS
        ESPApplication.getInstance().filter.search = ""

        var dao: FilterDAO? = null
        val list = ArrayList<String>()
        val title = arguments?.getString("title")
        if (title != null) {

            dao = Shared.getInstance().CloneFilter(ESPApplication.getInstance().filter)

            /* if (ESPApplication.getInstance().filter.statuses!!.size < 5) {
                 dao.statuses = ESPApplication.getInstance().filter.statuses
             } else {*/
            if (title.equals(getString(R.string.open), ignoreCase = true) ||
                    title.equals(getString(R.string.pending), ignoreCase = true)) {

                dao.isMySpace = true
                dao.isFilterApplied = true
                dao.myApplications = false
                list.add("2")
                dao.statuses = list
            }
            // }
        } else {

            /*if (ESPApplication.getInstance().filter.statuses!!.size == 5) {
                dao = Shared.getInstance().CloneFilter(ESPApplication.getInstance().filter)
                dao!!.statuses = null
                val empty_fitler = ArrayList<String>()
                empty_fitler.add("2")
                dao.statuses = empty_fitler
            } else {
                dao = Shared.getInstance().CloneFilter(ESPApplication.getInstance().filter)
            }*/
        }
        callCards = apis.getUserApplicationsV3(dao!!)



        callCards!!.enqueue(
                object : Callback<ResponseApplicationsDAO> {
                    override fun onResponse(call: Call<ResponseApplicationsDAO>, response: Response<ResponseApplicationsDAO>?) {

                        if (isLoadMore) {
                            view?.txtloadmorecards?.visibility = View.GONE
                        }

                        if (response != null && response.body() != null && response.body().totalRecords > 0) {
                            if (response.body().applications != null && response.body().applications!!.size > 0) {

                                if (isLoadMore) {
                                    if (app_actual_card_list == null) {
                                        app_actual_card_list = response.body().applications as MutableList<ApplicationsDAO>?
                                    } else if (app_actual_card_list != null && app_actual_card_list!!.size > 0) {

                                        if (app_actual_card_list != null)
                                            app_actual_card_list?.addAll(response.body().applications!!)
                                        else
                                            app_actual_card_list = response.body().applications as MutableList<ApplicationsDAO>?


                                    }



                                    PAGE_NO_CARD++
                                    IN_LIST_CARD_RECORDS = removeDuplication(app_actual_card_list).size
                                    TOTAL_CARD_RECORDS_AVAILABLE = response.body().totalRecords

                                    mApplicationCardAdapter = context?.let { ListCardsApplicationsAdapter(removeDuplication(app_actual_card_list), it, "", false) }
                                    cardAdapter = this@UsersApplicationsFragment.mApplicationCardAdapter as ListCardsApplicationsAdapter?
                                    cardStackView?.adapter = cardAdapter


                                } else {

                                    // val app_actual_card_list = filterData(response)
                                    if (app_actual_card_list != null)
                                        app_actual_card_list?.addAll(response.body().applications!!)
                                    else
                                        app_actual_card_list = response.body().applications as MutableList<ApplicationsDAO>?


                                    PAGE_NO_CARD++
                                    IN_LIST_CARD_RECORDS = removeDuplication(app_actual_card_list).size
                                    TOTAL_CARD_RECORDS_AVAILABLE = response.body().totalRecords



                                    mApplicationCardAdapter = context?.let { ListCardsApplicationsAdapter(removeDuplication(app_actual_card_list), it, "", false) }
                                    cardAdapter = this@UsersApplicationsFragment.mApplicationCardAdapter as ListCardsApplicationsAdapter?
                                    cardStackView?.adapter = cardAdapter

                                }

                                view?.txtseeall?.text = activity?.getString(R.string.seeall) + " (" + TOTAL_CARD_RECORDS_AVAILABLE.toString() + ")"

                                view?.rlcardstack?.visibility = View.VISIBLE
                                stop_loading_animation_cards()

                            } else {
                                // Shared.getInstance().messageBox(getString(R.string.some_thing_went_wrong), context)
                                stop_loading_animation_cards()
                                view?.rlcardstack?.visibility = View.GONE
                            }
                        } else {
                            // Shared.getInstance().messageBox(getString(R.string.some_thing_went_wrong), context)
                            stop_loading_animation_cards()
                            view?.rlcardstack?.visibility = View.GONE
                        }


                    }


                    override fun onFailure(call: Call<ResponseApplicationsDAO>, t: Throwable) {
                        //  Shared.getInstance().messageBox(t.message, context)
                        try {
                            Shared.getInstance().messageBox(activity?.getString(R.string.some_thing_went_wrong), context)
                        } catch (e: java.lang.Exception) {
                        }
                        stop_loading_animation_cards()
                        view?.card_stack_view?.visibility = View.GONE

                    }
                })


    }//LoggedInUser end

    fun removeDuplication(appActualList: MutableList<ApplicationsDAO>?): ArrayList<ApplicationsDAO> {
        val listCollections = ArrayList<ApplicationsDAO>()
        for (i in 0 until appActualList!!.size) {
            val getList = appActualList.get(i);
            val isArrayHasValue = listCollections.any { x -> x.id == getList.id }
            if (!isArrayHasValue) {
                listCollections.add(getList)
            }
        }

        return listCollections
    }

    override fun onDestroyView() {
        if (call != null) {
            call!!.cancel()
        }

        if (callCards != null) {
            callCards!!.cancel()
        }
        super.onDestroyView()
    }

    private fun start_loading_animation_cards() {
        view?.txtseeall?.text = ""
        view?.txtrequesttoscreen?.text = ""
        shimmer_view_container_cards?.visibility = View.VISIBLE
        shimmer_view_container_cards?.startShimmerAnimation();
        view?.card_stack_view?.visibility = View.GONE
    }

    private fun stop_loading_animation_cards() {
        view?.txtrequesttoscreen?.text = getString(R.string.requesttoaction)
        shimmer_view_container_cards?.visibility = View.GONE
        shimmer_view_container_cards?.stopShimmerAnimation();
        view?.card_stack_view?.visibility = View.VISIBLE


    }

    private fun start_loading_animation() {
        view?.no_application_available_div?.visibility = View.GONE
        shimmer_view_container?.visibility = View.VISIBLE
        shimmer_view_container?.startShimmerAnimation();
        view?.app_list?.visibility = View.GONE
    }

    private fun stop_loading_animation() {
        view?.swipeRefreshLayout?.isRefreshing = false
        shimmer_view_container?.visibility = View.GONE
        shimmer_view_container?.stopShimmerAnimation();
        view?.app_list?.visibility = View.VISIBLE


    }

    private fun SuccessResponse() {
        view?.app_list?.visibility = View.VISIBLE
        view?.no_application_available_div?.visibility = View.GONE

      //  if (app_actual_list != null && app_actual_list!!.size > 0) {

            if (mHSListener != null) {
                mHSListener?.mAction(false)
            }

     //   }
    }

    private fun UnSuccessResponse() {
        view?.app_list?.visibility = View.GONE
        view?.no_application_available_div?.visibility = View.VISIBLE

        try {


            if (ESPApplication.getInstance()?.user?.loginResponse?.role?.toLowerCase(Locale.getDefault()).equals(Enums.applicant.toString(), ignoreCase = true)) {
                view?.add_btn?.visibility = View.VISIBLE
                view?.detail_text?.visibility = View.VISIBLE

              //  if (app_actual_list == null && app_actual_list!!.size == 0) {

                    if (mHSListener != null) {
                        mHSListener?.mAction(false)
                    }

             //   }

            } else {
                view?.add_btn?.visibility = View.GONE
                view?.detail_text?.visibility = View.GONE
            }
        } catch (e: java.lang.Exception) {

        }

    }

    fun GetProfileStatus() {

        if (!ESPApplication.getInstance()?.user?.loginResponse?.role?.toLowerCase(Locale.getDefault()).equals(Enums.applicant.toString(), ignoreCase = true)) {
            stop_loading_animation()
            return
        }
        start_loading_animation()

        val apis = Shared.getInstance().retroFitObject(context)


        profile_call = apis.userProfileStatus
        profile_call!!.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>?) {

                stop_loading_animation()

                if (response?.body() != null) {

                    val response_text = response.body()
                    if (response_text != null)
                        ESPApplication.getInstance()?.user?.profileStatus = response_text

                    when {
                        response_text.equals(context?.getString(R.string.profile_complete), ignoreCase = true) -> {
                        }
                        response_text.equals(context?.getString(R.string.profile_incomplete), ignoreCase = true) -> Shared.getInstance().showAlertProfileMessage(context?.getString(R.string.profile_error_heading), context?.getString(R.string.profile_error_desc), context)
                        response_text.equals(context?.getString(R.string.profile_incomplete_admin), ignoreCase = true) -> Shared.getInstance().showAlertProfileMessage(context?.getString(R.string.profile_error_heading), context?.getString(R.string.profile_error_desc_admin), context)
                    }

                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                stop_loading_animation()
            }
        })


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

    companion object {

        var isShowingActivity: Boolean = false

        private val PERMISSION_REQUEST_CODE = 200
        val REQUEST_ID_MULTIPLE_PERMISSIONS = 1

        fun newInstance(): UsersApplicationsFragment {
            val fragment = UsersApplicationsFragment()
            val args = Bundle()

            fragment.arguments = args
            return fragment
        }

        fun newInstance(title: String): androidx.fragment.app.Fragment {
            val fragment = UsersApplicationsFragment()
            val args = Bundle()
            args.putString("title", title);
            fragment.arguments = args
            return fragment
        }
    }

    override fun onPause() {
        super.onPause()
        isShowingActivity = false
        if ((mApplicationAdapter as ListUsersApplicationsAdapter?)?.getPopUpMenu() != null) {
            val popUpMenu = (mApplicationAdapter as ListUsersApplicationsAdapter?)?.getPopUpMenu()
            popUpMenu?.dismiss()
        }
    }

    override fun onResume() {
        super.onResume()

        isShowingActivity = true

        when (Shared.getInstance().isWifiConnected(context)) {
            true -> reLoadApplications()
            false -> Shared.getInstance().showAlertMessage(context?.getString(R.string.internet_error_heading), context?.getString(R.string.internet_connection_error), context)
        }


    }


    override fun deletedraftApplication(applicationsDAO: ApplicationsDAO) {
        showConfirmationMessage(applicationsDAO)
    }

    private fun postFirebaseToken() {

        // start_loading_animation()


        val firebaseTokenDAO = FirebaseTokenDAO()
        firebaseTokenDAO.fbTokenId = pref?.firebaseId!!
        firebaseTokenDAO.personaId = pref?.personaId!!
        firebaseTokenDAO.token = pref?.firebaseToken
        firebaseTokenDAO.organizationId = pref?.organizationId!!
        firebaseTokenDAO.deviceId = Shared.getInstance().getDeviceId(context)

        try {

            var firebase_call = Shared.getInstance().retroFitObject(context).postFirebaseToken(firebaseTokenDAO)

            firebase_call.enqueue(object : Callback<FirebaseTokenDAO> {
                override fun onResponse(call: Call<FirebaseTokenDAO>, response: Response<FirebaseTokenDAO>?) {
                    // stop_loading_animation()
                    if (response?.body() != null) {


                    }


                }

                override fun onFailure(call: Call<FirebaseTokenDAO>, t: Throwable) {
                    CustomLogs.displayLogs("$TAG ${t.printStackTrace()}")
                    //  stop_loading_animation()
                    //  Shared.getInstance().showAlertMessage(context.getString(R.string.error), context.getString(R.string.some_thing_went_wrong), context)

                }
            })

        } catch (ex: Exception) {

            ex.printStackTrace()
            //  stop_loading_animation()
            // Shared.getInstance().showAlertMessage(context.getString(R.string.error), context.getString(R.string.some_thing_went_wrong), context)

        }

    }

    fun showConfirmationMessage(applicationDAO: ApplicationsDAO) {


        MaterialAlertDialogBuilder(activity, R.style.AlertDialogTheme)
                .setTitle(activity?.applicationContext?.getString(R.string.delete) + " " + pref?.getlabels()?.application)
                .setCancelable(false)
                .setMessage(activity?.applicationContext?.getString(R.string.areyousure) + " " + applicationDAO.definitionName + " " + pref?.getlabels()?.application + "?")
                .setPositiveButton(activity?.getApplicationContext()?.getString(R.string.yesdelete)) { dialogInterface, i ->
                    dialogInterface.dismiss()


                    when (Shared.getInstance().isWifiConnected(context)) {
                        true -> deleteApplication(applicationDAO.id)
                        false -> Shared.getInstance().showAlertMessage(context?.getString(R.string.internet_error_heading), context?.getString(R.string.internet_connection_error), context)
                    }
                }
                .setNeutralButton(activity?.getApplicationContext()?.getString(R.string.no)) { dialogInterface, i ->
                    dialogInterface.dismiss()
                }
                .show();


    }

    fun deleteApplication(id: Int) {

        start_loading_animation()

        val apis = Shared.getInstance().retroFitObject(context)
        val delete_call = apis.deleteApplication(id)
        delete_call.enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>?) {
                refreshListCall()
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                t.printStackTrace()
                stop_loading_animation()
                Shared.getInstance().showAlertMessage(pref?.getlabels()?.application, context?.getString(R.string.some_thing_went_wrong), context)
                return
            }
        })


    }


}// Required empty public constructor
