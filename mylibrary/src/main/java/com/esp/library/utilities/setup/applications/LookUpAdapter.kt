package utilities.adapters.setup.applications

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.esp.library.R
import com.esp.library.utilities.common.Shared
import com.esp.library.exceedersesp.BaseActivity
import utilities.data.applicants.addapplication.LookUpDAO
import utilities.data.applicants.dynamics.DynamicFormSectionFieldDAO




class LookUpAdapter(val allFields: List<LookUpDAO>, con: BaseActivity, search: String, internal var fieldDAO: DynamicFormSectionFieldDAO?) : androidx.recyclerview.widget.RecyclerView.Adapter<LookUpAdapter.ViewHolder>() {
    internal var imm: InputMethodManager? = null
    internal var search_text = ""
    private var context: BaseActivity
    inner class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {

        internal var lookup_row: LinearLayout
        internal var lookup_name: TextView
        internal var cross_icon: ImageView


        init {
            lookup_name = itemView.findViewById(R.id.lookup_name)
            lookup_row = itemView.findViewById(R.id.lookup_row)
            cross_icon = itemView.findViewById(R.id.cross_icon)
        }

    }

    init {
        context = con
        search_text = search
        imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.repeater_lookup_choose, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


        if (allFields.size == 0) {
            return
        }
        if (search_text.length > 0) {

            holder.lookup_name.text = Shared.getInstance().getSearchedTextHighlight(search_text, allFields[position].name,context)
        } else {
            holder.lookup_name.text = allFields[position].name
        }

        if (fieldDAO != null) {
            if (fieldDAO!!.value != null && fieldDAO!!.value!!.length > 0) {
                if (fieldDAO!!.lookupValue!!.toLowerCase() == allFields[position].name!!.toLowerCase()) {
                    holder.cross_icon.visibility = View.VISIBLE
                    holder.lookup_name.setTextColor(ContextCompat.getColor(context, R.color.green))
                } else {
                    holder.cross_icon.visibility = View.GONE
                    holder.lookup_name.setTextColor(ContextCompat.getColor(context, R.color.dark_grey))
                }
            } else {
                holder.cross_icon.visibility = View.GONE
                holder.lookup_name.setTextColor(ContextCompat.getColor(context, R.color.dark_grey))
            }
        } else {
            holder.cross_icon.visibility = View.GONE
            holder.lookup_name.setTextColor(ContextCompat.getColor(context, R.color.dark_grey))
        }


        holder.lookup_row.setOnClickListener {
            //selected
            val bnd = Bundle()
            bnd.putSerializable(DynamicFormSectionFieldDAO.BUNDLE_KEY, fieldDAO)
            bnd.putSerializable(LookUpDAO.BUNDLE_KEY, allFields[position])
            val intent = Intent()
            intent.putExtras(bnd)
            context.setResult(Activity.RESULT_OK, intent)
            context.finish()
        }


    }//End Holder Class


    override fun getItemCount(): Int {
        return allFields.size
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }



    companion object {
        private val LOG_TAG = "LookUpAdapter"

    }

}
