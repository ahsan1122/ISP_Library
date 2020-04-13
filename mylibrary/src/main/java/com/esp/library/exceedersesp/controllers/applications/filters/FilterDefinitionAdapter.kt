package com.esp.library.exceedersesp.controllers.applications.filters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatCheckBox
import com.esp.library.R
import com.esp.library.utilities.common.SharedPreference
import utilities.data.filters.FilterDefinitionSortDAO
import utilities.interfaces.ApplicationsFilterListener


class FilterDefinitionAdapter(val filterDefinitionSortList: List<FilterDefinitionSortDAO>, con: Context)
    : androidx.recyclerview.widget.RecyclerView.Adapter<FilterDefinitionAdapter.ParentViewHolder>() {


    var TAG = "FilterDefinitionAdapter"
    private var context: Context
    var pref: SharedPreference? = null
    var filterListener: ApplicationsFilterListener? = null


    open class ParentViewHolder(v: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v)

    inner class ActivitiesList(v: View) : ParentViewHolder(v) {


        internal var checkBox: AppCompatCheckBox
        internal var txtdefinitionname: TextView
        internal var rlParentLayout: RelativeLayout

        init {

            rlParentLayout = itemView.findViewById(R.id.rlParentLayout)
            checkBox = itemView.findViewById(R.id.checkBox)
            txtdefinitionname = itemView.findViewById(R.id.txtdefinitionname)
        }
    }

    init {
        context = con
        pref = SharedPreference(context)
        filterListener = context as ApplicationsFilterListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.activity_filter_definition_row, parent, false)
        return ActivitiesList(v)

    }


    override fun onBindViewHolder(holder_parent: ParentViewHolder, position: Int) {
        val holder = holder_parent as ActivitiesList
        val filterDefinitionDAO = filterDefinitionSortList.get(position)
        holder.checkBox.isChecked = filterDefinitionDAO.isCheck
        holder.txtdefinitionname.text = filterDefinitionDAO.name


        holder.checkBox.setOnCheckedChangeListener { buttonView, isChecked ->


            filterDefinitionDAO.isCheck = isChecked
            filterListener?.selectedValues(filterDefinitionSortList, position, isChecked)


            if (!isChecked && position > 0 && filterDefinitionSortList.get(0).isCheck) {
                filterDefinitionSortList.get(0).isCheck = false
                notifyItemChanged(0)
            }
        }

        holder.rlParentLayout.setOnClickListener {

            holder.checkBox.performClick()
        }
    }

    override fun getItemCount(): Int {
        return filterDefinitionSortList.size
    }


    private fun checkUncheck(isCheck: Boolean) {
        for (i in filterDefinitionSortList.indices) {
            filterDefinitionSortList.get(i).isCheck = isCheck
        }
        notifyDataSetChanged()
    }

}
