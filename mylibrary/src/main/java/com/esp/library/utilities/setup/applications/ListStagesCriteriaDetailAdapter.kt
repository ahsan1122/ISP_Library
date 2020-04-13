package utilities.adapters.setup.applications

import android.os.Bundle
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.esp.library.R
import com.esp.library.utilities.common.CustomLogs
import com.esp.library.utilities.common.Shared
import com.esp.library.exceedersesp.BaseActivity
import com.esp.library.exceedersesp.controllers.applications.AssessorApplicationStagesCeriteriaCommentsScreenActivity
import utilities.data.applicants.dynamics.DynamicStagesCriteriaListDAO
import utilities.data.applicants.dynamics.DynamicStagesDAO


class ListStagesCriteriaDetailAdapter(private val mCriterias: List<DynamicStagesCriteriaListDAO>?, mstage: DynamicStagesDAO?, con: BaseActivity, internal var searched_text: String) : androidx.recyclerview.widget.RecyclerView.Adapter<ListStagesCriteriaDetailAdapter.ParentViewHolder>() {

    internal var mstatus: CriteriaStatusChange? = null
    private var context: BaseActivity
    var mStage: DynamicStagesDAO? = null;

    interface CriteriaStatusChange {
        fun StatusChange(update: DynamicStagesCriteriaListDAO, isAccepted: Boolean)
    }

    open class ParentViewHolder(v: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v)

    inner class ActivitiesList(v: View) : ParentViewHolder(v) {

        internal var cards: LinearLayout
        internal var status_dot: RelativeLayout
        internal var criteria_name: TextView
        internal var days: TextView
        internal var criteria_owner: TextView
        internal var status: TextView
        internal var add_criteria_comments: ImageButton
        internal var add_criteria_action: ImageButton


        init {

            cards = itemView.findViewById(R.id.cards)
            status_dot = itemView.findViewById(R.id.status_dot)
            criteria_name = itemView.findViewById(R.id.criteria_name)
            status = itemView.findViewById(R.id.status)
            days = itemView.findViewById(R.id.days)
            criteria_owner = itemView.findViewById(R.id.criteria_owner)
            add_criteria_comments = itemView.findViewById(R.id.add_criteria_comments)
            add_criteria_action = itemView.findViewById(R.id.add_criteria_action)
        }

    }


    init {
        context = con
        mStage = mstage

        try {
            mstatus = context as CriteriaStatusChange
        } catch (e: ClassCastException) {
            throw ClassCastException("lisnter" + " must implement on Activity")
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentViewHolder {
        val v: View
        v = LayoutInflater.from(parent.context).inflate(R.layout.repeater_application_stages_criteria, parent, false)
        return ActivitiesList(v)
    }


    override fun onBindViewHolder(holder_parent: ParentViewHolder, position: Int) {

        val holder = holder_parent as ActivitiesList

        holder.criteria_name.text = mCriterias!![position].name

        var noDays = " " + context.getString(R.string.day)
        if (mCriterias[position].daysToComplete <= 1) {
            noDays = " " + context.getString(R.string.day)
        } else if (mCriterias[position].daysToComplete > 1) {
            noDays = " " + context.getString(R.string.day)
        }
        holder.days.text = mCriterias[position].daysToComplete.toString() + noDays
        try {
            val manager_name = Shared.getInstance().toSubStr(mCriterias[position].ownerName, 20)
            holder.criteria_owner.text = manager_name

        } catch (e: Exception) {

        }
        CustomLogs.displayLogs("mStage?.status: " + mStage?.status)


        if (mCriterias[position].assessmentStatus != null && mCriterias[position].assessmentStatus!!.length > 0) {
            holder.status.visibility = View.VISIBLE
            if (mCriterias[position].assessmentStatus!!.toLowerCase().equals(context.getString(R.string.active), ignoreCase = true) || mCriterias[position].assessmentStatus!!.toLowerCase().equals(context.getString(R.string.neww), ignoreCase = true)) {
                holder.status.setText(R.string.inprogress)

                holder.status_dot.setBackgroundDrawable(context.resources.getDrawable(R.drawable.draw_bg_pending))

                if (mCriterias[position].isOwner) {
                    holder.status.visibility = View.GONE
                    holder.add_criteria_action.visibility = View.VISIBLE
                    holder.add_criteria_action.setOnClickListener { view -> ShowMenu(view, mCriterias[position]) }
                }


            } else if (mCriterias[position].assessmentStatus!!.toLowerCase().equals(context.getString(R.string.rejectedsmall), ignoreCase = true)) {
                holder.status.setText(R.string.rejected)
                holder.status_dot.background = context.resources.getDrawable(R.drawable.draw_bg_rejected)
            } else if (mCriterias[position].assessmentStatus!!.toLowerCase().equals(context.getString(R.string.acceptedsmall), ignoreCase = true)) {
                holder.status.setText(R.string.accepted)
                holder.status_dot.background = context.resources.getDrawable(R.drawable.draw_bg_accepted)
            } else {
                holder.status_dot.background = context.resources.getDrawable(R.drawable.draw_bg_pending)
                holder.status.visibility = View.GONE
            }

        } else {
            holder.status.visibility = View.GONE
        }




        if (mCriterias[position].comments != null && mCriterias[position].comments!!.size > 0) {
            holder.add_criteria_comments.setImageDrawable(context.resources.getDrawable(R.drawable.ic_card_commented))
        } else {
            holder.add_criteria_comments.setImageDrawable(context.resources.getDrawable(R.drawable.ic_card_not_commented))
        }

        holder.add_criteria_comments.setOnClickListener {
            val bundle = Bundle()
            bundle.putSerializable(DynamicStagesCriteriaListDAO.BUNDLE_KEY, mCriterias[position])
            Shared.getInstance().callIntentWithResult(AssessorApplicationStagesCeriteriaCommentsScreenActivity::class.java, context, bundle, 2)
        }

        if (mStage?.status.equals(context.getString(R.string.locked), ignoreCase = true)) {
            holder.add_criteria_action.visibility = View.INVISIBLE
            holder.add_criteria_comments.visibility = View.INVISIBLE
        }

    }//End Holder Class


    override fun getItemCount(): Int {
        return mCriterias?.size ?: 0

    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    private fun RefreshList() {
        notifyDataSetChanged()
    }

    private fun ShowMenu(v: View, criteria: DynamicStagesCriteriaListDAO) {
        val popup = PopupMenu(context, v)
        popup.inflate(R.menu.menu_status);
        val menuOpts = popup.menu
        val findItem_accept = menuOpts.findItem(R.id.action_accept)
        val findItem_reject = menuOpts.findItem(R.id.action_reject)

        if (!criteria.approveText.isNullOrEmpty())
            findItem_accept.title = criteria.approveText

        if (!criteria.rejectText.isNullOrEmpty())
            findItem_reject.title = criteria.rejectText


        popup.setOnMenuItemClickListener { item ->
            val id = item.itemId
            if (id == R.id.action_accept) {
                if (mstatus != null) {
                    mstatus!!.StatusChange(criteria, true)
                }
            } else if (id == R.id.action_reject) {
                mstatus!!.StatusChange(criteria, false)
            }
            false
        }
        // popup.inflate(R.menu.menu_status)
        popup.show()

    }

    companion object {

        private val LOG_TAG = "ListStagesCriteriaDetailAdapter"


    }
}
