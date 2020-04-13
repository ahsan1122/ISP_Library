package utilities.adapters.setup.applications

import android.content.Intent
import android.net.Uri
import android.os.StrictMode
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.esp.library.R
import com.esp.library.exceedersesp.ESPApplication
import com.esp.library.utilities.common.Constants
import com.esp.library.utilities.common.RoundedPicasso
import com.esp.library.utilities.common.Shared
import com.esp.library.utilities.common.SharedPreference
import com.esp.library.exceedersesp.BaseActivity
import com.google.gson.GsonBuilder
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import utilities.data.apis.APIs
import utilities.data.applicants.addapplication.PostApplicationsCriteriaCommentsDAO
import utilities.data.applicants.feedback.ApplicationsFeedbackDAO
import java.io.File
import java.util.concurrent.TimeUnit


class ListApplicationFeedBackAdapter(private val mApplications: List<ApplicationsFeedbackDAO>?, con: BaseActivity, internal var searched_text: String) : androidx.recyclerview.widget.RecyclerView.Adapter<ListApplicationFeedBackAdapter.ParentViewHolder>() {
    internal var pref: SharedPreference
    private var context: BaseActivity?
    open class ParentViewHolder(v: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v)

    inner class ActivitiesList(v: View) : ParentViewHolder(v) {

        internal var user_img: ImageView
        internal var date_feedback: TextView
        internal var feedback: TextView
        internal var edit_feedback: ImageView
        internal var dynamic_fields_div: LinearLayout

        init {
            user_img = itemView.findViewById(R.id.user_img)
            edit_feedback = itemView.findViewById(R.id.edit_feedback)
            date_feedback = itemView.findViewById(R.id.date_feedback)
            feedback = itemView.findViewById(R.id.feedback)
            dynamic_fields_div = itemView.findViewById(R.id.dynamic_fields_div)
        }

    }


    init {
        context = con
        pref = SharedPreference(context!!)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentViewHolder {
        val v: View
        v = LayoutInflater.from(parent.context).inflate(R.layout.repeater_application_feedback, parent, false)
        return ActivitiesList(v)
    }


    override fun onBindViewHolder(holder_parent: ParentViewHolder, position: Int) {

        val holder = holder_parent as ActivitiesList

        holder.date_feedback.text = Shared.getInstance().getDisplayDate(context, mApplications!![position].createdOn, true)

        if (mApplications[position].comment!!.length > 150) {
            holder.feedback.text = Shared.getInstance().toSubStr(mApplications[position].comment, 150)
            holder.feedback.tag = context!!.getString(R.string.hidden)

        } else {
            holder.feedback.text = mApplications[position].comment
            holder.feedback.tag = context!!.getString(R.string.shown)
        }

        holder.feedback.setOnClickListener {
            val status = holder.feedback.tag as String

            if (status != null) {

                if (status.equals(context!!.getString(R.string.hidden), ignoreCase = true)) {
                    holder.feedback.text = mApplications[position].comment
                    holder.feedback.tag = context!!.getString(R.string.shown)
                } else {
                    holder.feedback.text = Shared.getInstance().toSubStr(mApplications[position].comment, 150)
                    holder.feedback.tag = context!!.getString(R.string.hidden)
                }
            }
        }


        if (mApplications[position].imageUrl != null && mApplications[position].imageUrl!!.length > 0) {
            Picasso.with(context)
                    .load(mApplications[position].imageUrl)
                    .placeholder(R.drawable.ic_contact_default)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .transform(RoundedPicasso())
                    .resize(30, 30)
                    .into(holder.user_img)


        }

        var ScropId: String? = null

        try {
            if (Shared.getInstance().ReadPref("scropId", "login_info", context) != null) {
                ScropId = Shared.getInstance().ReadPref("scropId", "login_info", context)
            }

        } catch (e: Exception) {
        }

        if (ScropId != null) {

            val CommentsId = mApplications[position].commentUserId.toString() + ""
            if (CommentsId == ScropId) {
                //	holder.edit_feedback.setVisibility(View.VISIBLE);
            } else {
                holder.edit_feedback.visibility = View.GONE
            }

        } else {
            holder.edit_feedback.visibility = View.GONE
        }

        if (mApplications[position].attachments != null && mApplications[position].attachments!!.size > 0) {

            holder.dynamic_fields_div.visibility = View.VISIBLE

            val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            layoutParams.setMargins(0, 15, 10, 10)
            val linearLayout = LinearLayout(context)
            val linearParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            linearLayout.orientation = LinearLayout.VERTICAL
            linearLayout.layoutParams = linearParams

            for (document in mApplications[position].attachments!!) {

                val document_view = context!!.layoutInflater.inflate(R.layout.repeater_feedback_documents, holder.dynamic_fields_div, false)

                if (document_view != null) {

                    val file_name = document_view.findViewById<TextView>(R.id.file_name)
                    file_name.text = document.name
                }

                linearLayout.addView(document_view)

            }
            holder.dynamic_fields_div.addView(linearLayout)


        } else {
            holder.dynamic_fields_div.visibility = View.GONE
        }

        holder.edit_feedback.setOnClickListener {
            val applicationsFeedbackDAO = mApplications[position]
            AddCriterComments(applicationsFeedbackDAO)
        }

    }//End Holder Class


    override fun getItemCount(): Int {
        return mApplications?.size ?: 0

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

    fun AddCriterComments(post: ApplicationsFeedbackDAO?) {

        val builder = AlertDialog.Builder(context!!)

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_layout, null)
        val input = view.findViewById<EditText>(R.id.reason)
        input.setHint(R.string.please_add_comment)
        builder.setView(view)

        if (post != null) {
            builder.setTitle(R.string.edit_comment)

            if (post.comment != null && post.comment!!.length > 0) {
                input.setText(post.comment)
            }

        } else {
            builder.setTitle(R.string.add_comment)
        }



        builder.setPositiveButton(R.string.save) { dialog, which ->
            val m_Text = input.text.toString()

            if (m_Text != null && m_Text.length > 0) {

                val post_comments = PostApplicationsCriteriaCommentsDAO()
                post_comments.assessmentId = post!!.assessmentId
                post_comments.comments = m_Text

                if (post != null) {
                    post_comments.id = post.id
                }

                AddEditComments(post_comments)

                dialog.cancel()

            }
            dialog.cancel()
        }



        builder.setNegativeButton(R.string.cancel) { dialog, which -> dialog.cancel() }

        val dialog = builder.create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(context!!.resources.getColor(R.color.black))


        if (post != null && post.comment != null && post.comment!!.length > 0) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
        } else {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
        }


        input.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int,
                                       count: Int) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int,
                                           after: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                // Check if edittext is empty
                if (TextUtils.isEmpty(s)) {
                    //Disable ok button
                    dialog.getButton(
                            AlertDialog.BUTTON_POSITIVE).isEnabled = false
                } else {
                    // Something into edit text. Enable the button.
                    dialog.getButton(
                            AlertDialog.BUTTON_POSITIVE).isEnabled = true
                }

            }
        })
    }


    fun AddEditComments(post: PostApplicationsCriteriaCommentsDAO) {

        try {

            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY
            val httpClient = OkHttpClient.Builder()
            httpClient.addInterceptor(logging)

            httpClient.addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                        .header("locale", Shared.getInstance().getLanguage(context))
                        .header("Authorization", "bearer " + ESPApplication.getInstance().user.loginResponse?.access_token)
                val request = requestBuilder.build()
                chain.proceed(request)
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

            val UserComments = RequestBody.create(MediaType.parse("text/plain"), post.comments)
            val status_call = apis.EditComments(post.id, post.assessmentId, UserComments)
            status_call.enqueue(object : Callback<Int> {
                override fun onResponse(call: Call<Int>, response: Response<Int>?) {

                    if (response != null && response.body() != null && response.body() > 0) {
                        notifyDataSetChanged()
                    }
                }

                override fun onFailure(call: Call<Int>, t: Throwable?) {

                    if (t != null && context != null) {
                        Shared.getInstance().showAlertMessage(pref.getlabels().application, context?.getString(R.string.filter_error), context)

                    }

                }
            })

        } catch (ex: Exception) {
            if (ex != null) {

                if (ex != null && context != null) {
                    Shared.getInstance().showAlertMessage(pref.getlabels().application, context?.getString(R.string.some_thing_went_wrong), context)
                }
            }
        }

    }


    /*
	private ApplicationsFeedbackDAO DownLoadFile(InputStream inputStream, ApplicationsFeedbackDAO attachmentsDAO){

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);

		String fileName = "";

		if(attachmentsDAO.getAttachments()!=null && attachmentsDAO.getAttachments().getName()!=null && attachmentsDAO.getAttachments().getName().length()>0){
			fileName = attachmentsDAO.getAttachments().getName();
		}

		if(attachmentsDAO.getAttachments()!=null && attachmentsDAO.getAttachments().getName()!=null && attachmentsDAO.getAttachments().getName().length()>0){
			fileName = attachmentsDAO.getAttachments().getName();
		}


		File file = new File(Constants.FOLDER_PATH+"/"+Constants.FOLDER_NAME,fileName );
		OutputStream output = null;
		try {
			output = new FileOutputStream(file);

			byte[] buffer = new byte[1024]; // or other buffer size
			int read;

			while ((read = inputStream.read(buffer)) != -1) {
				output.write(buffer, 0, read);
			}
			output.flush();
		} catch (IOException e) {
			attachmentsDAO.getAttachments().setFileDownling(false);
			attachmentsDAO.getAttachments().setFileDownloaded(false);
			//RefreshList();

			return attachmentsDAO;
		} finally {
			try {
				if (output != null) {
					output.close();
				}
				else{
				}
			} catch (IOException e){
				attachmentsDAO.getAttachments().setFileDownling(false);
				attachmentsDAO.getAttachments().setFileDownloaded(false);
				//RefreshList();
				return attachmentsDAO;
			}
		}

		attachmentsDAO.getAttachments().setFileDownloaded(true);
		attachmentsDAO.getAttachments().setFileDownling(false);
		return attachmentsDAO;
	}

	*/private fun OpenFile(filePath: String) {
        try {
            val file = File(filePath)
            if (file != null) {

                val builder = StrictMode.VmPolicy.Builder()
                StrictMode.setVmPolicy(builder.build())

                val i = Intent(Intent.ACTION_VIEW)
                i.setDataAndType(Uri.fromFile(file), Shared.getInstance().getMimeType(file.path))
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context!!.startActivity(i)
            }


        } catch (e: Exception) {
            Shared.getInstance().messageBox(context!!.getString(R.string.appnotavailable), context)

        }

    }

    companion object {

        private val LOG_TAG = "ListApplicationFeedBackAdapter"


    }

}
