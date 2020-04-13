package com.esp.library.exceedersesp.controllers.feedback

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.PopupMenu
import com.bumptech.glide.Glide
import com.esp.library.R
import com.esp.library.exceedersesp.controllers.fieldstype.classes.AttachmentItem
import com.esp.library.exceedersesp.controllers.fieldstype.other.AttachmentImageDownload
import com.esp.library.utilities.common.CustomLogs
import com.esp.library.utilities.common.Shared
import com.esp.library.utilities.common.SharedPreference
import okhttp3.OkHttpClient
import okhttp3.Request
import utilities.data.CriteriaRejectionfeedback.FeedbackDAO
import utilities.data.applicants.dynamics.DyanmicFormSectionFieldDetailsDAO
import utilities.interfaces.FeedbackConfirmationListener
import java.io.IOException


class ApplicationFeedbackAdapter(val feedbackList: List<FeedbackDAO>, con: Context, isshowCheckBox: Boolean)
    : androidx.recyclerview.widget.RecyclerView.Adapter<ApplicationFeedbackAdapter.ParentViewHolder>() {


    var TAG = "ApplicationFeedbackAdapter"
    private var context: Context
    var pref: SharedPreference? = null
    var isShowCheckBox: Boolean = false
    var feedbackConfirmationListener: FeedbackConfirmationListener? = null


    open class ParentViewHolder(v: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v)

    inner class ActivitiesList(v: View) : ParentViewHolder(v) {


        internal var checkBox: AppCompatCheckBox
        internal var ivUserImage: ImageView
        internal var attachtypeicon: ImageView
        internal var ivdots: ImageView
        internal var txtusername: TextView
        internal var txtUserType: TextView
        internal var txtcomment: TextView
        internal var txtacctehmentname: TextView
        internal var txtextensionsize: TextView
        internal var ibeditcomment: ImageButton
        internal var rlattachmentdetails: RelativeLayout
        internal var progressbar: ProgressBar

        init {

            ibeditcomment = itemView.findViewById(R.id.ibeditcomment)
            checkBox = itemView.findViewById(R.id.checkBox)
            ivUserImage = itemView.findViewById(R.id.ivUserImage)
            attachtypeicon = itemView.findViewById(R.id.attachtypeicon)
            txtusername = itemView.findViewById(R.id.txtusername)
            txtUserType = itemView.findViewById(R.id.txtUserType)
            txtcomment = itemView.findViewById(R.id.txtcomment)
            txtacctehmentname = itemView.findViewById(R.id.txtacctehmentname)
            txtextensionsize = itemView.findViewById(R.id.txtextensionsize)
            ivdots = itemView.findViewById(R.id.ivdots)
            rlattachmentdetails = itemView.findViewById(R.id.rlattachmentdetails)
            progressbar = itemView.findViewById(R.id.progressbar)
        }
    }

    init {
        context = con
        isShowCheckBox = isshowCheckBox
        pref = SharedPreference(context)
         try{
         feedbackConfirmationListener = context as FeedbackConfirmationListener}
         catch (e:Exception){ }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.activity_feedback_row, parent, false)
        return ActivitiesList(v)

    }


    override fun onBindViewHolder(holder_parent: ParentViewHolder, position: Int) {
        val holder = holder_parent as ActivitiesList
        val feedbackDAO = feedbackList.get(position)
        holder.txtusername.text = feedbackDAO.userName
        holder.txtUserType.text = feedbackDAO.userType
        holder.txtcomment.text = feedbackDAO.comment
        holder.checkBox.isChecked = feedbackDAO.isCheck

        if(isShowCheckBox)
            holder.checkBox.visibility=View.VISIBLE
        else
            holder.checkBox.visibility=View.GONE

        if (feedbackDAO.attachemntDetails != null) {

            holder.rlattachmentdetails.visibility = View.VISIBLE

            val uploadedFileName = feedbackDAO.attachemntDetails?.name
            val extension = uploadedFileName?.substring(uploadedFileName.lastIndexOf("."))

            var fileSize = extension?.replaceFirst(".".toRegex(), "")
            if (feedbackDAO.attachemntDetails?.fileSize != null)
                fileSize = extension?.replaceFirst(".".toRegex(), "")?.toUpperCase() + ", " + feedbackDAO.attachemntDetails?.fileSize
            holder.txtextensionsize.text = fileSize

            AttachmentItem.getInstance().setIconBasedOnMimeType(extension, holder.attachtypeicon)
            holder.txtacctehmentname.text = uploadedFileName

        }

        Glide.with(context)
                .load(feedbackDAO.userImage)
                .placeholder(R.drawable.default_profile_picture)
                .into(holder.ivUserImage)

        holder.checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
            feedbackDAO.isCheck = isChecked
             feedbackConfirmationListener?.isClickable(feedbackList)
        }


        holder.rlattachmentdetails.setOnClickListener {

            if (feedbackDAO.attachemntDetails?.path.isNullOrEmpty()) {
                val getOutputMediaFile = Shared.getInstance().getOutputMediaFile(feedbackDAO.attachemntDetails?.name)!!.path
                val isFileExist = Shared.getInstance().isFileExist(getOutputMediaFile, context)
                if (isFileExist)
                    AttachmentImageDownload.getInstance().OpenImage(getOutputMediaFile, context)
                else {
                    holder.progressbar.visibility = View.VISIBLE
                    DownloadAttachment(holder.progressbar, feedbackDAO.attachemntDetails, feedbackDAO.attachemntDetails?.name!!, position)
                }
            } else
                AttachmentImageDownload.getInstance().OpenImage(feedbackDAO.attachemntDetails?.path, context)

        }

        if(feedbackDAO.isOwner)
            holder.ibeditcomment.visibility=View.GONE
        else
            holder.ibeditcomment.visibility=View.GONE


           holder.ibeditcomment.setOnClickListener { v -> showEditMenu(v,feedbackDAO) }


    }

    private fun showEditMenu(v: View, feedbackDAO: FeedbackDAO) {


        val popup = PopupMenu(context, v)
        popup.inflate(R.menu.menu_list_item_add_application_fields)
        val menuOpts = popup.menu
        popup.gravity = Gravity.CENTER

        val action_remove = menuOpts.findItem(R.id.action_remove)
        val action_edit = menuOpts.findItem(R.id.action_edit)
        action_remove.setVisible(false)

        popup.setOnMenuItemClickListener { menuItem ->

            val id = menuItem.itemId

            if (id == R.id.action_edit) {
                feedbackConfirmationListener?.editComment(feedbackDAO)
            }

            false
        }


        popup.show()

    }

    private fun DownloadAttachment(progressBar: ProgressBar, attachment: DyanmicFormSectionFieldDetailsDAO?,
                                   uploadedFileName: String, position: Int) {

        val client = OkHttpClient()
        var imgURL: String? = ""

        if (attachment?.downloadUrl != null && attachment.downloadUrl!!.length > 0) {
            imgURL = attachment.downloadUrl
        }

        CustomLogs.displayLogs("$TAG imgURL: $imgURL")

        val request = Request.Builder()
                .url(imgURL!!)
                .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {


            @Throws(IOException::class)
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {

                if (response.isSuccessful) {
                    val attachmentsDAO = AttachmentImageDownload.getInstance().DownloadImage(response.body()?.byteStream(),
                            attachment, uploadedFileName, null, null, position)
                }

                val handler = Handler(Looper.getMainLooper()) // write in onCreate function
                handler.post {
                    progressBar.visibility = View.GONE
                }
            }

            override fun onFailure(call: okhttp3.Call, e: IOException) {

            }

        })


    }//End Download

    override fun getItemCount(): Int {
        return feedbackList.size
    }

}
