package com.esp.library.utilities.setup.applications

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import com.esp.library.R
import com.esp.library.exceedersesp.BaseActivity
import com.esp.library.exceedersesp.ESPApplication
import com.esp.library.exceedersesp.controllers.applications.ActivityStageDetails
import com.esp.library.exceedersesp.controllers.applications.ApplicationDetailScreenActivity
import com.esp.library.exceedersesp.controllers.applications.AssessorApplicationStagesCeriteriaCommentsScreenActivity
import com.esp.library.exceedersesp.controllers.applications.UsersList
import com.esp.library.utilities.common.Enums
import com.esp.library.utilities.common.Shared
import com.esp.library.utilities.common.SharedPreference
import com.google.gson.Gson
import utilities.adapters.setup.applications.ListAddApplicationSectionsAdapter
import utilities.data.applicants.dynamics.*
import utilities.interfaces.FeedbackSubmissionClick
import java.util.*
import kotlin.collections.ArrayList


class ApplicationCriteriaAdapter(val criterialist: List<DynamicStagesCriteriaListDAO?>, con: Context, rvCrietria: androidx.recyclerview.widget.RecyclerView)
    : androidx.recyclerview.widget.RecyclerView.Adapter<ApplicationCriteriaAdapter.ParentViewHolder>() {


    var TAG = "ApplicationCriteriaAdapter"
    private var context: Context? = null
    internal var imm: InputMethodManager? = null
    //  var criteriaList: List<DynamicStagesCriteriaListDAO>?
    var actualResponseJson: String? = null
    var isViewOnly: Boolean = false
    var listener: FeedbackSubmissionClick? = null
    var rvCrietrias: androidx.recyclerview.widget.RecyclerView? = null
    var isNotifyOnly: Boolean = false
    var isComingfromAssessor: Boolean = false
    var mApplicationSectionsAdapter: ListAddApplicationSectionsAdapter? = null
    val POPUP_CONSTANT = "mPopup"
    val POPUP_FORCE_SHOW_ICON = "setForceShowIcon"
    var dynamicStagesDAO: DynamicStagesDAO? = null
    var isNotifyOnlyPosition: Int = 0
    var criteriaList: List<DynamicStagesCriteriaListDAO?>? = null
    var pref: SharedPreference? = null

    init {
        context = con
        pref = SharedPreference(context)
        criteriaList = criterialist
        rvCrietrias = rvCrietria
        try {
            listener = context as FeedbackSubmissionClick
        } catch (e: java.lang.Exception) {

        }
        if (ESPApplication.getInstance()?.user?.loginResponse?.role?.toLowerCase() != context!!.getString(R.string.applicantsmall))
            isComingfromAssessor = true
    }


    fun getStage(dynamicstagesDAO: DynamicStagesDAO) {
        dynamicStagesDAO = dynamicstagesDAO
    }

    fun setCriterias(criterialist: List<DynamicStagesCriteriaListDAO?>) {
        criteriaList = criterialist

        for (i in criteriaList!!.indices) {
            val getForm = criterialist.get(i)?.form
            val getFormvalues = criterialist.get(i)?.formValues
            //Setting Sections With FieldsCards.
            var sectionDAO = DynamicFormSectionDAO()
            if (getForm?.sections != null && getForm.sections!!.size > 0) {
                for (st in 0 until getForm.sections!!.size) {
                    sectionDAO = getForm.sections!![st]
                    if (sectionDAO.fields != null && sectionDAO.fields!!.size > 0) {
                        for (s in sectionDAO.fields!!.indices) {
                            for (j in getFormvalues!!.indices) {
                                val dynamicFormValuesDAO = getFormvalues.get(j)
                                if (dynamicFormValuesDAO.sectionCustomFieldId == sectionDAO.fields?.get(s)?.sectionCustomFieldId) {
                                    dynamicFormValuesDAO.value = sectionDAO.fields?.get(s)?.value
                                }
                            }
                        }

                    }

                }
            }
        }

    }

    open class ParentViewHolder(v: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v)

    inner class ActivitiesList(v: View) : ParentViewHolder(v) {

        internal var rvFields: androidx.recyclerview.widget.RecyclerView
        internal var txtcrierianame: TextView
        internal var ivainforrow: ImageView
        internal var durationtextvalue: TextView
        internal var ownertextvalue: TextView
        internal var txtstatus: TextView
        internal var txtmoreinfo: TextView
        internal var txtline: TextView
        internal var rlacceptapprove: LinearLayout
        internal var lldetail: RelativeLayout
        internal var rlmoreinfo: RelativeLayout
        internal var btapprove: Button
        internal var btreject: Button
        internal var add_criteria_comments: ImageButton
        internal var ibReassignCard: ImageButton
        internal var pendinglineview: View

        init {
            pendinglineview = itemView.findViewById(R.id.pendinglineview)
            txtcrierianame = itemView.findViewById(R.id.txtcrierianame)
            ivainforrow = itemView.findViewById(R.id.ivainforrow)
            rvFields = itemView.findViewById(R.id.rvFields)
            durationtextvalue = itemView.findViewById(R.id.durationtextvalue)
            ownertextvalue = itemView.findViewById(R.id.ownertextvalue)
            txtstatus = itemView.findViewById(R.id.txtstatus)
            txtmoreinfo = itemView.findViewById(R.id.txtmoreinfo)
            txtline = itemView.findViewById(R.id.txtline)
            rlacceptapprove = itemView.findViewById(R.id.rlacceptapprove)
            btapprove = itemView.findViewById(R.id.btapprove)
            btreject = itemView.findViewById(R.id.btreject)
            rlmoreinfo = itemView.findViewById(R.id.rlmoreinfo)
            add_criteria_comments = itemView.findViewById(R.id.add_criteria_comments)
            ibReassignCard = itemView.findViewById(R.id.ibReassignCard)
            lldetail = itemView.findViewById(R.id.lldetail)
        }
    }


    fun getActualResponse(actualresponseJson: String) {
        actualResponseJson = actualresponseJson
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_criteria_row, parent, false)
        return ActivitiesList(view)
    }

    fun notifyOnly(position: Int) {
        isNotifyOnly = true;
        isNotifyOnlyPosition = position
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder_parent: ParentViewHolder, position: Int) {
        val holder = holder_parent as ActivitiesList

        val criteriaListDAO = criteriaList?.get(position)

        holder.rvFields.setHasFixedSize(true)
        holder.rvFields.isNestedScrollingEnabled = false
        val linearLayoutManagerCrteria = androidx.recyclerview.widget.LinearLayoutManager(context, androidx.recyclerview.widget.LinearLayoutManager.VERTICAL, false)
        holder.rvFields.layoutManager = linearLayoutManagerCrteria

        holder.txtcrierianame.text = criteriaListDAO?.name
        var daysVal = context?.getString(R.string.day)
        if (criteriaListDAO?.daysToComplete != null && criteriaListDAO.daysToComplete > 1)
            daysVal = context?.getString(R.string.days)

        holder.durationtextvalue.text = criteriaListDAO?.daysToComplete.toString() + " " + daysVal

        var criteriaOwner = criteriaListDAO?.ownerName;

        if (criteriaListDAO?.ownerName.isNullOrEmpty())
            criteriaOwner = context?.getString(R.string.applicant)

        holder.ownertextvalue.text = criteriaOwner
        //val assessmentStatus = criteriaListDAO?.assessmentStatus
        holder.txtstatus.text = criteriaListDAO?.assessmentStatus



        if (isNotifyOnly) {
            mApplicationSectionsAdapter?.notifyItemChanged(isNotifyOnlyPosition)
        }
        val sectionsStages = GetStagesFieldsCards(criteriaListDAO)
        val sections = ArrayList<DynamicFormSectionDAO>()
        for (j in 0 until sectionsStages.size) {
            if (sectionsStages.get(j).fields == null) {
                holder.rvFields.visibility = View.INVISIBLE
                break
            }
            sections.add(sectionsStages.get(j))
        }
        setStatusColor(holder, criteriaListDAO, sections, position)
        isViewOnly = !criteriaListDAO?.assessmentStatus.equals(context?.getString(R.string.active))

        if (criteriaListDAO?.type.equals(context?.getString(R.string.feedback), ignoreCase = true))
            holder.btreject.visibility = View.GONE
        else
            holder.btreject.visibility = View.VISIBLE

        if (!criteriaListDAO?.approveText.isNullOrEmpty())
            holder.btapprove.text = criteriaListDAO?.approveText

        if (!criteriaListDAO?.rejectText.isNullOrEmpty())
            holder.btreject.text = criteriaListDAO?.rejectText


        if (criteriaListDAO?.comments != null && criteriaListDAO.comments!!.size > 0) {
            holder.add_criteria_comments.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_card_commented))
        } else {
            holder.add_criteria_comments.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_card_not_commented))
        }



        mApplicationSectionsAdapter = ListAddApplicationSectionsAdapter(sections, (context as BaseActivity?)!!, "", isViewOnly)
        actualResponseJson?.let { mApplicationSectionsAdapter?.setActualResponseJson(it) }
        holder.rvFields.setAdapter(mApplicationSectionsAdapter)
        mApplicationSectionsAdapter?.getCriteriaObject(criteriaListDAO)
        try {
            mApplicationSectionsAdapter?.setmApplicationFieldsAdapterListener2(context as ApplicationDetailScreenActivity)
        } catch (e: java.lang.Exception) {
            mApplicationSectionsAdapter?.setmApplicationFieldsAdapterListener2(context as ActivityStageDetails)
        }


        holder.rvFields.visibility = View.GONE
        holder.rlacceptapprove.visibility = View.GONE
        holder.pendinglineview.visibility = View.GONE

        if (!ApplicationDetailScreenActivity.criteriaWasLoaded)
            iterateOnSection(sections, holder, criteriaListDAO)

        holder.rlmoreinfo.setOnClickListener {
            if (!criteriaListDAO?.assessmentStatus.equals(context?.getString(R.string.reassigned), ignoreCase = true)) {

                if (holder.txtmoreinfo.text.equals(context?.getString(R.string.moreinformation))) {
                    moreInfo(holder, criteriaListDAO, position, sections)
                } else {
                    holder.txtmoreinfo.text = context?.getString(R.string.moreinformation)
                    holder.ivainforrow.setImageResource(R.drawable.ic_more_info_down)
                    holder.rvFields.visibility = View.GONE
                    holder.rlacceptapprove.visibility = View.GONE
                }
            }
        }

        holder.btapprove.setOnClickListener {
            listener?.feedbackClick(true, criteriaListDAO, dynamicStagesDAO, position)
        }
        holder.btreject.setOnClickListener {
            listener?.feedbackClick(false, criteriaListDAO, dynamicStagesDAO, position)
        }

        if (holder.txtmoreinfo.text.equals(context?.getString(R.string.lessinformation))) {
            moreInfo(holder, criteriaListDAO, position, sections)
        }

        if (!isComingfromAssessor)
            holder.add_criteria_comments.visibility = View.GONE



        holder.add_criteria_comments.setOnClickListener {
            if (!criteriaListDAO?.assessmentStatus.equals(context?.getString(R.string.reassigned), ignoreCase = true)) {
                val bundle = Bundle()
                bundle.putSerializable(DynamicStagesCriteriaListDAO.BUNDLE_KEY, criteriaListDAO)
                Shared.getInstance().callIntentWithResult(AssessorApplicationStagesCeriteriaCommentsScreenActivity::class.java, (context as Activity?), bundle, 2)
            }
        }




        if (pref?.selectedUserRole.equals(Enums.assessor.toString(), ignoreCase = true))
            isComingfromAssessor = true

        if ((ESPApplication.getInstance()?.user?.role.equals(context?.getString(R.string.admin), ignoreCase = true)) ||
                ((isComingfromAssessor && criteriaListDAO?.isOwner!!) && (dynamicStagesDAO?.status.equals(context?.getString(R.string.open), ignoreCase = true)
                        || dynamicStagesDAO?.status.equals(context?.getString(R.string.locked), ignoreCase = true)) &&
                        !criteriaListDAO.assessmentStatus.equals(context?.getString(R.string.accepted), ignoreCase = true) &&
                        !criteriaListDAO.assessmentStatus.equals(context?.getString(R.string.rejected), ignoreCase = true)))
            holder.ibReassignCard.visibility = View.VISIBLE
        else
            holder.ibReassignCard.visibility = View.GONE

        holder.ibReassignCard.setOnClickListener { v ->
            if (!criteriaListDAO?.assessmentStatus.equals(context?.getString(R.string.reassigned), ignoreCase = true))
                popUpMenu(v, criteriaListDAO)
        }


    }//End Holder Class


    private fun moreInfo(holder: ActivitiesList, criteriaListDAO: DynamicStagesCriteriaListDAO?, position: Int,
                         sections: ArrayList<DynamicFormSectionDAO>) {
        holder.txtmoreinfo.text = context?.getString(R.string.lessinformation)
        holder.ivainforrow.setImageResource(R.drawable.ic_more_info_up)
        holder.rvFields.visibility = View.VISIBLE

        if (sections != null && sections.size > 0) {
            for (sectionFields in sections) {
                if (sectionFields.fieldsCardsList != null || sectionFields.fieldsCardsList!!.isNotEmpty()) {
                    for (i in 0 until sectionFields.fieldsCardsList!!.size) {
                        if (sectionFields.fieldsCardsList?.get(i)?.fields?.size!! > 0) {
                            if (criteriaListDAO?.assessmentStatus.equals(context?.getString(R.string.active), ignoreCase = true)) {
                                holder.txtmoreinfo.visibility = View.VISIBLE
                                holder.ivainforrow.visibility = View.VISIBLE
                            }
                        }

                    }
                }
            }
        } else {
            if (!isNotifyOnly) {
                holder.txtmoreinfo.visibility = View.GONE
                holder.ivainforrow.visibility = View.GONE
            }
        }


        actionButtons(criteriaListDAO, holder, position)
    }

    private fun actionButtons(criteriaListDAO: DynamicStagesCriteriaListDAO?, holder: ActivitiesList, position: Int) {
        val actualResponseJson = Gson().fromJson(actualResponseJson, DynamicResponseDAO::class.java)
        if (criteriaListDAO?.assessmentStatus.equals(context?.getString(R.string.active), ignoreCase = true)
                && criteriaListDAO!!.isOwner && !actualResponseJson.applicationStatus.equals(context?.getString(R.string.rejected), ignoreCase = true)) {
            holder.rlacceptapprove.visibility = View.VISIBLE
            holder.pendinglineview.visibility = View.VISIBLE
        } else {
            holder.rlacceptapprove.visibility = View.GONE
        }
    }

    private fun iterateOnSection(sections: ArrayList<DynamicFormSectionDAO>, holder: ActivitiesList, criteriaListDAO: DynamicStagesCriteriaListDAO?) {
        if (sections != null && sections.size > 0) {
            for (sectionFields in sections) {
                for (i in 0 until sectionFields.fieldsCardsList!!.size) {
                    if (sectionFields.fieldsCardsList?.get(i)?.fields!!.size > 0) {
                        if (criteriaListDAO?.assessmentStatus.equals(context?.getString(R.string.active), ignoreCase = true)) {
                            holder.rvFields.visibility = View.GONE
                            holder.txtmoreinfo.visibility = View.GONE
                            holder.ivainforrow.visibility = View.GONE
                        } /*else {
                    holder.rlmoreinfo.visibility = View.GONE
                }*/
                    } else
                        holder.rlmoreinfo.visibility = View.GONE
                }
            }
        } else {
            if (!criteriaListDAO!!.isOwner)
                holder.rlmoreinfo.visibility = View.GONE
            else {
                holder.txtmoreinfo.visibility = View.GONE
                holder.ivainforrow.visibility = View.GONE
            }
        }
    }

    private fun GetStagesFieldsCards(stages: DynamicStagesCriteriaListDAO?): List<DynamicFormSectionDAO> {

        val sections = ArrayList<DynamicFormSectionDAO>()

        val getForm = stages?.form
        //Setting Sections With FieldsCards.
        var sectionDAO = DynamicFormSectionDAO()
        if (getForm?.sections != null && getForm.sections!!.size > 0) {
            for (st in 0 until getForm.sections!!.size) {
                sectionDAO = getForm.sections!![st]
                if (sectionDAO.fields != null && sectionDAO.fields!!.size > 0) {
                    sectionDAO.defaultName = stages.name
                    if (stages.formValues.size > 0) {
                        for (j in 0 until stages.formValues.size) {
                            val getSectionCustomFieldId = stages.formValues[j].sectionCustomFieldId
                            for (i in 0 until sectionDAO.fields!!.size) {


                                val sectionCustomFieldId = sectionDAO.fields!![i].sectionCustomFieldId
                                val getType = sectionDAO.fields!![i].type
                                if (sectionCustomFieldId == getSectionCustomFieldId) {
                                    var value = stages.formValues[j].value

                                    if (getType == 13) // lookupvalue
                                    {
                                        val fieldsCardsList = stages.form.sections?.get(0)?.fields?.get(j)
                                        //stages.formValues.get(j)=fieldsCardsList

                                        // value = stages.formValues[j].selectedLookupText

                                        /*  value = stages.formValues[j].selectedLookupText
                                          if (value == null)
                                              value = stages.formValues[j].value

                                          sectionDAO.fields!![i].lookupValue = value
                                          if (stages.formValues[j].value != null && Shared.getInstance().isNumeric(stages.formValues[j].value))
                                              sectionDAO.fields!![i].id = Integer.parseInt(stages.formValues[j].value!!)*/


                                        value = fieldsCardsList?.lookupValue
                                        if (value == null)
                                            value = stages.formValues[j].selectedLookupText

                                        sectionDAO.fields!![i].lookupValue = value
                                        if (stages.formValues[j].value != null && Shared.getInstance().isNumeric(stages.formValues[j].value))
                                            sectionDAO.fields!![i].id = Integer.parseInt(stages.formValues[j].value!!)

                                    }
                                    //  sectionDAO.fields!![i].value = value
                                    if (getType == 7) { // for attachments only
                                        try {
                                            val details = DyanmicFormSectionFieldDetailsDAO()
                                            details.downloadUrl = stages.formValues[j].details!!.downloadUrl
                                            details.mimeType = stages.formValues[j].details!!.mimeType
                                            details.createdOn = stages.formValues[j].details!!.createdOn
                                            details.name = stages.formValues[j].details!!.name
                                            sectionDAO.fields!![i].details = details
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }

                                    }

                                    sectionDAO.fields!![i].value = value
                                    sectionDAO.fields!![i].sectionId = sectionDAO.id

                                }
                            }

                        }
                        Shared.getInstance().loadFeedback(sectionDAO, sections, stages)
                    } else {
                        Shared.getInstance().loadFeedback(sectionDAO, sections, stages)
                    }
                }

            }
        } else {
            Shared.getInstance().loadFeedback(sectionDAO, sections, stages)
        }


        return sections
    }

    private fun setStatusColor(holder: ActivitiesList, criteriaListDAO: DynamicStagesCriteriaListDAO?,
                               sections: ArrayList<DynamicFormSectionDAO>,
                               position: Int) {
        val assessmentStatus = criteriaListDAO?.assessmentStatus
        val actualResponseJson = Gson().fromJson(actualResponseJson, DynamicResponseDAO::class.java)

        if (ESPApplication.getInstance().isComponent || ESPApplication.getInstance()?.user?.loginResponse?.role?.toLowerCase(Locale.getDefault()) == Enums.assessor.toString())
            holder.lldetail.setBackgroundResource(R.drawable.draw_bg_white)
        else
            holder.lldetail.setBackgroundResource(R.drawable.draw_bg_white_grey_stroke)

        if (assessmentStatus == null) {
            holder.txtline.setBackgroundColor(ContextCompat.getColor(context!!, R.color.status_new))
            return
        };

        if (actualResponseJson.applicationStatusId == 5) // cancalled
        {
            val status = context?.getString(R.string.cancelled)
            holder.txtstatus.setText(status)
            holder.ivainforrow.visibility = View.GONE
            holder.rlacceptapprove.visibility = View.GONE
            holder.rlmoreinfo.visibility = View.GONE
            holder.txtstatus.setTextColor(ContextCompat.getColor(context!!, R.color.status_draft))
            holder.txtline.setBackgroundColor(ContextCompat.getColor(context!!, R.color.status_draft_background))
            return
        }

        // holder.txtstatus.setBackgroundResource(R.drawable.status_background)
        // val drawable = holder.txtstatus.getBackground() as GradientDrawable

        when (assessmentStatus) {
            context?.getString(R.string.invited) // Invited
            -> {
                holder.txtstatus.setTextColor(ContextCompat.getColor(context!!, R.color.status_invited))
                holder.txtline.setBackgroundColor(ContextCompat.getColor(context!!, R.color.status_invited))
                //   drawable.setColor(ContextCompat.getColor(context!!, R.color.status_invited_background))
            }
            context?.getString(R.string.neww) // New
            -> {
                holder.txtstatus.setTextColor(ContextCompat.getColor(context!!, R.color.status_new))
                holder.txtline.setBackgroundColor(ContextCompat.getColor(context!!, R.color.status_new))
                //   drawable.setColor(ContextCompat.getColor(context!!, R.color.status_new_background))
            }
            context?.getString(R.string.pending) // Pending
            -> {
                holder.txtstatus.text = context!!.getString(R.string.inprogress)
                holder.txtstatus.setTextColor(ContextCompat.getColor(context!!, R.color.status_pending))
                holder.txtline.setBackgroundColor(ContextCompat.getColor(context!!, R.color.status_pending))
                //   drawable.setColor(ContextCompat.getColor(context!!, R.color.status_pending_background))
            }
            context?.getString(R.string.inprogress) // inprogress
            -> {
                holder.txtstatus.text = context!!.getString(R.string.inprogress)
                holder.txtstatus.setTextColor(ContextCompat.getColor(context!!, R.color.status_pending))
                holder.txtline.setBackgroundColor(ContextCompat.getColor(context!!, R.color.status_pending))
                //  drawable.setColor(ContextCompat.getColor(context!!, R.color.status_pending_background))
            }
            context?.getString(R.string.accepted) // Accepted
            -> {
                holder.txtstatus.setTextColor(ContextCompat.getColor(context!!, R.color.status_accepted))
                holder.txtline.setBackgroundColor(ContextCompat.getColor(context!!, R.color.status_accepted))
                //  drawable.setColor(ContextCompat.getColor(context!!, R.color.status_accepted_background))
            }
            context?.getString(R.string.rejected)  // Rejected
            -> {

                holder.txtstatus.setTextColor(ContextCompat.getColor(context!!, R.color.status_rejected))
                holder.txtline.setBackgroundColor(ContextCompat.getColor(context!!, R.color.status_rejected))
                //  drawable.setColor(ContextCompat.getColor(context!!, R.color.status_rejected_background))
            }

            context?.getString(R.string.cancelled)   // Cancelled
            -> {
                holder.txtstatus.setTextColor(ContextCompat.getColor(context!!, R.color.status_draft))
                holder.txtline.setBackgroundColor(ContextCompat.getColor(context!!, R.color.status_draft_background))
            }
            context?.getString(R.string.reassigned)  // reassign
            -> {

                holder.lldetail.setBackgroundResource(R.drawable.draw_bg_grey_stroke)
                holder.txtstatus.setTextColor(ContextCompat.getColor(context!!, R.color.coolgrey))
                holder.txtline.setBackgroundColor(ContextCompat.getColor(context!!, R.color.coolgrey))
                //   drawable.setColor(ContextCompat.getColor(context!!, R.color.transparent_color))
            }

            context?.getString(R.string.activecaps)  // Active
            -> {


                if (actualResponseJson.applicationStatus.equals(context?.getString(R.string.rejected), ignoreCase = true)) {
                    holder.txtstatus.setTextColor(ContextCompat.getColor(context!!, R.color.status_rejected))
                    holder.txtstatus.setText(context?.getString(R.string.rejected))
                    holder.txtline.setBackgroundColor(ContextCompat.getColor(context!!, R.color.status_rejected))
                } else if (!criteriaListDAO.isOwner) {
                    holder.txtstatus.setTextColor(ContextCompat.getColor(context!!, R.color.status_pending))
                    holder.txtstatus.text = context?.getString(R.string.inprogress)
                    holder.txtline.setBackgroundColor(ContextCompat.getColor(context!!, R.color.status_pending))
                    //   drawable.setColor(ContextCompat.getColor(context!!, R.color.status_pending_background))
                } else {

                    if (isNotifyOnly) {
                        if (holder.txtmoreinfo.text.equals(context?.getString(R.string.lessinformation))) {
                            moreInfo(holder, criteriaListDAO, isNotifyOnlyPosition, sections)
                        }
                    } else {
                        holder.rlmoreinfo.post {

                            if (holder.txtmoreinfo.text.equals(context?.getString(R.string.moreinformation)) &&
                                    actualResponseJson.stageVisibilityApplicant.equals(context?.getString(R.string.no), ignoreCase = true)) {
                                holder.rlmoreinfo.performClick()
                            } else if (holder.txtmoreinfo.text.equals(context?.getString(R.string.moreinformation)) && !ApplicationDetailScreenActivity.criteriaWasLoaded) {
                                if ((criteriaList!!.size - 1) == position)
                                    ApplicationDetailScreenActivity.criteriaWasLoaded = true
                                holder.rlmoreinfo.performClick()
                            }
                        }
                    }



                    holder.txtstatus.setTextColor(ContextCompat.getColor(context!!, R.color.status_new))
                    holder.txtstatus.text = context?.getString(R.string.opencaps)
                    holder.txtline.setBackgroundColor(ContextCompat.getColor(context!!, R.color.status_new))
                    //   drawable.setColor(ContextCompat.getColor(context!!, R.color.status_new_background))
                }
            }
            else -> {
                holder.txtstatus.text = context!!.getString(R.string.inprogress)
                holder.txtstatus.setTextColor(ContextCompat.getColor(context!!, R.color.status_pending))
                holder.txtline.setBackgroundColor(ContextCompat.getColor(context!!, R.color.status_pending))
            }
        }
    }

    override fun getItemCount(): Int {
        return criteriaList!!.size
    }

    fun notifyChangeIfAny(criteriaId: Int) {
        for (i in criteriaList!!.indices) {
            if (criteriaId == criteriaList?.get(i)?.id) {
                val isValidate = criteriaList?.get(i)?.isValidate
                val tempBtApprove = rvCrietrias?.layoutManager?.findViewByPosition(i)?.findViewById<Button>(R.id.btapprove)
                val tempBtReject = rvCrietrias?.layoutManager?.findViewByPosition(i)?.findViewById<Button>(R.id.btreject)
                checkButtonStatus(tempBtApprove, tempBtReject, isValidate)

            }
        }


    }

    private fun checkButtonStatus(tempBtApprove: Button?, tempBtReject: Button?, isEnable: Boolean?) {
        when (isEnable) {
            true -> {
                tempBtApprove?.isEnabled = true
                tempBtApprove?.alpha = 1f
            }
            false -> {
                tempBtApprove?.isEnabled = false
                tempBtApprove?.alpha = 0.5f

            }

        }
        tempBtReject?.isEnabled = true
        tempBtReject?.alpha = 1f

    }

    private fun popUpMenu(view: View, criteriaListDAO: DynamicStagesCriteriaListDAO?) {
        //creating a popup menu
        val popup = PopupMenu(context!!, view)
        popup.gravity = Gravity.CENTER

        try {
            // Reflection apis to enforce show icon
            val fields = popup.javaClass.declaredFields
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

        //inflating menu from xml resource
        popup.inflate(R.menu.menu_reassign)
        //adding click listener
        popup.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_reassign) {

                var intent = Intent(context, UsersList::class.java)
                intent.putExtra("criteriaListDAO", criteriaListDAO)
                context?.startActivity(intent)


            }
            false
        }
        //displaying the popup
        popup.show()
    }

    fun getAllCriteriaFields(): List<DynamicFormSectionFieldDAO>? {
        val fields = java.util.ArrayList<DynamicFormSectionFieldDAO>()
        for (criterialist in criteriaList!!) {
            for (sectionDAO in criterialist?.form?.sections!!) {
                if (sectionDAO.fieldsCardsList != null) {
                    for (sectionFieldsCardsDAO in sectionDAO.fieldsCardsList!!) {
                        fields.addAll(sectionFieldsCardsDAO.fields!!)
                    }
                }
            }

        }

        return fields
    }


}
