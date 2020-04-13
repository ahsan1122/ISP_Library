package utilities.adapters.setup.applications

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.RelativeLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.esp.library.R
import com.esp.library.exceedersesp.BaseActivity
import de.hdodenhof.circleimageview.CircleImageView
import utilities.data.applicants.UsersListDAO
import utilities.interfaces.UserListClickListener


class ListUsersAdapter(private val userslist: List<UsersListDAO>?, con: BaseActivity, internal var searched_text: String)
    : androidx.recyclerview.widget.RecyclerView.Adapter<ListUsersAdapter.ParentViewHolder>(),Filterable  {


    private var context: BaseActivity
    var userItemClick:UserListClickListener?=null
    var usersListFiltered: List<UsersListDAO>?=null
    var usersList: List<UsersListDAO>?=null


    open class ParentViewHolder(v: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v)

    inner class ActivitiesList(v: View) : ParentViewHolder(v) {


        internal var ivuser: CircleImageView
        internal var txtusername: TextView
        internal var txtuseremail: TextView
        internal var rlitem: RelativeLayout



        init {

            ivuser = itemView.findViewById(R.id.ivuser)
            txtuseremail = itemView.findViewById(R.id.txtuseremail)
            txtusername = itemView.findViewById(R.id.txtusername)
            rlitem = itemView.findViewById(R.id.rlitem)
        }

    }


    init {
        context = con
        usersList = userslist
        usersListFiltered = userslist
        userItemClick=context as UserListClickListener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentViewHolder {
        val v: View = LayoutInflater.from(parent.context).inflate(R.layout.acitivity_user_row, parent, false)
        return ActivitiesList(v)
    }


    override fun onBindViewHolder(holder_parent: ParentViewHolder, position: Int) {

        val userslistDAO = usersListFiltered?.get(position)
        val holder = holder_parent as ActivitiesList

        holder.txtusername.text=userslistDAO?.fullName
        holder.txtuseremail.text=userslistDAO?.email
        Glide.with(context).load(userslistDAO?.pictureUrl).placeholder(R.drawable.default_profile_picture)
                .error(R.drawable.default_profile_picture).into(holder.ivuser)


        holder.rlitem.setOnClickListener{
            userItemClick?.userClick(usersListFiltered?.get(position))

        }


    }//End Holder Class



    override fun getItemCount(): Int {
        return usersListFiltered?.size ?: 0

    }


    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }


    companion object {

        private val LOG_TAG = "ListUsersAdapter"


    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                if (charString.isEmpty()) {
                    usersListFiltered = usersList
                } else {
                    val filteredList = ArrayList<UsersListDAO>()

                    for (i in 0 until usersList!!.size)
                    {
                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (usersList?.get(i)?.fullName?.toLowerCase()!!.contains(charString.toLowerCase())) {
                            filteredList.add(usersList!!.get(i))
                        }
                    }

                    usersListFiltered = filteredList
                }

                val filterResults = FilterResults()
                filterResults.values = usersListFiltered
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                usersListFiltered = filterResults.values as ArrayList<UsersListDAO>
                notifyDataSetChanged()
            }
        }
    }

}
