package utilities.adapters.setup.applications

import android.graphics.Color
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import com.esp.library.R
import com.esp.library.exceedersesp.controllers.applications.ApplicationDetailScreenActivity
import com.esp.library.utilities.common.SharedPreference
import com.esp.library.utilities.setup.applications.ApplicationFieldsRecyclerAdapter
import com.esp.library.exceedersesp.BaseActivity
import com.esp.library.exceedersesp.controllers.applications.ActivityStageDetails
import com.esp.library.utilities.common.ViewAnimationUtils
import com.google.gson.Gson
import utilities.data.applicants.dynamics.*
import utilities.interfaces.FeedbackSubmissionClick


class ListAddApplicationSectionsAdapter(mApplication: ArrayList<DynamicFormSectionDAO>?, con: BaseActivity, internal var searched_text: String,
                                        internal var isViewOnly: Boolean) : androidx.recyclerview.widget.RecyclerView.Adapter<ListAddApplicationSectionsAdapter.ParentViewHolder>() {
    private var actualResponseJson: String? = null
    internal var pref: SharedPreference
    private var context: BaseActivity
    var listener: FeedbackSubmissionClick? = null
    var isEnable: Boolean? = false
    var criteriaListDAO: DynamicStagesCriteriaListDAO? = null
    var mApplicationFieldsCardsAdapter: ListAddApplicationSectionFieldsCardsAdapter? = null;
    var dynamicStagesCriteriaListDAO: DynamicStagesCriteriaListDAO? = null
    private var mApplications: ArrayList<DynamicFormSectionDAO>? = null

    private var mApplicationFieldsAdapterListener: ApplicationFieldsRecyclerAdapter.ApplicationFieldsAdapterListener? = null
    private var mApplicationFieldsDetailAdapterListener: ApplicationFieldsRecyclerAdapter.ApplicationDetailFieldsAdapterListener? = null

    fun setmApplicationFieldsAdapterListener(mApplicationFieldsAdapterListener: ApplicationFieldsRecyclerAdapter.ApplicationFieldsAdapterListener) {
        this.mApplicationFieldsAdapterListener = mApplicationFieldsAdapterListener
    }

    fun setmApplicationFieldsAdapterListener2(mApplicationFieldsDetailAdapterListener: ApplicationDetailScreenActivity) {
        this.mApplicationFieldsDetailAdapterListener = mApplicationFieldsDetailAdapterListener
    }

    fun setmApplicationFieldsAdapterListener2(mApplicationFieldsDetailAdapterListener: ActivityStageDetails) {
        this.mApplicationFieldsDetailAdapterListener = mApplicationFieldsDetailAdapterListener
    }


    fun getCriteriaObject(criterialistDAO: DynamicStagesCriteriaListDAO?) {
        criteriaListDAO = criterialistDAO;
    }


    open class ParentViewHolder(v: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v)

    inner class ActivitiesList(v: View) : ParentViewHolder(v) {

        internal var tvSectionHeader: TextView
        internal var txtaddsectionbutton: TextView
        internal var tvSectionHeaderCount: TextView
        internal var tvSectionLabelsName: TextView
        internal var vsperatorTop: View
        internal var vbottomSeperator: View
        internal var rlsection: RelativeLayout
        internal var ibshowoptions: ImageButton
        internal var ivarrow: ImageButton
        internal var rvFieldsCards: androidx.recyclerview.widget.RecyclerView
        internal var rladdnewsection: RelativeLayout
        internal var parentlayout: LinearLayout
        internal var viewsectionbottom: View

        init {
            ibshowoptions = itemView.findViewById(R.id.ibshowoptions)
            ivarrow = itemView.findViewById(R.id.ivarrow)
            viewsectionbottom = itemView.findViewById(R.id.viewsectionbottom)
            tvSectionHeaderCount = itemView.findViewById(R.id.tvSectionHeaderCount)
            tvSectionLabelsName = itemView.findViewById(R.id.tvSectionLabelsName)
            tvSectionHeader = itemView.findViewById(R.id.tvSectionHeader)
            rvFieldsCards = itemView.findViewById(R.id.rvFieldsCards)
            rladdnewsection = itemView.findViewById(R.id.rladdnewsection)
            txtaddsectionbutton = itemView.findViewById(R.id.txtaddsectionbutton)
            rlsection = itemView.findViewById(R.id.rlsection)
            parentlayout = itemView.findViewById(R.id.parentlayout)
            vsperatorTop = itemView.findViewById(R.id.vsperatorTop)
            vbottomSeperator = itemView.findViewById(R.id.vbottomSeperator)

        }

    }

    init {
        context = con
        mApplications = mApplication

        pref = SharedPreference(context)
        try {
            listener = context as FeedbackSubmissionClick
        } catch (e: Exception) {

        }
    }

    fun getmApplications(): List<DynamicFormSectionDAO>? {
        return mApplications
    }


    fun setActualResponseJson(actualResponseJson: String) {
        this.actualResponseJson = actualResponseJson
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentViewHolder {
        val v: View = LayoutInflater.from(parent.context).inflate(R.layout.repeater_add_application_section, parent, false)
        return ActivitiesList(v)
    }
/*
    private fun setTextStyle(holder: ActivitiesList) {
        holder.rvFieldsCards.setPadding(0, 20, 0, 0)
        holder.tvSectionHeader.setPadding(40, 24, 0, 24)
        holder.tvSectionHeader.setTextColor(Color.BLACK)
        holder.tvSectionHeader.textSize = 18f
        val typeface = Typeface.createFromAsset(context.assets, "font/lato/lato_medium.ttf")
        holder.tvSectionHeader.setTypeface(typeface);
        holder.viewsectionbottom.visibility = View.VISIBLE

    }

    private fun setSubmissionTextStyle(holder: ActivitiesList) {
        val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(20, 0, 10, 10)
        holder.rvFieldsCards.setLayoutParams(params)
        // holder.rvFieldsCards.setPadding(0, 0, 0, 0)
        holder.tvSectionHeader.setPadding(50, 24, 0, 13)
        holder.tvSectionHeader.setTextColor(Color.BLACK)
        holder.tvSectionHeader.textSize = 15f
        val typeface = Typeface.createFromAsset(context.assets, "font/lato/lato_medium.ttf")
        holder.tvSectionHeader.setTypeface(typeface);

    }*/


    override fun onBindViewHolder(holder_parent: ParentViewHolder, position: Int) {

        val dynamicFormSectionDAO = mApplications?.get(position)

        val sectionType = dynamicFormSectionDAO?.type
        val holder = holder_parent as ActivitiesList

        if (position > 0)
            holder.vsperatorTop.visibility = View.GONE

        dynamicStagesCriteriaListDAO = dynamicFormSectionDAO?.dynamicStagesCriteriaListDAO

        if (dynamicStagesCriteriaListDAO?.assessmentStatus != null) {
            if (pref.language.equals("ar", ignoreCase = true))
                holder.tvSectionHeader.gravity = Gravity.END
            else
                holder.tvSectionHeader.gravity = Gravity.START

            isViewOnly = !dynamicStagesCriteriaListDAO?.assessmentStatus.equals(context.getString(R.string.active), ignoreCase = true)
            //holder.tvSectionHeader.text = dynamicFormSectionDAO.defaultName
            holder.tvSectionHeader.text = dynamicStagesCriteriaListDAO?.name
            holder.tvSectionHeaderCount.text = (position + 1).toString()

            holder.tvSectionHeader.visibility = View.GONE

        } else {
            holder.tvSectionHeader.visibility = View.GONE

            if (dynamicFormSectionDAO?.defaultName != null && !dynamicFormSectionDAO.defaultName!!.isEmpty()) {
                //   setSubmissionTextStyle(holder)
                holder.tvSectionHeader.visibility = View.VISIBLE
                if (isViewOnly) {
                    //  holder.tvSectionHeader.setPadding(36, 0, 0, 0)
                    if (ListUsersApplicationsAdapter.isSubApplications) {
                        //setSubmissionTextStyle(holder)
                        if (position > 0)
                            holder.viewsectionbottom.visibility = View.VISIBLE
                    } else {
                        holder.viewsectionbottom.visibility = View.GONE
                        //  holder.tvSectionHeader.setTextColor(ContextCompat.getColor(context, R.color.cooltwogrey))
                        // holder.tvSectionHeader.setPadding(40, 0, 0, 0)
                    }
                    holder.tvSectionHeader.text = dynamicFormSectionDAO.defaultName
                    holder.tvSectionHeaderCount.text = (position + 1).toString()

                } else {
                    holder.tvSectionHeader.text = dynamicFormSectionDAO.defaultName
                    holder.tvSectionHeaderCount.text = (position + 1).toString()
                }

            } else {
                if (position > 0)
                    holder.viewsectionbottom.visibility = View.VISIBLE
            }
        }
        holder.rladdnewsection.visibility = View.GONE

        if (criteriaListDAO != null) {
            holder.tvSectionHeaderCount.visibility = View.GONE
            holder.vbottomSeperator.visibility = View.GONE
            holder.vsperatorTop.visibility = View.GONE
            holder.ivarrow.visibility = View.GONE
        }

        if (dynamicFormSectionDAO!!.isMultipule && holder.tvSectionLabelsName.visibility == View.GONE) {
            if (dynamicFormSectionDAO.fieldsCardsList!!.isNotEmpty() &&
                    dynamicFormSectionDAO.fieldsCardsList?.get(0)?.fields!!.isNotEmpty()) {
                holder.rladdnewsection.visibility = View.VISIBLE
            } else {
                holder.tvSectionHeader.visibility = View.GONE
            }
        }
        if (isViewOnly) {
            holder.rladdnewsection.visibility = View.GONE
            holder.parentlayout.setBackgroundColor(Color.WHITE)
        }

        holder.txtaddsectionbutton.text = context.getString(R.string.add) + " " + holder.tvSectionHeader.text


        //recycler view for items
        holder.rvFieldsCards.setHasFixedSize(true)
        holder.rvFieldsCards.isNestedScrollingEnabled = false
        val linearLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(context, androidx.recyclerview.widget.LinearLayoutManager.VERTICAL, false)
        holder.rvFieldsCards.layoutManager = linearLayoutManager



        holder.ivarrow.setOnClickListener {
            if (holder.rvFieldsCards.visibility == View.VISIBLE) {


                ViewAnimationUtils.collapse(holder.rvFieldsCards,holder.tvSectionLabelsName)

                val handler = Handler()
                handler.postDelayed({
                    holder.rvFieldsCards.visibility = View.GONE

                    holder.tvSectionLabelsName.visibility = View.VISIBLE
                    holder.rlsection.setBackgroundColor(ContextCompat.getColor(context, R.color.pale_grey))
                    //  holder.tvSectionHeaderCount.setTextColor(ContextCompat.getColor(context, R.color.grey))
                    holder.ivarrow.setImageResource(R.drawable.ic_arrow_down)
                    val concateLabel = StringBuilder()
                    for (i in dynamicFormSectionDAO.fieldsCardsList!!.indices) {

                        for (j in dynamicFormSectionDAO.fieldsCardsList!![i].fields!!.indices) {
                            val label = dynamicFormSectionDAO.fieldsCardsList!![i].fields?.get(j)?.label
                            concateLabel.append(label)
                            concateLabel.append(", ")
                        }
                    }

                    var fieldLabels: String
                    fieldLabels = concateLabel.toString().trim()
                    if (fieldLabels.endsWith(",")) {
                        fieldLabels = fieldLabels.substring(0, fieldLabels.length - 1);
                    }
                    holder.tvSectionLabelsName.text = fieldLabels

                    if (dynamicFormSectionDAO.isMultipule)
                        holder.rladdnewsection.visibility = View.GONE
                }, 300)


            } else {
                if (dynamicFormSectionDAO.isMultipule && !isViewOnly)
                    holder.rladdnewsection.visibility = View.VISIBLE
                holder.rvFieldsCards.visibility = View.VISIBLE
                holder.tvSectionLabelsName.visibility = View.GONE
                holder.rlsection.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
                //   holder.tvSectionHeaderCount.setTextColor(ContextCompat.getColor(context, R.color.green))
                holder.ivarrow.setImageResource(R.drawable.ic_arrow_up)
                ViewAnimationUtils.expand(holder.rvFieldsCards)


            }
        }

        //    var getAssessmentStatus = dynamicStagesCriteriaListDAO?.assessmentStatus


        val refinementOfFieldCardList = refinementOfFieldCardList(dynamicFormSectionDAO.fieldsCardsList)
        dynamicFormSectionDAO.fieldsCardsList = refinementOfFieldCardList

        mApplicationFieldsCardsAdapter = actualResponseJson?.let {
            ListAddApplicationSectionFieldsCardsAdapter(
                    dynamicFormSectionDAO.fieldsCardsList as MutableList<DynamicFormSectionFieldsCardsDAO>,
                    context, "", it, isViewOnly, dynamicStagesCriteriaListDAO, sectionType!!)
        }

        if (criteriaListDAO != null)
            mApplicationFieldsCardsAdapter?.getCriteriaObject(criteriaListDAO)

        mApplicationFieldsCardsAdapter?.setmApplications(mApplications!!)
        mApplicationFieldsCardsAdapter?.setSectionHeader(holder.tvSectionHeader.text.toString())
        mApplicationFieldsCardsAdapter?.isMultipleSection(dynamicFormSectionDAO.isMultipule)



        holder.rvFieldsCards.adapter = mApplicationFieldsCardsAdapter


        // setAdapterData(position,dynamicStagesCriteriaListDAO,sectionType,holder)

        mApplicationFieldsCardsAdapter?.setmApplicationFieldsAdapterListener(object : ApplicationFieldsRecyclerAdapter.ApplicationFieldsAdapterListener {

            override fun onFieldValuesChanged() {

                if (mApplicationFieldsAdapterListener != null) {
                    mApplicationFieldsAdapterListener!!.onFieldValuesChanged()
                } else if (mApplicationFieldsDetailAdapterListener != null) {
                    mApplicationFieldsDetailAdapterListener!!.onFieldValuesChanged()
                }
            }

            override fun onAttachmentFieldClicked(fieldDAO: DynamicFormSectionFieldDAO, position: Int) {

                if (mApplicationFieldsAdapterListener != null) {
                    mApplicationFieldsAdapterListener!!.onAttachmentFieldClicked(fieldDAO, position)
                } else if (mApplicationFieldsDetailAdapterListener != null) {
                    mApplicationFieldsDetailAdapterListener!!.onAttachmentFieldClicked(fieldDAO, position)
                }

            }

            override fun onLookupFieldClicked(fieldDAO: DynamicFormSectionFieldDAO, position: Int, isCalculatedMappedField: Boolean) {

                if (mApplicationFieldsAdapterListener != null) {
                    mApplicationFieldsAdapterListener!!.onLookupFieldClicked(fieldDAO, position, isCalculatedMappedField)
                } else if (mApplicationFieldsDetailAdapterListener != null) {
                    mApplicationFieldsDetailAdapterListener!!.onLookupFieldClicked(fieldDAO, position, isCalculatedMappedField)
                }
            }
        })


        holder.rladdnewsection.setOnClickListener {
            if (actualResponseJson != null && !actualResponseJson!!.isEmpty()) {
                val actualResponse = Gson().fromJson(actualResponseJson, DynamicResponseDAO::class.java)

                if (actualResponse != null) {
                    val sections = actualResponse.form?.sections
                    for (i in sections!!.indices) {
                        if (sections[i].id == dynamicFormSectionDAO.id) {
                            val fieldsList = sections[i].fields
                            val fieldsListTempArray = ArrayList<DynamicFormSectionFieldDAO>()
                            for (j in 0 until fieldsList!!.size) {
                                val fields = fieldsList.get(j)

                                if (fields.type == 18 || fields.type == 19)
                                    fields.value = ""

                                if (fields.isVisible) {
                                    fieldsListTempArray.add(fields)
                                }
                            }
                            val clonedFieldsCard = DynamicFormSectionFieldsCardsDAO(fieldsListTempArray)
                            mApplicationFieldsCardsAdapter?.addCard(clonedFieldsCard, position)
                        }
                    }
                    // mApplicationFieldsCardsAdapter?.notifyDataSetChanged()
                    // setAdapterData(position,dynamicStagesCriteriaListDAO,sectionType,holder)
                    notifyItemChanged(position)
                }


            }
        }

        holder.ibshowoptions.setOnClickListener { view ->
            ShowMenu(view, dynamicStagesCriteriaListDAO)
        }
    }//End Holder Class

    /* private fun setAdapterData(position: Int, dynamicStagesCriteriaListDAO: DynamicStagesCriteriaListDAO?, sectionType: Int, holder: ActivitiesList) {
         val refinementOfFieldCardList = refinementOfFieldCardList(mApplications?.get(position)?.fieldsCardsList)
         mApplications?.get(position)?.fieldsCardsList = refinementOfFieldCardList

         mApplicationFieldsCardsAdapter = actualResponseJson?.let {
             ListAddApplicationSectionFieldsCardsAdapter(
                     mApplications?.get(position)?.fieldsCardsList as MutableList<DynamicFormSectionFieldsCardsDAO>,
                     context, "", it, isViewOnly, dynamicStagesCriteriaListDAO, sectionType)
         }

         if (criteriaListDAO != null)
             mApplicationFieldsCardsAdapter?.getCriteriaObject(criteriaListDAO)

         if (mApplications != null) {
             mApplicationFieldsCardsAdapter?.setmApplications(mApplications)
         }



         holder.rvFieldsCards.adapter = mApplicationFieldsCardsAdapter


     }*/

    private fun refinementOfFieldCardList(fieldsCardsList: List<DynamicFormSectionFieldsCardsDAO>?): ArrayList<DynamicFormSectionFieldsCardsDAO> {
        val tempFieldsCardsList = ArrayList<DynamicFormSectionFieldsCardsDAO>()
        for (i in 0 until fieldsCardsList!!.size) {
            val getFieldsCard = fieldsCardsList.get(i)
            if (getFieldsCard.fields!!.size > 0) {
                tempFieldsCardsList.add(getFieldsCard)
            }
        }

        return tempFieldsCardsList;
    }

    private fun ShowMenu(v: View, criteria: DynamicStagesCriteriaListDAO?) {
        val popup = PopupMenu(context, v)
        popup.inflate(R.menu.menu_status);
        val menuOpts = popup.menu
        val findItem_accept = menuOpts.findItem(R.id.action_accept)
        val findItem_reject = menuOpts.findItem(R.id.action_reject)

        val POPUP_CONSTANT = "mPopup"
        val POPUP_FORCE_SHOW_ICON = "setForceShowIcon"

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


        findItem_reject.isVisible = !criteria?.type.equals(context.getString(R.string.feedback), ignoreCase = true)


        when (isEnable) {
            true -> findItem_accept?.isEnabled = true
            false -> findItem_accept?.isEnabled = false
        }
        if (!criteria?.approveText.isNullOrEmpty())
            findItem_accept.title = criteria?.approveText

        if (!criteria?.rejectText.isNullOrEmpty())
            findItem_reject.title = criteria?.rejectText


        popup.setOnMenuItemClickListener { item ->
            val id = item.itemId
            if (id == R.id.action_accept) {
                // listener?.feedbackClick(true, criteriaListDAO, dynamicStagesCriteriaListDAO)
            } else if (id == R.id.action_reject) {
                //  listener?.feedbackClick(false, criteriaListDAO, dynamicStagesCriteriaListDAO)
            }
            false
        }
        // popup.inflate(R.menu.menu_status)
        popup.show()

    }

    override fun getItemCount(): Int {
        return mApplications?.size ?: 0

    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun RefreshList() {
        notifyDataSetChanged()
    }

    fun GetAllFields(): List<DynamicFormSectionFieldDAO>? {
        if (mApplications != null && mApplications!!.size > 0) {

            val fields = ArrayList<DynamicFormSectionFieldDAO>()

            for (sectionDAO in mApplications!!) {

                if (sectionDAO.fieldsCardsList!!.size > 0) {

                    for (sectionFieldsCardsDAO in sectionDAO.fieldsCardsList!!) {
                        fields.addAll(sectionFieldsCardsDAO.fields!!)
                    }

                }

            }

            return fields

        }

        return null
    }

    companion object {

        private val LOG_TAG = "ListAddApplicationSectionsAdapter"


    }

}
