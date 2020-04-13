package utilities.adapters.setup.applications

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.esp.library.R
import com.esp.library.exceedersesp.BaseActivity
import com.esp.library.exceedersesp.ESPApplication
import com.esp.library.exceedersesp.controllers.applications.ApplicationDetailScreenActivity
import com.esp.library.exceedersesp.fragments.applications.UsersApplicationsFragment
import com.esp.library.utilities.common.CustomLogs
import com.esp.library.utilities.common.Shared
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import utilities.data.applicants.ApplicationSingleton
import utilities.data.applicants.ApplicationsDAO
import utilities.data.applicants.feedback.ApplicationsFeedbackDAO
import utilities.interfaces.DeleteDraftListener

class ListUsersApplicationsAdapter(private var mApplications: List<ApplicationsDAO>?, con: BaseActivity,
                                   internal var searched_text: String?, subApplications: Boolean) :
        androidx.recyclerview.widget.RecyclerView.Adapter<ListUsersApplicationsAdapter.ParentViewHolder>() {

    private var context: BaseActivity? = null
    var popup: PopupMenu? = null
    var deleteDraftListener: DeleteDraftListener? = null


    open class ParentViewHolder(v: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v)

    inner class ActivitiesList(v: View) : ParentViewHolder(v) {

        internal var cards: RelativeLayout
        internal var rlcategory: RelativeLayout
        internal var category: TextView
        internal var definitionName: TextView
        internal var rlpendingfor: RelativeLayout? = null
        internal var startedOn: TextView
        internal var startedOntext: TextView
        internal var applicationNumber: TextView
        internal var rlrequestNum: RelativeLayout
        internal var txtstatus: TextView
        internal var reasontextvalue: TextView
        internal var rlreason: RelativeLayout
        internal var ibRemoveCard: ImageButton
        internal var categorytext: TextView
        internal var pendingfor: TextView? = null
        internal var status_list: androidx.recyclerview.widget.RecyclerView
        internal var voverduedot: View


        init {

            cards = itemView.findViewById(R.id.cards)
            rlcategory = itemView.findViewById(R.id.rlcategory)
            category = itemView.findViewById(R.id.category)
            definitionName = itemView.findViewById(R.id.definitionName)
            startedOn = itemView.findViewById(R.id.startedOn)
            startedOntext = itemView.findViewById(R.id.startedOntext)
            applicationNumber = itemView.findViewById(R.id.applicationNumber)
            rlrequestNum = itemView.findViewById(R.id.rlrequestNum)
            txtstatus = itemView.findViewById(R.id.txtstatus)
            rlreason = itemView.findViewById(R.id.rlreason)
            ibRemoveCard = itemView.findViewById(R.id.ibRemoveCard)
            categorytext = itemView.findViewById(R.id.categorytext)
            reasontextvalue = itemView.findViewById(R.id.reasontextvalue)
            status_list = itemView.findViewById(R.id.status_list)
            voverduedot = itemView.findViewById(R.id.voverduedot)
            status_list.setHasFixedSize(true)
            status_list.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context, androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false)
            status_list.setItemAnimator(androidx.recyclerview.widget.DefaultItemAnimator())

            try {
                rlpendingfor = itemView.findViewById(R.id.rlpendingfor)
                pendingfor = itemView.findViewById(R.id.pendingfor)
            } catch (e: java.lang.Exception) {

            }


        }
    }

    init {
        context = con
        isSubApplications = subApplications

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentViewHolder {
        val v: View
        if (isSubApplications)
            v = LayoutInflater.from(parent.context).inflate(R.layout.list_subapplications_row, parent, false)
        else
            v = LayoutInflater.from(parent.context).inflate(R.layout.list_applications_row, parent, false)
        return ActivitiesList(v)
    }

    fun getPopUpMenu(): PopupMenu? {
        return popup
    }


    fun setSpots(mApplications: List<ApplicationsDAO>) {
        this.mApplications = mApplications
    }

    fun getSpots(): List<ApplicationsDAO> {
        return this.mApplications!!
    }

    fun getFragmentContext(activity: UsersApplicationsFragment) {
        deleteDraftListener = activity
    }

    override fun onBindViewHolder(holder_parent: ParentViewHolder, position: Int) {


        val holder = holder_parent as ActivitiesList
        val applicationsDAO = mApplications?.get(position)


        val category = applicationsDAO?.category
        val definitionName = applicationsDAO?.definitionName
        val applicationNumber = applicationsDAO?.applicationNumber
        var applicationName: String? = ""
        applicationName = when (ESPApplication.getInstance()?.user?.loginResponse?.role?.toLowerCase().equals(context?.getString(R.string.applicantsmall), ignoreCase = true)) {
            true -> ESPApplication.getInstance()?.user?.loginResponse?.name
            false -> applicationsDAO?.applicantName
        }

        /*       if (ESPApplication.getInstance()?.user?.loginResponse?.role?.toLowerCase() != context?.getString(R.string.applicantsmall)
                       && applicationsDAO?.isOverDue!!)
                   holder.voverduedot.visibility = View.VISIBLE*/

        if (searched_text != null && searched_text!!.length > 0) {

            holder.category.text = Shared.getInstance().getSearchedTextHighlight(searched_text, category,context)
            holder.definitionName.text = Shared.getInstance().getSearchedTextHighlight(searched_text, definitionName,context)
            holder.applicationNumber.text = Shared.getInstance().getSearchedTextHighlight(searched_text, applicationNumber,context)

        } else {

            if (isSubApplications) {
                holder.categorytext.text = context?.getString(R.string.applicantcolon)
                holder.category.text = applicationName
                holder.definitionName.text = definitionName
                holder.rlpendingfor?.visibility = View.VISIBLE
            } else {
                if (category.isNullOrEmpty())
                    holder.rlcategory.visibility = View.GONE

                holder.category.text = category
                holder.definitionName.text = definitionName
                holder.rlpendingfor?.visibility = View.GONE
            }
            holder.applicationNumber.text = applicationNumber
        }


        var displayDate = "";

        if (applicationsDAO?.submittedOn != null && applicationsDAO.submittedOn!!.length > 0) {
            displayDate = Shared.getInstance().getDisplayDate(context, applicationsDAO.submittedOn, true)
            holder.startedOn.text = displayDate
        } else {
            displayDate = Shared.getInstance().getDisplayDate(context, applicationsDAO?.createdOn, true)
            holder.startedOn.text = displayDate
        }


        val days = Shared.getInstance().fromStringToDate(context, displayDate)


        var daysVal = context?.getString(R.string.day)
        if (days > 1)
            daysVal = context?.getString(R.string.days)

        holder.pendingfor?.setText(days.toString() + " " + daysVal)

        holder.ibRemoveCard.setOnClickListener { v ->
            if (applicationsDAO != null) {
                ShowMenu(v, applicationsDAO)
            }
        }


        if (applicationsDAO != null) {
            setStatusColor(holder, applicationsDAO)
        }



        holder.cards.setOnClickListener {
            holder.cards.isEnabled = false

            if (ESPApplication.getInstance()?.user?.profileStatus == null || ESPApplication.getInstance()?.user?.profileStatus == context?.getString(R.string.profile_complete)) {
                if (applicationsDAO != null) {
                    appDetail(applicationsDAO, false)
                }
            } else if (ESPApplication.getInstance()?.user?.profileStatus == context?.getString(R.string.profile_incomplete)) {
                Shared.getInstance().showAlertProfileMessage(context?.getString(R.string.profile_error_heading), context?.getString(R.string.profile_error_desc), context)

            } else if (ESPApplication.getInstance()?.user?.profileStatus == context?.getString(R.string.profile_incomplete_admin)) {
                Shared.getInstance().showAlertProfileMessage(context?.getString(R.string.profile_error_heading), context?.getString(R.string.profile_error_desc_admin), context)
            }

            val handler = Handler()
            handler.postDelayed({ holder.cards.isEnabled = true }, 2000)

        }

        val statusAdapter = ApplicationStatusAdapter(applicationsDAO?.stageStatuses, context!!);
        holder.status_list.adapter = statusAdapter

        if (applicationsDAO?.statusId == 4) {
            holder.rlreason.visibility = View.VISIBLE
            /*val lp = holder.txtstatus.getLayoutParams() as RelativeLayout.LayoutParams
            lp.addRule(RelativeLayout.BELOW, holder.reasontext.getId());
            holder.txtstatus.setLayoutParams(lp);*/
            GetApplicationFeedBack(applicationsDAO.id.toString(), holder)
        } else
            holder.rlreason.visibility = View.GONE

        if (applicationsDAO?.statusId == 1) // if draft application hide submitted on and align status for this purpose swap submitted on with request # and hide request #
        {
            holder.startedOntext.text = context?.getString(R.string.requestnumber)
            holder.startedOn.text = holder.applicationNumber.text
            holder.rlrequestNum.visibility = View.GONE
        }


    }//End Holder Class

    private fun ShowMenu(v: View, applicationsDAO: ApplicationsDAO) {

        val POPUP_CONSTANT = "mPopup"
        val POPUP_FORCE_SHOW_ICON = "setForceShowIcon"

        popup = PopupMenu(context!!, v)
        popup?.inflate(R.menu.menu_user_list_actions);
        val menuOpts = popup?.menu

        popup?.gravity = Gravity.CENTER

        try {
            // Reflection apis to enforce show icon
            val fields = popup!!.javaClass.declaredFields
            for (field in fields) {
                if (field.name == POPUP_CONSTANT) {
                    field.isAccessible = true
                    val menuPopupHelper = field.get(popup)
                    val classPopupHelper = Class.forName(menuPopupHelper.javaClass.name)
                    val setForceIcons = classPopupHelper.getMethod(POPUP_FORCE_SHOW_ICON, Boolean::class.javaPrimitiveType!!)
                    setForceIcons.invoke(menuPopupHelper, true)
                    break
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }


        val action_resubmit = menuOpts?.findItem(R.id.action_resubmit)
        val action_delete_request = menuOpts?.findItem(R.id.action_delete_request)

        if (applicationsDAO.statusId == 1) {
            action_delete_request?.isEnabled = true
            action_resubmit?.isEnabled = false
        } else {
            action_delete_request?.isEnabled = false
            action_resubmit?.isEnabled = true
        }


        popup?.setOnMenuItemClickListener { item ->
            val id = item.itemId
            if (id == R.id.action_resubmit) {
                appDetail(applicationsDAO, true)
            } else if (id == R.id.action_delete_request) {
                deleteDraftListener?.deletedraftApplication(applicationsDAO)
            }
            false
        }
        popup?.show()


    }


    private fun setStatusColor(holder: ActivitiesList, applicationsDAO: ApplicationsDAO) {
        val getStatusId = applicationsDAO.statusId
        holder.txtstatus.setBackgroundResource(R.drawable.status_background)
        val drawable = holder.txtstatus.getBackground() as GradientDrawable
        when (getStatusId) {
            0 // Invited
            -> {

                holder.txtstatus.setText(R.string.invited)
                holder.txtstatus.setTextColor(ContextCompat.getColor(context!!, R.color.status_invited))
                drawable.setColor(ContextCompat.getColor(context!!, R.color.status_invited_background))
                holder.ibRemoveCard.visibility = View.GONE
            }
            1 // New as draft
            -> {
                holder.txtstatus.setText(R.string.draftcaps)
                holder.txtstatus.setTextColor(ContextCompat.getColor(context!!, R.color.status_draft))
                drawable.setColor(ContextCompat.getColor(context!!, R.color.status_draft_background))
                if (ESPApplication.getInstance().isComponent ||ESPApplication.getInstance()?.user?.loginResponse?.role?.toLowerCase().equals(context?.getString(R.string.applicantsmall), ignoreCase = true))
                    holder.ibRemoveCard.visibility = View.VISIBLE
                else
                    holder.ibRemoveCard.visibility = View.GONE
            }
            2 // Pending
            -> {
                holder.txtstatus.setText(R.string.inprogress)
                holder.txtstatus.setTextColor(ContextCompat.getColor(context!!, R.color.status_pending))
                drawable.setColor(ContextCompat.getColor(context!!, R.color.status_pending_background))
                holder.ibRemoveCard.visibility = View.GONE
            }
            3 // Accepted
            -> {

                holder.txtstatus.setText(R.string.accepted)
                holder.txtstatus.setTextColor(ContextCompat.getColor(context!!, R.color.status_accepted))
                drawable.setColor(ContextCompat.getColor(context!!, R.color.status_accepted_background))
                holder.ibRemoveCard.visibility = View.GONE
            }
            4  // Rejected
            -> {
                holder.txtstatus.setText(R.string.rejected)
                holder.txtstatus.setTextColor(ContextCompat.getColor(context!!, R.color.status_rejected))
                drawable.setColor(ContextCompat.getColor(context!!, R.color.status_rejected_background))
                if (ESPApplication.getInstance().isComponent || ESPApplication.getInstance()?.user?.loginResponse?.role?.toLowerCase().equals(context?.getString(R.string.applicantsmall), ignoreCase = true))
                    holder.ibRemoveCard.visibility = View.VISIBLE
                else
                    holder.ibRemoveCard.visibility = View.GONE
            }
            5  // Cancelled
            -> {
                holder.txtstatus.setText(R.string.cancelled)
                holder.txtstatus.setTextColor(ContextCompat.getColor(context!!, R.color.status_draft))
                drawable.setColor(ContextCompat.getColor(context!!, R.color.status_draft_background))
                holder.ibRemoveCard.visibility = View.GONE
            }
            else -> {
                holder.txtstatus.setText(R.string.inprogress)
                holder.txtstatus.setTextColor(ContextCompat.getColor(context!!, R.color.status_pending))
                drawable.setColor(ContextCompat.getColor(context!!, R.color.status_pending_background))
                holder.ibRemoveCard.visibility = View.GONE
            }
        }
    }

    private fun appDetail(applicationsDAO: ApplicationsDAO, isResubmit: Boolean) {
        CustomLogs.displayLogs(LOG_TAG + " mApplications.getStatus(): " + applicationsDAO.status!!.toLowerCase())

        val status = applicationsDAO.status!!.toLowerCase()
        val statusId = applicationsDAO.statusId


        if (ESPApplication.getInstance().isComponent ||ESPApplication.getInstance()?.user?.loginResponse?.role?.toLowerCase().equals(context?.getString(R.string.applicantsmall), ignoreCase = true)
                || isSubApplications) {
            val bundle = Bundle()
            bundle.putSerializable(ApplicationsDAO.BUNDLE_KEY, applicationsDAO)
            bundle.putString("appStatus", status)
            bundle.putInt("statusId", statusId)
            bundle.putBoolean("isResubmit", isResubmit)
            bundle.putBoolean("isSubApplications", isSubApplications)
            Shared.getInstance().callIntentWithResult(ApplicationDetailScreenActivity::class.java, context, bundle, 2)
        } else {

            if (ApplicationSingleton.instace.application != null) {
                ApplicationSingleton.instace.application = null
            }
            val bundle = Bundle()
            bundle.putSerializable(ApplicationsDAO.BUNDLE_KEY, applicationsDAO)
            bundle.putString("appStatus", status)
            bundle.putInt("statusId", statusId)
            bundle.putBoolean("isComingfromAssessor", true)
            Shared.getInstance().callIntentWithResult(ApplicationDetailScreenActivity::class.java, context, bundle, 2)
        }
    }

    override fun getItemCount(): Int {
        return mApplications?.size ?: 0
    }


    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun GetApplicationFeedBack(id: String, holder: ActivitiesList) {

        val apis = Shared.getInstance().retroFitObject(context)

        val detail_call = apis.GetApplicationFeedBack(id)
        detail_call.enqueue(object : Callback<List<ApplicationsFeedbackDAO>> {
            override fun onResponse(call: Call<List<ApplicationsFeedbackDAO>>, response: Response<List<ApplicationsFeedbackDAO>>?) {

                if (response?.body() != null && response.body().size > 0) {
                    for (i in 0 until response.body().size) {
                        val comment = response.body()[i].comment
                        /*val reasonTextConcate = context?.getString(R.string.reasonfordecline) + " " + comment
                        val wordtoSpan: Spannable = SpannableString(reasonTextConcate)
                        wordtoSpan.setSpan(ForegroundColorSpan(ContextCompat.getColor(context!!, R.color.coolgrey)), 0, 22, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        wordtoSpan.setSpan(ForegroundColorSpan(ContextCompat.getColor(context!!, R.color.black)), 23, reasonTextConcate.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
*/
                        if (comment.isNullOrEmpty())
                            holder.rlreason.visibility = View.GONE
                        else
                            holder.reasontextvalue.text = comment;


                    }


                }
            }

            override fun onFailure(call: Call<List<ApplicationsFeedbackDAO>>, t: Throwable) {


                // Shared.getInstance().showAlertMessage(pref.getlabels().getApplication(), getString(R.string.some_thing_went_wrong), bContext);

            }
        })


    }


    companion object {

        private val LOG_TAG = "ListUsersApplicationsAdapter"
        var isSubApplications: Boolean = false;

    }
}
