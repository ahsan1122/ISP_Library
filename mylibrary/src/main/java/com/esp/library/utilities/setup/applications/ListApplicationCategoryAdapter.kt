package utilities.adapters.setup.applications

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.esp.library.R
import com.esp.library.exceedersesp.BaseActivity
import com.esp.library.exceedersesp.controllers.applications.filters.FilterActivity
import utilities.data.applicants.addapplication.CategoryAndDefinationsDAO
import utilities.interfaces.CheckFilterSelection

class ListApplicationCategoryAdapter(private val mApplications: List<CategoryAndDefinationsDAO>?, con: BaseActivity) : androidx.recyclerview.widget.RecyclerView.Adapter<ListApplicationCategoryAdapter.ParentViewHolder>() {


    private var context: BaseActivity
    var filterSelectionListener: CheckFilterSelection? = null
    var previousPosition: Int = 0
    var categoryAndDefinationsDAOFilteredList = ArrayList<CategoryAndDefinationsDAO>()

    open class ParentViewHolder(v: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v)

    inner class ActivitiesList(v: View) : ParentViewHolder(v) {

        internal var ivChecked: ImageView
        internal var tvMultiSelectionFilter: TextView
        internal var llFilterCont: LinearLayout

        init {
            ivChecked = itemView.findViewById(R.id.ivChecked)
            tvMultiSelectionFilter = itemView.findViewById(R.id.tvMultiSelectionFilter)
            llFilterCont = itemView.findViewById(R.id.llFilterCont)


        }

    }


    init {
        context = con
        filterSelectionListener = context as CheckFilterSelection

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentViewHolder {
        val v: View = LayoutInflater.from(parent.context).inflate(R.layout.row_filter_multi_selection, parent, false)
        return ActivitiesList(v)
    }


    override fun onBindViewHolder(holder_parent: ParentViewHolder, position: Int) {

        val holder = holder_parent as ActivitiesList
        val categoryAndDefinationsDAO = mApplications!![position]

        holder.tvMultiSelectionFilter.text = categoryAndDefinationsDAO.name

        defualtButton(holder,categoryAndDefinationsDAO.isChecked,categoryAndDefinationsDAO)

        holder.llFilterCont.setOnClickListener {

            if (categoryAndDefinationsDAO.isChecked) {
                categoryAndDefinationsDAO.isChecked = false
                defualtButton(holder, false,categoryAndDefinationsDAO)

            } else {

                categoryAndDefinationsDAO.isChecked = true
                defualtButton(holder, true,categoryAndDefinationsDAO)
            }

            filterSelectionListener?.checkFilterSelection(mApplications)
        }


    }//End Holder Class


    private fun defualtButton(holder: ActivitiesList, checked: Boolean,
                              categoryAndDefinationsDAO: CategoryAndDefinationsDAO) {

        if(checked)
        {
            categoryAndDefinationsDAOFilteredList.add(categoryAndDefinationsDAO)
            holder.ivChecked.visibility = View.VISIBLE
            holder.llFilterCont.background = ContextCompat.getDrawable(context, R.drawable.draw_bg_green)
            holder.tvMultiSelectionFilter.setTextColor(ContextCompat.getColor(context, R.color.white))
        }
        else {
            categoryAndDefinationsDAOFilteredList.remove(categoryAndDefinationsDAO)
            holder.ivChecked.visibility = View.GONE
            holder.llFilterCont.background = ContextCompat.getDrawable(context, R.drawable.draw_bg_green_stroke)
            holder.tvMultiSelectionFilter.setTextColor(ContextCompat.getColor(context, R.color.green))
        }

       // AddApplicationCategoryAndDefinationsFragment.categoryAndDefinationsDAOFilteredList = categoryAndDefinationsDAOFilteredList
        FilterActivity.tempFilterSelectionValues = categoryAndDefinationsDAOFilteredList
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


    companion object {

        private val LOG_TAG = "ListApplicationCategoryAdapter"


    }


}
