package utilities.adapters.setup.applications

import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import com.esp.library.R
import com.esp.library.utilities.common.Shared
import com.esp.library.utilities.common.SharedPreference
import com.esp.library.exceedersesp.controllers.applications.ActivityStageDetails
import com.esp.library.utilities.common.Enums
import com.esp.library.utilities.setup.applications.ApplicationCriteriaAdapter
import com.google.gson.Gson
import utilities.data.applicants.dynamics.DynamicFormSectionFieldDAO
import utilities.data.applicants.dynamics.DynamicResponseDAO
import utilities.data.applicants.dynamics.DynamicStagesCriteriaListDAO
import utilities.data.applicants.dynamics.DynamicStagesDAO
import java.util.*


class ApplicationStagesAdapter(iscomingfromAssessor: Boolean, val stagesList: MutableList<DynamicStagesDAO>,
                               actualresponseJson: String, con: Context, var nestedscrollview: NestedScrollView)
    : androidx.recyclerview.widget.RecyclerView.Adapter<ApplicationStagesAdapter.ParentViewHolder>() {


    var TAG = "ApplicationCriteriaAdapter"
    private var context: Context
    // var stagesList: List<DynamicStagesDAO>
    var actualResponseJson: String
    var isComingfromAssessor: Boolean = false
    public var criteriaAdapter: ApplicationCriteriaAdapter? = null;
    var criteriaListCollections = ArrayList<DynamicStagesCriteriaListDAO?>()
    var pref: SharedPreference? = null


    open class ParentViewHolder(v: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v)

    inner class ActivitiesList(v: View) : ParentViewHolder(v) {

        internal var rvCrietrias: androidx.recyclerview.widget.RecyclerView
        internal var rvExpandCrietrias: androidx.recyclerview.widget.RecyclerView
        internal var llstagesrow: LinearLayout
        internal var lldetail: LinearLayout
        internal var txtStagename: TextView
        internal var txtstatus: TextView
        internal var acceptedontext: TextView
        internal var acceptedonvalue: TextView
        internal var sequencetextvalue: TextView
        internal var acceptencetextvalue: TextView
        internal var acceptencetext: TextView
        internal var txtline: TextView
        internal var ivarrow: ImageView
        internal var rlaccepreject: RelativeLayout

        init {
            rvCrietrias = itemView.findViewById(R.id.rvCrietrias)
            rvExpandCrietrias = itemView.findViewById(R.id.rvExpandCrietrias)
            llstagesrow = itemView.findViewById(R.id.llstagesrow)
            lldetail = itemView.findViewById(R.id.lldetail)
            txtStagename = itemView.findViewById(R.id.txtStagename)
            txtstatus = itemView.findViewById(R.id.txtstatus)
            acceptedonvalue = itemView.findViewById(R.id.acceptedonvalue)
            acceptedontext = itemView.findViewById(R.id.acceptedontext)
            sequencetextvalue = itemView.findViewById(R.id.sequencetextvalue)
            acceptencetextvalue = itemView.findViewById(R.id.acceptencetextvalue)
            acceptencetext = itemView.findViewById(R.id.acceptencetext)
            txtline = itemView.findViewById(R.id.txtline)
            ivarrow = itemView.findViewById(R.id.ivarrow)
            rlaccepreject = itemView.findViewById(R.id.rlaccepreject)
        }
    }

    init {
        context = con
        isComingfromAssessor = iscomingfromAssessor
        actualResponseJson = actualresponseJson
        pref = SharedPreference(context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_stage_row, parent, false)
        return ActivitiesList(v)

    }


    override fun onBindViewHolder(holder_parent: ParentViewHolder, position: Int) {
        val holder = holder_parent as ActivitiesList
        val dynamicStagesDAO = stagesList.get(position)
        if (isComingfromAssessor) {


            holder.llstagesrow.visibility = View.VISIBLE
            holder.txtStagename.text = dynamicStagesDAO.name
            holder.sequencetextvalue.text = dynamicStagesDAO.order.toString()
            holder.txtstatus.text = dynamicStagesDAO.status


            /*  if (dynamicStagesDAO.isAll) {
                  holder.conditiontextvalue.text = context.getString(R.string.all)
              } else {
                  holder.conditiontextvalue.text = context.getString(R.string.any)
              }*/

            val displayDate = Shared.getInstance().getStageDisplayDate(context, dynamicStagesDAO.completedOn)
            if (displayDate.isNullOrEmpty())
                holder.rlaccepreject.visibility = View.GONE

            val actualResponse = Gson().fromJson(actualResponseJson, DynamicResponseDAO::class.java)
            if (actualResponse.applicationStatus.equals(Enums.rejected.toString(), ignoreCase = true)) // rejected
                holder.acceptedontext.text = context.getString(R.string.rejectedon)


            holder.acceptedonvalue.text = displayDate


            if (dynamicStagesDAO.linkDefinitionId > 0) {
                holder.acceptencetext.text = context.getString(R.string.subdefinition)
                holder.acceptencetextvalue.text = dynamicStagesDAO.linkDefinitionName
            } else {
                holder.acceptencetext.text = context.getString(R.string.acceptcriteria)
                if (dynamicStagesDAO.criteriaList != null && dynamicStagesDAO.criteriaList!!.isNotEmpty()) {
                    holder.acceptencetextvalue.text = dynamicStagesDAO.criteriaList!!.size.toString()
                } else {
                    holder.acceptencetextvalue.text = "0"
                }
            }

            setStatusColor(holder, dynamicStagesDAO, position)


            /*if (dynamicStagesDAO.status.equals(context.getString(R.string.open), ignoreCase = true))
                holder.ivarrow.post { holder.ivarrow.performClick() }*/


        } else {
            holder.llstagesrow.visibility = View.GONE

            for (i in 0 until dynamicStagesDAO.criteriaList!!.size) {
                val getList = dynamicStagesDAO.criteriaList?.get(i);
                val isArrayHasValue = criteriaListCollections.any { x -> x?.assessmentId == getList?.assessmentId }
                if (!isArrayHasValue) {
                    if (getList?.isEnabled!!)
                        criteriaListCollections.add(getList)
                }
            }

            if (position == (stagesList.size - 1)) {
                holder.rvCrietrias.setHasFixedSize(true)
                holder.rvCrietrias.isNestedScrollingEnabled = false
                holder.rvCrietrias.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
                criteriaAdapter = ApplicationCriteriaAdapter(criteriaListCollections, context, holder.rvCrietrias)
                criteriaAdapter?.getActualResponse(actualResponseJson)
                holder.rvCrietrias.adapter = criteriaAdapter
            }
        }


        if (dynamicStagesDAO.criteriaList?.size == 0) {
            holder.ivarrow.visibility = View.GONE
            holder.rvExpandCrietrias.visibility = View.GONE
        }

        holder.llstagesrow.setOnClickListener {


            /* if (holder.rvExpandCrietrias.visibility == View.GONE) {
                 holder.ivarrow.setImageResource(R.drawable.ic_arrow_up)
                 expandableCriterias(dynamicStagesDAO, holder)
                 holder.rvExpandCrietrias.visibility = View.VISIBLE
             } else
             {
                 holder.ivarrow.setImageResource(R.drawable.ic_arrow_down)
                 holder.rvExpandCrietrias.visibility = View.GONE
             }*/

            // if (!dynamicStagesDAO.status.equals(context.getString(R.string.locked), ignoreCase = true)) {
            //  if (dynamicStagesDAO.criteriaList!!.size > 0) {

            if ((!dynamicStagesDAO.status.equals(Enums.locked.toString(), ignoreCase = true))) {
                if (!dynamicStagesDAO.type.equals(Enums.link.toString(), ignoreCase = true)) {
                    if (holder.txtstatus.text != Enums.locked.toString()) {
                        val intent = Intent(context, ActivityStageDetails::class.java)
                        intent.putExtra("dynamicStagesDAO", dynamicStagesDAO)
                        intent.putExtra("actualResponseJson", actualResponseJson)
                        context.startActivity(intent)
                    }
                }
            }
        }


    }


    fun expandableCriterias(dynamicStagesDAO: DynamicStagesDAO, holder: ActivitiesList) {
        criteriaListCollections.clear()
        for (i in dynamicStagesDAO.criteriaList!!.indices) {
            val getList = dynamicStagesDAO.criteriaList?.get(i);
            val isArrayHasValue = criteriaListCollections.any { x -> x?.assessmentId == getList?.assessmentId }
            if (!isArrayHasValue) {
                if (getList?.isEnabled!!)
                    criteriaListCollections.add(getList)
            }
        }


        holder.rvExpandCrietrias.setHasFixedSize(true)
        holder.rvExpandCrietrias.isNestedScrollingEnabled = false
        holder.rvExpandCrietrias.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        criteriaAdapter = ApplicationCriteriaAdapter(criteriaListCollections, context, holder.rvExpandCrietrias)
        criteriaAdapter?.getStage(dynamicStagesDAO)
        criteriaAdapter?.getActualResponse(actualResponseJson)
        holder.rvExpandCrietrias.adapter = criteriaAdapter
    }

    override fun getItemCount(): Int {
        return stagesList.size
    }

    fun notifyChangeIfAny(criteriaId: Int) {
        criteriaAdapter?.notifyChangeIfAny(criteriaId)
    }

    private fun setStatusColor(holder: ActivitiesList, dynamicStagesDAO: DynamicStagesDAO, position: Int) {
        var status = dynamicStagesDAO.status?.toLowerCase(Locale.getDefault())
        val actualResponseJson = Gson().fromJson(actualResponseJson, DynamicResponseDAO::class.java)
        //  holder.lldetail.setBackgroundResource(R.drawable.draw_bg_white)

        if (actualResponseJson.applicationStatusId == 5) // cancalled
        {
            status = context.getString(R.string.locked)
            holder.txtstatus.setText(status)
        }
        if (status == null) {
            holder.txtline.setBackgroundColor(ContextCompat.getColor(context, R.color.status_new))
            return
        }
        holder.txtstatus.setBackgroundResource(R.drawable.status_background)
        val drawable = holder.txtstatus.background as GradientDrawable


        when (status.toLowerCase(Locale.getDefault())) {
            Enums.invited.toString() // Invited
            -> {
                holder.txtstatus.setTextColor(ContextCompat.getColor(context, R.color.status_invited))
                holder.txtline.setBackgroundColor(ContextCompat.getColor(context, R.color.status_invited))
                drawable.setColor(ContextCompat.getColor(context, R.color.status_invited_background))
            }
            Enums.newstatus.toString() // New
            -> {
                holder.txtstatus.setTextColor(ContextCompat.getColor(context, R.color.status_new))
                holder.txtline.setBackgroundColor(ContextCompat.getColor(context, R.color.status_new))
                drawable.setColor(ContextCompat.getColor(context, R.color.status_new_background))
            }
            Enums.pending.toString() // Pending
            -> {
                holder.txtstatus.setText(context.getString(R.string.inprogress))
                holder.txtstatus.setTextColor(ContextCompat.getColor(context, R.color.status_pending))
                holder.txtline.setBackgroundColor(ContextCompat.getColor(context, R.color.status_pending))
                drawable.setColor(ContextCompat.getColor(context, R.color.status_pending_background))
            }
            Enums.locked.toString() // locked
            -> {
              lockedCase(holder,dynamicStagesDAO,position,actualResponseJson,drawable)
            }

            Enums.completed.toString() // Completed
            -> {
                completeStage(dynamicStagesDAO, holder, drawable, actualResponseJson)
            }

            Enums.complete.toString() // Complete
            -> {
                completeStage(dynamicStagesDAO, holder, drawable, actualResponseJson)
            }

            Enums.rejected.toString()  // Rejected
            -> {

                holder.txtstatus.setTextColor(ContextCompat.getColor(context, R.color.status_rejected))
                holder.txtline.setBackgroundColor(ContextCompat.getColor(context, R.color.status_rejected))
                drawable.setColor(ContextCompat.getColor(context, R.color.status_rejected_background))
            }

            else -> {
                holder.txtstatus.setTextColor(ContextCompat.getColor(context, R.color.status_new))
                holder.txtline.setBackgroundColor(ContextCompat.getColor(context, R.color.status_new))
                drawable.setColor(ContextCompat.getColor(context, R.color.status_new_background))
            }
        }
    }

    private fun lockedCase(holder: ActivitiesList, dynamicStagesDAO: DynamicStagesDAO, position: Int, actualResponseJson: DynamicResponseDAO, drawable: GradientDrawable)
    {
        val linkText = Enums.link.toString()
        if (((position == 0 && dynamicStagesDAO.type.equals(linkText, ignoreCase = true))
                        || (actualResponseJson.applicationStatus.equals(Enums.rejected.toString(), ignoreCase = true) ||
                        actualResponseJson.applicationStatus.equals(Enums.accepted.toString(), ignoreCase = true)))
                || position > 0 && stagesList.get(position - 1).type.equals(Enums.completed.toString(), ignoreCase = true)
                && dynamicStagesDAO.type.equals(linkText, ignoreCase = true)) {
            holder.txtstatus.text = context.getString(R.string.completedcaps)

            if (actualResponseJson.applicationStatus.equals(Enums.rejected.toString(), ignoreCase = true)) {
                holder.txtstatus.setTextColor(ContextCompat.getColor(context, R.color.status_rejected))
                drawable.setColor(ContextCompat.getColor(context, R.color.status_rejected_background))
            } else if (actualResponseJson.applicationStatus.equals(Enums.accepted.toString(), ignoreCase = true)) {
                holder.txtstatus.setTextColor(ContextCompat.getColor(context, R.color.status_accepted))
                drawable.setColor(ContextCompat.getColor(context, R.color.status_accepted_background))
            } else {
                holder.txtstatus.setTextColor(ContextCompat.getColor(context, R.color.status_locked))
                drawable.setColor(ContextCompat.getColor(context, R.color.transparent_color))
            }

        }/* else if (!stagesList.get(position - 1).type.equals(context.getString(R.string.completed), ignoreCase = true)
                        && dynamicStagesDAO.type.equals(linkText, ignoreCase = true)) {

                }*/ else {
            holder.lldetail.setBackgroundResource(R.drawable.draw_bg_grey_stroke)
            holder.txtstatus.setTextColor(ContextCompat.getColor(context, R.color.status_locked))
            drawable.setColor(ContextCompat.getColor(context, R.color.status_locked_background))

        }
        holder.txtline.setBackgroundColor(ContextCompat.getColor(context, R.color.status_locked))

    }

    private fun completeStage(dynamicStagesDAO: DynamicStagesDAO, holder: ActivitiesList, drawable: GradientDrawable,
                              actualResponseJson: DynamicResponseDAO) {
        holder.txtstatus.setText(context.getString(R.string.completedcaps))

        var getAssessmentStatus = "";
        for (i in 0 until dynamicStagesDAO.criteriaList!!.size) {
            getAssessmentStatus = dynamicStagesDAO.criteriaList?.get(i)?.assessmentStatus.toString()
        }

        if (getAssessmentStatus.equals(Enums.rejected.toString(), ignoreCase = true) || actualResponseJson.applicationStatus.equals(Enums.rejected.toString(), ignoreCase = true)) {
            holder.txtstatus.setTextColor(ContextCompat.getColor(context, R.color.status_rejected))
            drawable.setColor(ContextCompat.getColor(context, R.color.status_rejected_background))
        } else if (getAssessmentStatus.equals(Enums.accepted.toString(), ignoreCase = true) || actualResponseJson.applicationStatus.equals(Enums.accepted.toString(), ignoreCase = true)) {
            holder.txtstatus.setTextColor(ContextCompat.getColor(context, R.color.status_accepted))
            drawable.setColor(ContextCompat.getColor(context, R.color.status_accepted_background))
        } else {
            holder.txtstatus.setTextColor(ContextCompat.getColor(context, R.color.status_accepted))
            holder.txtline.setBackgroundColor(ContextCompat.getColor(context, R.color.status_accepted))
            drawable.setColor(ContextCompat.getColor(context, R.color.status_accepted_background))
        }
    }


    fun getAllCriteriaFields(): List<DynamicFormSectionFieldDAO>? {
        if (stagesList != null && stagesList.size > 0) {
            val fields = ArrayList<DynamicFormSectionFieldDAO>()
            for (stageslist in stagesList) {
                for (criterialist in stageslist.criteriaList!!) {
                    for (sectionDAO in criterialist.form.sections!!) {
                        if (sectionDAO.fieldsCardsList != null) {
                            for (sectionFieldsCardsDAO in sectionDAO.fieldsCardsList!!) {
                                fields.addAll(sectionFieldsCardsDAO.fields!!)
                            }
                        }
                    }

                }

            }

            return fields

        }

        return null
    }
}
