package utilities.adapters.setup

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.esp.library.R
import com.esp.library.exceedersesp.BaseActivity
import com.esp.library.utilities.common.*
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import utilities.data.apis.APIs
import utilities.data.applicants.FirebaseTokenDAO
import utilities.data.setup.OrganizationPersonaDao
import utilities.data.setup.PersonaDAO
import utilities.data.setup.TokenDAO
import java.util.*
import java.util.concurrent.TimeUnit

class ListPersonaDAOAdapter(persoans: List<OrganizationPersonaDao.Personas>, internal var section: OrganizationPersonaDao,
                            context: BaseActivity, private val personas: TokenDAO) : androidx.recyclerview.widget.RecyclerView.Adapter<ListPersonaDAOAdapter.ViewHolder>() {

    private val TAG = "ListPersonaDAOAdapter"
    internal var mUser: RefreshToken
    internal var pref: SharedPreference
    private val context: BaseActivity
    internal var pDialog: AlertDialog? = null
    internal var persoans: List<OrganizationPersonaDao.Personas> = ArrayList()

    init {
        this.context = context
        this.persoans = persoans
        try {
            mUser = context as RefreshToken
        } catch (e: ClassCastException) {
            throw ClassCastException("lisnter" + " must implement on Activity")
        }
        pref = SharedPreference(context)
        pDialog = Shared.getInstance().setProgressDialog(context)
    }

    interface RefreshToken {
        fun StatusChange(update: PersonaDAO)
    }

    inner class ViewHolder(v: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v) {

        internal var cardView: LinearLayout
        internal var organization: TextView
        internal var organization_role: TextView
        internal var user_type: ImageView


        init {
            cardView = itemView.findViewById(R.id.cards)
            organization = itemView.findViewById(R.id.organization)
            organization_role = itemView.findViewById(R.id.organization_role)
            user_type = itemView.findViewById(R.id.user_type)
        }

    }


    /* public ListPersonaDAOAdapter(List<PersonaDAO> myDataset, BaseActivity con, String ref_token, boolean isMenu_) {

        mOrgs = myDataset;
        refresh_token = ref_token;
        context = con;
        isMenu = isMenu_;
        try {
            mUser = (RefreshToken) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("lisnter" + " must implement on Activity");
        }
        pref = new SharedPreference(context);

    }*/

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View
        v = LayoutInflater.from(parent.context).inflate(R.layout.repeater_org_list, parent, false)
        return ViewHolder(v)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val personaDAO = persoans[position]

        /*val applicantPersonaId = personas.applicantPersonaId?.toIntOrNull() ?: 0
        if (applicantPersonaId > 0 && (personas.role.equals(context.getString(R.string.admin), ignoreCase = true)
                        || personas.role.equals(context.getString(R.string.user), ignoreCase = true))) {
            holder.organization_role.text = context.getString(R.string.assessor) + " - " + section.name
        }
        else
            holder.organization_role.text = context.getString(R.string.applicant) + " - " + section.name*/

        if (personaDAO.type.toLowerCase().equals("app", ignoreCase = true)) {
            holder.organization_role.text = context.getString(R.string.applicant) + " - " + section.name
        } else {
            holder.organization_role.text = context.getString(R.string.assessor) + " - " + section.name
        }
        Glide.with(context).load(personaDAO.imagerUrl).placeholder(R.drawable.default_profile_picture)
                .error(R.drawable.default_profile_picture).into(holder.user_type)
        holder.organization.text = personaDAO.name


        holder.cardView.setOnClickListener {
            val personaDAO1 = PersonaDAO()
            personaDAO1.refresh_token = personas.refresh_token
            personaDAO1.id = personaDAO.id.toString()

            //mUser.StatusChange(personaDAO1)
            CustomLogs.displayLogs(TAG + " personaDAO.id: " + personaDAO1.id + " personaDAO.getOrgId(): " + section.id)
            pref.saveLocales(section.supportedLocales)

            pref.savePersonaId(personaDAO1.id!!.toInt())
            pref.saveOrganizationId(section.id)

            if (personaDAO.type?.toLowerCase().equals("app", ignoreCase = true)) {
                pref.saveSelectedUserRole(Enums.applicant.toString())
            } else {
                pref.saveSelectedUserRole(Enums.assessor.toString())
            }

            postFirebaseToken(personaDAO1.id)

            /*  when (pref.firebaseId == 0) {
                  true -> postFirebaseToken(personaDAO1.id, section.id)
                  false -> mUser.StatusChange(personaDAO1)
              }*/


        }


    }//End Holder Class

    private fun postFirebaseToken(personaId: String?) {

        start_loading_animation()


        val firebaseTokenDAO = FirebaseTokenDAO()
        firebaseTokenDAO.fbTokenId = pref.firebaseId
        firebaseTokenDAO.personaId = pref.personaId
        firebaseTokenDAO.token = pref.firebaseToken
        firebaseTokenDAO.organizationId = pref.organizationId
        firebaseTokenDAO.deviceId = Shared.getInstance().getDeviceId(context)

        try {
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY
            val httpClient = OkHttpClient.Builder()
            httpClient.addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                        .header("locale", Shared.getInstance().getLanguageSimpleContext(context))
                        .header("Authorization", "bearer " + personas.access_token);
                //
                val request = requestBuilder.build()
                chain.proceed(request)
            }
            if (Constants.WRITE_LOG) {
                httpClient.addInterceptor(logging)
            }

            httpClient.connectTimeout(10, TimeUnit.SECONDS)
            httpClient.readTimeout(10, TimeUnit.SECONDS)
            httpClient.writeTimeout(10, TimeUnit.SECONDS)

            val gson = GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                    .create()

            val retrofit = Retrofit.Builder()
                    .baseUrl(Constants.base_url)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(httpClient.build())

                    .build()


            val apis = retrofit.create(APIs::class.java)
            var firebase_call = apis.postFirebaseToken(firebaseTokenDAO)

            firebase_call.enqueue(object : Callback<FirebaseTokenDAO> {
                override fun onResponse(call: Call<FirebaseTokenDAO>, response: Response<FirebaseTokenDAO>?) {
                    stop_loading_animation()
                    if (response?.body() != null) {

                        if (response.body().status) {
                            val fbid = response.body().data as Double
                            pref.saveFirebaseId(fbid.toInt())
                            val personaDAO1 = PersonaDAO()
                            personaDAO1.refresh_token = personas.refresh_token
                            personaDAO1.id = personaId.toString()
                            mUser.StatusChange(personaDAO1)
                            CustomLogs.displayLogs(TAG + " personaDAO.id: " + "$personaId" + " personaDAO.getOrgId(): " + section.id)
                            pref.saveLocales(section.supportedLocales)
                        } else
                            Shared.getInstance().showAlertMessage(context.getString(R.string.error), response.body().errorMessage, context)

                    } else
                        Shared.getInstance().showAlertMessage(context.getString(R.string.error), context.getString(R.string.some_thing_went_wrong), context)

                }

                override fun onFailure(call: Call<FirebaseTokenDAO>, t: Throwable) {
                    CustomLogs.displayLogs("$TAG ${t.printStackTrace()}")
                    stop_loading_animation()
                    Shared.getInstance().showAlertMessage(context.getString(R.string.error), context.getString(R.string.some_thing_went_wrong), context)

                }
            })

        } catch (ex: Exception) {

            ex.printStackTrace()
            stop_loading_animation()
            Shared.getInstance().showAlertMessage(context.getString(R.string.error), context.getString(R.string.some_thing_went_wrong), context)

        }

    }

    private fun start_loading_animation() {

        try {
            if (!pDialog!!.isShowing)
                pDialog!!.show()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun stop_loading_animation() {
        try {
            if (pDialog!!.isShowing)
                pDialog!!.dismiss()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return persoans.size

    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    private fun RefreshList() {
        notifyDataSetChanged()
    }


}
