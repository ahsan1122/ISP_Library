package utilities.adapters.setup

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import com.esp.library.R
import com.esp.library.exceedersesp.fragments.applications.AddApplicationCategoryAndDefinationsFragment
import com.esp.library.exceedersesp.controllers.applications.ApplicationsActivityDrawer
import utilities.data.applicants.addapplication.CategoryAndDefinationsDAO
import utilities.interfaces.DeleteFilterListener


class FilterItemsAdapter(internal var filtersList: MutableList<CategoryAndDefinationsDAO>, bContext: Context) : androidx.recyclerview.widget.RecyclerView.Adapter<FilterItemsAdapter.ParentViewHolder>() {

    private val TAG = javaClass.simpleName
    private val context: Context
    var deleteFilterListener:DeleteFilterListener?=null

    init {
        this.context = bContext

    }


    open class ParentViewHolder(v: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v)

    inner class ActivitiesList @SuppressLint("RestrictedApi")
    constructor(v: View) : ParentViewHolder(v) {

        internal var rlfilterbutton: RelativeLayout
        internal var txtcatName: TextView

        init {
            rlfilterbutton = itemView.findViewById(R.id.rlfilterbutton)
            txtcatName = itemView.findViewById(R.id.txtcatName)

        }

    }

    fun setActivitContext(addApplicationCategoryAndDefinationsFragment: AddApplicationCategoryAndDefinationsFragment)
    {
        //addApplicationCategoryAndDefinationsFragment= addApplicationCategoryAndDefinationsFragment
        deleteFilterListener= addApplicationCategoryAndDefinationsFragment
    }

    fun setActivitContext(applicationsActivityDrawer: ApplicationsActivityDrawer)
    {
        deleteFilterListener= applicationsActivityDrawer
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.filter_item_row, parent, false)
        return ActivitiesList(v)
    }

    override fun onBindViewHolder(holder_parent: ParentViewHolder, position: Int) {

        val holder = holder_parent as ActivitiesList
        val filtersList = filtersList[position]
        holder.txtcatName.text = filtersList.name

        holder.rlfilterbutton.setOnClickListener {
            deleteFilterListener?.deleteFilters(filtersList)
        }


    }//End Holder Class


    override fun getItemCount(): Int {
        return filtersList.size

    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }


}