package utilities.adapters.setup.applications

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import com.esp.library.R
import com.esp.library.utilities.common.Shared
import com.esp.library.exceedersesp.BaseActivity
import com.esp.library.exceedersesp.controllers.applications.AddApplicationsFromScreenActivity
import org.json.JSONException
import org.json.JSONObject
import com.esp.library.utilities.customcontrols.BodyTextFont
import utilities.data.applicants.addapplication.CategoryAndDefinationsDAO
import java.io.IOException
import java.nio.charset.Charset


class ListApplicationCategoryAndDefinationAdapter(private val mApplications: List<CategoryAndDefinationsDAO>?,
                                                  con: BaseActivity, internal var list_type: String) :
        androidx.recyclerview.widget.RecyclerView.Adapter<ListApplicationCategoryAndDefinationAdapter.ParentViewHolder>(), Filterable {

    internal var mCat: CategorySelection? = null
    private var context: BaseActivity
    var mApplicationsFiltered: List<CategoryAndDefinationsDAO>? = null


    var search_text: String = "";

    interface CategorySelection {
        fun StatusChange(update: CategoryAndDefinationsDAO)
    }

    open class ParentViewHolder(v: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v)

    inner class ActivitiesList(v: View) : ParentViewHolder(v) {

        internal var btrequest: Button
        internal var cards: androidx.cardview.widget.CardView
        internal var name: TextView
        internal var description: TextView
        internal var txtcategory: TextView
        internal var icons: BodyTextFont

        init {
            cards = itemView.findViewById(R.id.cards)
            btrequest = itemView.findViewById(R.id.btrequest)
            name = itemView.findViewById(R.id.name)
            description = itemView.findViewById(R.id.description)
            txtcategory = itemView.findViewById(R.id.txtcategory)
            icons = itemView.findViewById(R.id.icons)


        }

    }


    init {
        context = con
        mApplicationsFiltered = mApplications
        try {
            mCat = context as CategorySelection
        } catch (e: ClassCastException) {
            throw ClassCastException("lisnter" + " must implement on Activity")
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentViewHolder {
        val v: View
        v = LayoutInflater.from(parent.context).inflate(R.layout.add_def_category_list, parent, false)
        return ActivitiesList(v)
    }


    override fun onBindViewHolder(holder_parent: ParentViewHolder, position: Int) {

        val holder = holder_parent as ActivitiesList
        val getmApplications = mApplicationsFiltered!!.get(position)

        try {
            val obj = JSONObject(loadJSONFromAsset())
            val icon = obj.getString(mApplications!![position].iconName)
            holder.icons.text = icon
        } catch (e: JSONException) {
            e.printStackTrace()
        }



        /*holder.description.setOnClickListener {

            if (holder.description.maxLines == 3)
                holder.description.maxLines = 50
            else
                holder.description.maxLines = 3

        }*/


        if (search_text.length > 0)
            holder.name.text = Shared.getInstance().getSearchedTextHighlight(search_text, getmApplications.name,context)
        else
            holder.name.text = getmApplications.name
        holder.txtcategory.text = getmApplications.category
        holder.description.text = getmApplications.description

        if(TextUtils.isEmpty(getmApplications.category))
            holder.txtcategory.visibility=View.INVISIBLE


        if (list_type.equals(context.getString(R.string.categorysmall), ignoreCase = true)) {
            holder.name.setTextColor(context.resources.getColor(R.color.green))
        } else {
            holder.name.setTextColor(context.resources.getColor(R.color.dark_grey))
        }

        holder.cards.setOnClickListener {
            if (list_type.equals(context.getString(R.string.categorysmall), ignoreCase = true)) {

                if (mCat != null) {
                    mCat!!.StatusChange(getmApplications)
                }

            } else {

                val bundle = Bundle()
                bundle.putSerializable(CategoryAndDefinationsDAO.BUNDLE_KEY, getmApplications)
                Shared.getInstance().callIntentWithResult(AddApplicationsFromScreenActivity::class.java, context, bundle, 2)

            }
        }


    }//End Holder Class

    fun loadJSONFromAsset(): String? {
        var json: String? = null
        try {
            val `is` = context.assets.open("definition_icons_info.json")
            val size = `is`.available()
            val buffer = ByteArray(size)
            `is`.read(buffer)
            `is`.close()
            val charset: Charset = Charsets.UTF_8
            json = String(buffer, charset)
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }

        return json
    }

    override fun getItemCount(): Int {
        return mApplicationsFiltered?.size ?: 0

    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }


    companion object {

        private val LOG_TAG = "ListApplicationCategoryAndDefinationAdapter"


    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                if (charString.isEmpty()) {
                    mApplicationsFiltered = mApplications
                } else {
                    val filteredList = ArrayList<CategoryAndDefinationsDAO>()
                    for (row in mApplications!!) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match

                        search_text = charString.toLowerCase()
                        if (row.name?.toLowerCase()?.contains(charString.toLowerCase())!!) {
                            filteredList.add(row)
                        }
                    }

                    mApplicationsFiltered = filteredList
                }

                val filterResults = FilterResults()
                filterResults.values = mApplicationsFiltered
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                mApplicationsFiltered = filterResults.values as ArrayList<CategoryAndDefinationsDAO>
                notifyDataSetChanged()
            }
        }
    }


}
