/*
package utilities.adapters.setup.applications

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager

import com.esp.library.exceedersesp.BaseActivity
import com.esp.library.exceedersesp.R
import com.esp.library.exceedersesp.fragments.applications.UsersApplicationsFragment
import com.esp.library.utilities.customcontrols.CustomButton

import utilities.data.applicants.dynamics.DynamicStagesDAO
import utilities.interfaces.LinkDefinitionItemClick


class LinkDefinitionStageAdapter(val dynamicStagesDAOList: ArrayList<DynamicStagesDAO>, con: BaseActivity)
    : RecyclerView.Adapter<LinkDefinitionStageAdapter.ViewHolder>() {


    private var submit_request: UsersApplicationsFragment? = null
    internal var imm: InputMethodManager? = null
    private var context: BaseActivity = con
    var LinkDefinitionItemClickListener: LinkDefinitionItemClick?=null

    init {
        LinkDefinitionItemClickListener = context as LinkDefinitionItemClick
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal var btlinkdefname: CustomButton = itemView.findViewById(R.id.btlinkdefname)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.application_status_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dynamicStagesDAO = dynamicStagesDAOList[position]
        holder.btlinkdefname.text = dynamicStagesDAO.linkDefinitionValue
        holder.btlinkdefname.setOnClickListener(View.OnClickListener {

            if(LinkDefinitionItemClickListener!=null)
                LinkDefinitionItemClickListener?.linkDefinitionItem(dynamicStagesDAO)

        })


    }//End Holder Class

    override fun getItemCount(): Int {
        return dynamicStagesDAOList.size
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }


    companion object {
        private val LOG_TAG = "LinkDefinitionStageAdapter"

    }

}
*/
