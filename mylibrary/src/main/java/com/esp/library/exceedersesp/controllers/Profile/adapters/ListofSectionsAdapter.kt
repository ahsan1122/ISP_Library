package com.esp.library.exceedersesp.controllers.Profile.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import com.esp.library.R
import com.esp.library.exceedersesp.controllers.Profile.EditSectionDetails
import com.esp.library.exceedersesp.controllers.Profile.adapters.ListofSectionsFieldsAdapter
import com.esp.library.utilities.common.Shared
import com.esp.library.utilities.common.SharedPreference
import utilities.data.applicants.dynamics.DynamicFormSectionDAO
import utilities.data.applicants.dynamics.DynamicFormSectionFieldDAO
import utilities.data.applicants.dynamics.DynamicFormSectionFieldsCardsDAO
import utilities.data.applicants.profile.ApplicationProfileDAO


class ListofSectionsAdapter(internal var dynamicFormSectionDAO: DynamicFormSectionDAO,
                            internal var dataapplicant: ApplicationProfileDAO, internal var sectionsFields: List<DynamicFormSectionFieldDAO>,
                            private val context: Context, internal var ischeckerror: Boolean) : androidx.recyclerview.widget.RecyclerView.Adapter<ListofSectionsAdapter.ParentViewHolder>() {

    private val TAG = javaClass.simpleName
    internal var isViewOnly = false
    internal var pref: SharedPreference
    internal var fieldsCardsList: List<DynamicFormSectionFieldsCardsDAO>


    init {
        fieldsCardsList = dynamicFormSectionDAO.fieldsCardsList!!
        pref = SharedPreference(context)
    }


    open class ParentViewHolder(v: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v)

    inner class ActivitiesList @SuppressLint("RestrictedApi")
    constructor(v: View) : ParentViewHolder(v) {

        internal var ibEditCard: ImageButton
        internal var rvFields: androidx.recyclerview.widget.RecyclerView
        internal var dividerview: View
        internal var llbasicinfo: LinearLayout
        internal var tNameLabel: TextView
        internal var tNameValue: TextView
        internal var tEmailLabel: TextView
        internal var tEmailValue: TextView
        internal var tProfileTypeLabel: TextView
        internal var tProfileTypeValue: TextView

        init {
            rvFields = itemView.findViewById(R.id.rvFields)
            ibEditCard = itemView.findViewById(R.id.ibRemoveCard)
            dividerview = itemView.findViewById(R.id.dividerview)
            llbasicinfo = itemView.findViewById(R.id.llbasicinfo)
            tNameLabel = itemView.findViewById(R.id.tNameLabel)
            tNameValue = itemView.findViewById(R.id.tNameValue)
            tEmailLabel = itemView.findViewById(R.id.tEmailLabel)
            tEmailValue = itemView.findViewById(R.id.tEmailValue)
            tProfileTypeLabel = itemView.findViewById(R.id.tProfileTypeLabel)
            tProfileTypeValue = itemView.findViewById(R.id.tProfileTypeValue)

            if (dynamicFormSectionDAO.isDefault) {
                llbasicinfo.visibility = View.VISIBLE
                val name = dataapplicant.applicant.name
                val emailAddress = dataapplicant.applicant.emailAddress
                var profileTemplateString = dataapplicant.applicant.profileTemplateString

                if (TextUtils.isEmpty(profileTemplateString))
                {
                    profileTemplateString = context.getString(R.string.base)
                    dataapplicant.applicant.profileTemplateString=profileTemplateString
                }

                tNameLabel.text = context.getString(R.string.name)
                tNameValue.text = name
                tEmailLabel.text = context.getString(R.string.email)
                tEmailValue.text = emailAddress
                tProfileTypeLabel.text = context.getString(R.string.profiletype)
                tProfileTypeValue.text = profileTemplateString
            } else
                llbasicinfo.visibility = View.GONE


        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentViewHolder {
        val v: View
        if (isViewOnly)
            v = LayoutInflater.from(parent.context).inflate(R.layout.repeater_add_application_section_fields_view, parent, false)
        else
            v = LayoutInflater.from(parent.context).inflate(R.layout.list_of_sections_row, parent, false)
        return ActivitiesList(v)
    }

    override fun onBindViewHolder(holder_parent: ParentViewHolder, position: Int) {

        val holder = holder_parent as ActivitiesList

        if (fieldsCardsList.size > 1)
            holder.dividerview.visibility = View.VISIBLE

        //recycler view for fields
        holder.rvFields.setHasFixedSize(true)
        holder.rvFields.isNestedScrollingEnabled = false

        val linearLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(context, androidx.recyclerview.widget.LinearLayoutManager.VERTICAL, false)
        holder.rvFields.layoutManager = linearLayoutManager

        // val adapter = ListofSectionsFieldsAdapter(fieldsCardsList[position].fields, context, ischeckerror)
        val adapter = ListofSectionsFieldsAdapter(fieldsCardsList[position].fields, context, ischeckerror, true)
        holder.rvFields.adapter = adapter

        if (dynamicFormSectionDAO.type == 1)  // 1 = editable
            holder.ibEditCard.visibility = View.VISIBLE
        else if (dynamicFormSectionDAO.type == 2) // 2 = viewonly
            holder.ibEditCard.visibility = View.GONE


        holder.ibEditCard.setOnClickListener { v ->
            val dynamicFormSectionFieldsCardsDAO = fieldsCardsList[position].fields
            dynamicFormSectionDAO.fields = dynamicFormSectionFieldsCardsDAO
            removeCard(v, position)
        }


    }//End Holder Class


    override fun getItemCount(): Int {
        return fieldsCardsList.size

    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }


    private fun removeCard(v: View, position: Int) {
        //creating a popup menu
        val popup = PopupMenu(context, v)
        popup.gravity = Gravity.CENTER

        try {
            // Reflection apis to enforce show icon
            val fields = popup.javaClass.declaredFields
            for (field in fields) {
                if (field.name == "mPopup") {
                    field.isAccessible = true
                    val menuPopupHelper = field.get(popup)
                    val classPopupHelper = Class.forName(menuPopupHelper.javaClass.name)
                    val setForceIcons = classPopupHelper.getMethod("setForceShowIcon", Boolean::class.javaPrimitiveType!!)
                    setForceIcons.invoke(menuPopupHelper, true)
                    break
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        //inflating menu from xml resource
        popup.inflate(R.menu.menu_edit)

        //  val item_edit = popup.menu.findItem(R.id.action_edit)
        val item_delete = popup.menu.findItem(R.id.action_delete)
        item_delete.isVisible = false
        /*if (!dynamicFormSectionDAO.isMultipule)
            item_delete.isVisible = false*/

        //adding click listener
        popup.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_edit) {

                val i = Intent(context, EditSectionDetails::class.java)
                i.putExtra("data", dynamicFormSectionDAO)
                i.putExtra("dataapplicant", dataapplicant)
                i.putExtra("ischeckerror", ischeckerror)
                i.putExtra("position", position)
                context.startActivity(i)
            } else if (item.itemId == R.id.action_delete) {
                Shared.getInstance().messageBox("Delete", context as Activity)
            }
            false
        }
        //displaying the popup
        popup.show()
    }

}