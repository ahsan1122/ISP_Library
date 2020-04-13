package com.esp.library.exceedersesp.controllers.lookupinfo.adapter

import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.esp.library.R
import com.esp.library.utilities.common.SharedPreference

import com.esp.library.exceedersesp.BaseActivity
import com.esp.library.exceedersesp.controllers.lookupinfo.LoopUpItemDetailList

import java.util.ArrayList

import utilities.data.lookup.LookupInfoListDAO

class ListLookupInfoDAOAdapter(lookupInfoList: List<LookupInfoListDAO>, context: BaseActivity) : androidx.recyclerview.widget.RecyclerView.Adapter<ListLookupInfoDAOAdapter.ViewHolder>() {

    private val TAG = "ListLookupInfoDAOAdapter"
    internal var pref: SharedPreference
    internal var lookupInfoList: List<LookupInfoListDAO> = ArrayList()
    private val context: BaseActivity
    init {
        this.context = context
        this.lookupInfoList = lookupInfoList
        pref = SharedPreference(context)
    }

    inner class ViewHolder(v: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v) {

        internal var txtname: TextView
        internal var ivlogo: ImageView
        internal var lllayout: LinearLayout

        init {
            txtname = itemView.findViewById(R.id.txtname)
            ivlogo = itemView.findViewById(R.id.ivlogo)
            lllayout = itemView.findViewById(R.id.lllayout)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View
        v = LayoutInflater.from(parent.context).inflate(R.layout.lookup_info_list_item, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val lookupInfoListDAO = lookupInfoList[position]

        holder.txtname.text = lookupInfoListDAO.name
        holder.lllayout.setOnClickListener {
            val i = Intent(context, LoopUpItemDetailList::class.java)
            i.putExtra("lookupid", lookupInfoListDAO.id)
            i.putExtra("toolbar_heading", lookupInfoListDAO.name)
            context.startActivity(i)
        }


    }//End Holder Class


    override fun getItemCount(): Int {
        return lookupInfoList.size
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }




}
