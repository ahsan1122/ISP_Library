package utilities.adapters.setup

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.esp.library.R
import com.esp.library.exceedersesp.BaseActivity
import utilities.data.setup.OrganizationPersonaDao
import utilities.data.setup.TokenDAO
import java.util.*


class ListofOrganizationSectionsAdapter(internal var sections: List<OrganizationPersonaDao>,
                                        bContext: BaseActivity, private val personas: TokenDAO) : androidx.recyclerview.widget.RecyclerView.Adapter<ListofOrganizationSectionsAdapter.ParentViewHolder>() {

    private val TAG = javaClass.simpleName
    private val context: BaseActivity

    init {
        this.context = bContext
    }


    open class ParentViewHolder(v: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v)

    inner class ActivitiesList @SuppressLint("RestrictedApi")
    constructor(v: View) : ParentViewHolder(v) {

        internal var org_list: androidx.recyclerview.widget.RecyclerView
        internal var txtlabel: TextView

        init {
            org_list = itemView.findViewById(R.id.org_list)
            txtlabel = itemView.findViewById(R.id.txtlabel)

        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.organization_section_row, parent, false)
        return ActivitiesList(v)
    }

    override fun onBindViewHolder(holder_parent: ParentViewHolder, position: Int) {

        val holder = holder_parent as ActivitiesList
        val organizationPersonaDao = sections[position]
        //recycler view for fields
        holder.org_list.setHasFixedSize(true)
        holder.org_list.isNestedScrollingEnabled = false

        val linearLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(context, androidx.recyclerview.widget.LinearLayoutManager.VERTICAL, false)
        holder.org_list.layoutManager = linearLayoutManager

        holder.txtlabel.text = organizationPersonaDao.name
        val personaDAOList: MutableList<OrganizationPersonaDao.Personas> = ArrayList()
        if (organizationPersonaDao.persoans.size > 1) {
            for (j in organizationPersonaDao.persoans.indices) {
                val personas = organizationPersonaDao.persoans[j]
                if (!personas.type.toLowerCase().equals("app", ignoreCase = true)) {
                    personaDAOList.add(personas)
                }

            }
        } else {
            for (j in organizationPersonaDao.persoans.indices) {
                val personas = organizationPersonaDao.persoans[j]
                personaDAOList.add(personas)
            }
        }

        val adapter = ListPersonaDAOAdapter(personaDAOList, organizationPersonaDao, context, personas)
        holder.org_list.adapter = adapter


    }//End Holder Class


    override fun getItemCount(): Int {
        return sections.size

    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }


}