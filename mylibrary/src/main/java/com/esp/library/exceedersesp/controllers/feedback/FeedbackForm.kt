package com.esp.library.exceedersesp.controllers.feedback

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import com.esp.library.R
import com.esp.library.exceedersesp.controllers.fieldstype.classes.AttachmentItem
import com.esp.library.exceedersesp.controllers.fieldstype.other.AttachmentImageDownload
import com.esp.library.ipaulpro.afilechooser.utils.FileUtils
import com.esp.library.utilities.common.RealPathUtil
import com.esp.library.utilities.common.Shared
import com.esp.library.utilities.common.SharedPreference
import com.esp.library.exceedersesp.BaseActivity
import com.esp.library.exceedersesp.controllers.applications.ActivityStageDetails
import com.esp.library.utilities.common.Enums
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_feedback_form.*
import kotlinx.android.synthetic.main.activity_stage_detail.toolbar
import kotlinx.android.synthetic.main.feedback_add_section.*
import okhttp3.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import utilities.data.CriteriaRejectionfeedback.FeedbackDAO
import utilities.data.applicants.addapplication.PostApplicationsStatusDAO
import utilities.data.applicants.dynamics.DyanmicFormSectionFieldDetailsDAO
import utilities.data.applicants.dynamics.DynamicFormValuesDAO
import utilities.data.applicants.dynamics.DynamicResponseDAO
import utilities.data.applicants.dynamics.DynamicStagesCriteriaListDAO
import utilities.data.applicants.feedback.ApplicationsFeedbackDAO
import utilities.interfaces.FeedbackConfirmationListener
import java.io.File
import java.io.IOException

class FeedbackForm : BaseActivity(), FeedbackConfirmationListener {


    var dyanmicFormSectionFieldDetailsDAO: DyanmicFormSectionFieldDetailsDAO? = null
    var context: BaseActivity? = null
    private val REQUEST_CHOOSER = 12345
    internal var pDialog: AlertDialog? = null
    val feedbackList = ArrayList<FeedbackDAO>()
    var actualResponse: DynamicResponseDAO? = null
    var pref: SharedPreference? = null
    var isApproveClick: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback_form)
        initialize()

        llattachment.setOnClickListener {
            val getContentIntent = Intent(Intent.ACTION_GET_CONTENT)
            getContentIntent.type = "*/*"
            getContentIntent.addCategory(Intent.CATEGORY_OPENABLE)
            val intent = Intent.createChooser(getContentIntent, getString(R.string.selectafile))
            startActivityForResult(intent, REQUEST_CHOOSER)
        }

        ivdots.setOnClickListener { v -> showRemoveMenu(v) }





        txtcomment.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun afterTextChanged(editable: Editable) {

                val outputedText = editable.toString()

                when (outputedText.isNotEmpty()) {
                    true -> {
                        btaddcomment.isEnabled = true
                        btaddcomment.alpha = 1f
                    }
                    false -> {
                        btaddcomment.isEnabled = false
                        btaddcomment.alpha = 0.5f
                    }

                }

            }
        })


        btaddcomment.setOnClickListener {


            Shared.getInstance().hideKeyboard(this)

            when (Shared.getInstance().isWifiConnected(context)) {
                true -> {
                    if (dyanmicFormSectionFieldDetailsDAO != null)
                        updateLoadImageForField(dyanmicFormSectionFieldDetailsDAO?.uri!!)
                    else {
                        UpLoadFile(null)
                    }
                }
                false -> Shared.getInstance().showAlertMessage(context?.getString(R.string.internet_error_heading), context?.getString(R.string.internet_connection_error), context)
            }


        }

        rlattachmentdetails.setOnClickListener {
           // AttachmentImageDownload.getInstance().OpenImage(dyanmicFormSectionFieldDetailsDAO?.path, context)



            val getOutputMediaFile = Shared.getInstance().getOutputMediaFile(dyanmicFormSectionFieldDetailsDAO?.name)!!.path
            val isFileExist = Shared.getInstance().isFileExist(getOutputMediaFile, context)
            if (isFileExist)
                AttachmentImageDownload.getInstance().OpenImage(getOutputMediaFile, context)
            else {
                progressbar.visibility=View.VISIBLE
                DownloadAttachment(dyanmicFormSectionFieldDetailsDAO, dyanmicFormSectionFieldDetailsDAO?.name!!)
            }
        }

        when (Shared.getInstance().isWifiConnected(context)) {
            true -> getApplicationFeedBack(actualResponse?.applicationId.toString())
            false -> Shared.getInstance().showAlertMessage(context?.getString(R.string.internet_error_heading), context?.getString(R.string.internet_connection_error), context)
        }

        btconfirm.setOnClickListener {

            val dynamicStagesCriteriaListDAO = intent.getSerializableExtra("criteriaListDAO") as DynamicStagesCriteriaListDAO

            val criteriaFormValues = getCriteriaFormValues(dynamicStagesCriteriaListDAO)
            val post = PostApplicationsStatusDAO()
            post.isAccepted = isApproveClick
            post.applicationId = actualResponse?.applicationId!!
            post.assessmentId = dynamicStagesCriteriaListDAO.assessmentId
            post.comments = ""
            post.stageId = dynamicStagesCriteriaListDAO.stageId
            post.values = criteriaFormValues

            when (Shared.getInstance().isWifiConnected(context)) {
                true -> stagefeedbackSubmitForm(post)
                false -> Shared.getInstance().showAlertMessage(context?.getString(R.string.internet_error_heading), context?.getString(R.string.internet_connection_error), context)
            }
        }


    }


    private fun initialize() {
        context = this
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowHomeEnabled(false)
        supportActionBar!!.title = ""
        toolbar.navigationIcon = ContextCompat.getDrawable(context!!, R.drawable.ic_nav_back)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        toolbar.navigationIcon!!.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        toolbar_heading.text = getString(R.string.feedback)
        pDialog = Shared.getInstance().setProgressDialog(bContext)
        pref = SharedPreference(context)
        isApproveClick = intent.getBooleanExtra("isAccepted", false)
        btaddcomment.isEnabled = false
        btaddcomment.alpha = 0.5f


        if (!isApproveClick) {
            btconfirm.isEnabled = false
            btconfirm.alpha = 0.5f
            btconfirm.text=getString(R.string.rejectfeedback)
        }

        rvCommentsList.setHasFixedSize(true)
        rvCommentsList.isNestedScrollingEnabled = false
        val linearLayoutManagerCrteria = androidx.recyclerview.widget.LinearLayoutManager(context, androidx.recyclerview.widget.LinearLayoutManager.VERTICAL, false)
        rvCommentsList.layoutManager = linearLayoutManagerCrteria
        val actualResponseJson = intent.getStringExtra("actualResponseJson")
        actualResponse = Gson().fromJson<DynamicResponseDAO>(actualResponseJson, DynamicResponseDAO::class.java)

    }

    private fun getCriteriaFormValues(criteriaListDAO: DynamicStagesCriteriaListDAO): List<DynamicFormValuesDAO> {
        var sectionId = 0
        val formValuesList = java.util.ArrayList<DynamicFormValuesDAO>()
        val form = criteriaListDAO.form
        if (form.sections != null) {
            for (sections in form.sections!!) {
                sectionId = sections.id
                if (sections.fields != null) {
                    for (dynamicFormSectionFieldDAO in sections.fields!!) {
                        val value = DynamicFormValuesDAO()
                        value.sectionCustomFieldId = dynamicFormSectionFieldDAO.sectionCustomFieldId
                        value.type = dynamicFormSectionFieldDAO.type
                        value.value = dynamicFormSectionFieldDAO.value
                        value.sectionId = sectionId
                        value.details = value.details
                        if (dynamicFormSectionFieldDAO.type == 11) {
                            var finalValue = value.value
                            if (finalValue != null && !finalValue.isEmpty())
                                finalValue += ":" + dynamicFormSectionFieldDAO.selectedCurrencyId + ":" + dynamicFormSectionFieldDAO.selectedCurrencySymbol

                            value.value = finalValue
                        }
                        formValuesList.add(value)
                    }
                }
            }
        }

        return formValuesList
    }

    private fun DownloadAttachment(attachment: DyanmicFormSectionFieldDetailsDAO?,
                                   uploadedFileName: String) {

        val client = OkHttpClient()
        var imgURL: String? = ""

        if (attachment?.downloadUrl != null && attachment.downloadUrl!!.length > 0) {
            imgURL = attachment.downloadUrl
        }

        val request = Request.Builder()
                .url(imgURL!!)
                .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {


            @Throws(IOException::class)
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {

                if (response.isSuccessful) {
                    val attachmentsDAO = AttachmentImageDownload.getInstance().DownloadImage(response.body()?.byteStream(),
                            attachment, uploadedFileName, null, null, 0)
                }

                val handler = Handler(Looper.getMainLooper()) // write in onCreate function
                handler.post {
                    progressbar.visibility = View.GONE
                }
            }

            override fun onFailure(call: okhttp3.Call, e: IOException) {

            }

        })


    }//End Download

    private fun showRemoveMenu(v: View) {


        val popup = PopupMenu(context!!, v)
        popup.inflate(R.menu.menu_list_item_add_application_fields)
        val menuOpts = popup.menu
        popup.gravity = Gravity.CENTER

        val action_remove = menuOpts.findItem(R.id.action_remove)
        val action_edit = menuOpts.findItem(R.id.action_edit)
        action_edit.setVisible(false)

        popup.setOnMenuItemClickListener { menuItem ->

            val id = menuItem.itemId
            if (id == R.id.action_remove) {
                rlattachmentdetails.visibility = View.GONE
                dyanmicFormSectionFieldDetailsDAO = null
            }


            false
        }


        popup.show()

    }



    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == REQUEST_CHOOSER && data != null) {

                val uri = data.data

                if (uri != null) {

                    val path = RealPathUtil.getPath(bContext, uri)
                    val file = File(path)
                    val attachmentFileSize = Shared.getInstance().getAttachmentFileSize(file)
                    txtacctehmentname.text = file.getName()
                    val extension = file.getName().substring(file.getName().lastIndexOf("."))
                    val fileSize = extension.replaceFirst(".".toRegex(), "").toUpperCase() + ", " + attachmentFileSize
                    txtextensionsize.text = fileSize
                    AttachmentItem.getInstance().setIconBasedOnMimeType(extension, attachtypeicon)


                    dyanmicFormSectionFieldDetailsDAO = DyanmicFormSectionFieldDetailsDAO()
                    dyanmicFormSectionFieldDetailsDAO?.mimeType = FileUtils.getMimeType(file)
                    dyanmicFormSectionFieldDetailsDAO?.name = file.getName()
                    dyanmicFormSectionFieldDetailsDAO?.uri = uri
                    dyanmicFormSectionFieldDetailsDAO?.path = file.absolutePath
                    dyanmicFormSectionFieldDetailsDAO?.createdOn = Shared.getInstance().GetCurrentDateTime()
                    dyanmicFormSectionFieldDetailsDAO?.fileSize = attachmentFileSize

                    rlattachmentdetails.visibility = View.VISIBLE


                }


            }

        }

    }

    fun updateLoadImageForField(uri: Uri) {


        var body: MultipartBody.Part? = null

        try {

            body = Shared.getInstance().prepareFilePart(uri, bContext)
            UpLoadFile(body)

        } catch (e: Exception) {
            Shared.getInstance().errorLogWrite("FILE", e.message)
        }

    }

    private fun UpLoadFile(body: MultipartBody.Part?) {

        start_loading_animation()
        try {
            val UserComments = RequestBody.create(MediaType.parse("text/plain"), txtcomment.text.toString())

            val call_upload = Shared.getInstance().retroFitObject(context).feedbackComments(body, actualResponse?.applicationId!!,
                    UserComments, true)
            call_upload.enqueue(object : Callback<Int> {
                override fun onResponse(call: Call<Int>, response: Response<Int>?) {

                    if (response != null && response.body() != null) {
                        txtcomment.setText("")
                        btaddcomment.isEnabled = false
                        btaddcomment.alpha = 0.5f
                        rlattachmentdetails.visibility = View.GONE
                        dyanmicFormSectionFieldDetailsDAO = null
                        when (Shared.getInstance().isWifiConnected(context)) {
                            true -> getApplicationFeedBack(actualResponse?.applicationId.toString())
                            false -> Shared.getInstance().showAlertMessage(context?.getString(R.string.internet_error_heading), context?.getString(R.string.internet_connection_error), context)
                        }


                    } else {
                        stop_loading_animation()
                        Shared.getInstance().messageBox(getString(R.string.some_thing_went_wrong), bContext)
                    }

                }

                override fun onFailure(call: Call<Int>, t: Throwable) {
                    stop_loading_animation()
                    Shared.getInstance().showAlertMessage(getString(R.string.error), getString(R.string.some_thing_went_wrong), bContext)
                }
            })

        } catch (ex: Exception) {
            stop_loading_animation()
            Shared.getInstance().showAlertMessage(getString(R.string.error), getString(R.string.some_thing_went_wrong), bContext)

        }

    }//LoggedInUser end

    fun getApplicationFeedBack(id: String) {

        start_loading_animation()

        val apis = Shared.getInstance().retroFitObject(context)

        val detail_call = apis.GetApplicationFeedBack(id)
        detail_call.enqueue(object : Callback<List<ApplicationsFeedbackDAO>> {
            override fun onResponse(call: Call<List<ApplicationsFeedbackDAO>>, response: Response<List<ApplicationsFeedbackDAO>>?) {
                stop_loading_animation()
                if (response?.body() != null && response.body().size > 0) {
                    val body = response.body()
                    feedbackList.clear()
                    for (i in 0 until body.size) {
                        val applicationsFeedbackDAO = body.get(i)
                        val feedbackDao = FeedbackDAO()
                        feedbackDao.userName = applicationsFeedbackDAO.fullName
                        feedbackDao.comment = applicationsFeedbackDAO.comment
                        feedbackDao.isCheck = true
                        feedbackDao.userImage = applicationsFeedbackDAO.imageUrl
                        feedbackDao.isOwner = applicationsFeedbackDAO.isOwner

                        var role=context?.getString(R.string.member)
                        if(applicationsFeedbackDAO.isAdmin)
                            role = Enums.assessor.toString()

                       /* if (role.equals(context?.getString(R.string.applicant), ignoreCase = true))
                            role = getString(R.string.member)*/


                        feedbackDao.userType = role


                        for (j in 0 until applicationsFeedbackDAO.attachments!!.size) {
                            val get = applicationsFeedbackDAO.attachments?.get(j)
                            val dyanmicFormSectionFieldDetailsDAO = DyanmicFormSectionFieldDetailsDAO()
                            dyanmicFormSectionFieldDetailsDAO.mimeType = get?.mimeType
                            dyanmicFormSectionFieldDetailsDAO.name = get?.name
                            dyanmicFormSectionFieldDetailsDAO.downloadUrl = get?.downloadUrl
                            dyanmicFormSectionFieldDetailsDAO.createdOn = get?.createdOn
                            feedbackDao.attachemntDetails = dyanmicFormSectionFieldDetailsDAO
                        }
                        feedbackList.add(feedbackDao)
                    }
                    feedbackList.reverse()
                    val feedbackAdapter = ApplicationFeedbackAdapter(feedbackList, context!!, true)
                    rvCommentsList.adapter = feedbackAdapter
                    validateFields(feedbackList)
                }
            }

            override fun onFailure(call: Call<List<ApplicationsFeedbackDAO>>, t: Throwable) {
                stop_loading_animation()
                Shared.getInstance().showAlertMessage(pref?.getlabels()?.application, getString(R.string.some_thing_went_wrong), context)
            }
        })


    }

    fun stagefeedbackSubmitForm(post: PostApplicationsStatusDAO) {
        start_loading_animation()
        try {


            val status_call = Shared.getInstance().retroFitObject(context).AcceptRejectApplication(post)


            status_call.enqueue(object : Callback<Int> {
                override fun onResponse(call: Call<Int>, response: Response<Int>) {
                    stop_loading_animation()
                    ActivityStageDetails.isGoBAck = true
                    isComingFromFeedbackFrom = true
                    onBackPressed()

                }

                override fun onFailure(call: Call<Int>, t: Throwable?) {
                    stop_loading_animation()
                    Shared.getInstance().showAlertMessage(pref?.getlabels()?.application, getString(R.string.some_thing_went_wrong), bContext)
                }
            })

        } catch (ex: Exception) {
            stop_loading_animation()
            Shared.getInstance().showAlertMessage(pref?.getlabels()?.application, getString(R.string.some_thing_went_wrong), bContext)

        }

    }

    private fun start_loading_animation() {

        if (!pDialog!!.isShowing())
            pDialog?.show()


    }

    private fun stop_loading_animation() {

        if (pDialog!!.isShowing())
            pDialog?.dismiss()


    }

    override fun isClickable(feedbackList: List<FeedbackDAO>) {

        if (!isApproveClick)
            validateFields(feedbackList)

    }

    companion object {

        var isComingFromFeedbackFrom: Boolean = false


    }


    override fun editComment(feedbackDAO: FeedbackDAO) {

        rvCommentsList.visibility=View.GONE
        txtdescription.visibility=View.GONE
        btcancel.visibility=View.VISIBLE
        btconfirm.visibility=View.GONE
        txtcomment.setText(feedbackDAO.comment)
        btaddcomment.text=getString(R.string.post_comment)
        txtheading.text=getString(R.string.edit_feedback)



        /*val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        )

        btconfirm.setPadding(30,0,30,0)
        btconfirm.layoutParams = params;*/




        btcancel.setOnClickListener{
           /* val paramss = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            )
            btconfirm.layoutParams = paramss;*/

            btcancel.visibility=View.GONE
            btconfirm.visibility=View.VISIBLE

            dyanmicFormSectionFieldDetailsDAO=null
            rlattachmentdetails.visibility=View.GONE
            txtcomment.setText("")
            rvCommentsList.visibility=View.VISIBLE
            txtdescription.visibility=View.VISIBLE
            btaddcomment.text=getString(R.string.addcomment)
            txtheading.text=getString(R.string.confirmfeedback)
        }

        val fileName=feedbackDAO.attachemntDetails?.name
        txtacctehmentname.text = fileName
        val extension = fileName?.substring(fileName.lastIndexOf("."))
        AttachmentItem.getInstance().setIconBasedOnMimeType(extension, attachtypeicon)
        val extensionToShow = extension?.replaceFirst(".".toRegex(), "")?.toUpperCase()
        txtextensionsize.text = extensionToShow

        dyanmicFormSectionFieldDetailsDAO = DyanmicFormSectionFieldDetailsDAO()
        dyanmicFormSectionFieldDetailsDAO?.mimeType = feedbackDAO.attachemntDetails?.mimeType
        dyanmicFormSectionFieldDetailsDAO?.name = feedbackDAO.attachemntDetails?.name
        dyanmicFormSectionFieldDetailsDAO?.uri = feedbackDAO.attachemntDetails?.uri
        dyanmicFormSectionFieldDetailsDAO?.path = feedbackDAO.attachemntDetails?.path
        dyanmicFormSectionFieldDetailsDAO?.createdOn = Shared.getInstance().GetCurrentDateTime()
        dyanmicFormSectionFieldDetailsDAO?.fileSize = feedbackDAO.attachemntDetails?.fileSize
        rlattachmentdetails.visibility=View.VISIBLE

    }


    private fun validateFields(feedbackList: List<FeedbackDAO>) {
        for (i in 0 until this.feedbackList.size) {
            if (this.feedbackList.get(i).isCheck) {
                btconfirm.isEnabled = true
                btconfirm.alpha = 1.0f
                break
            } else {
                btconfirm.isEnabled = false
                btconfirm.alpha = 0.5f
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()

        Shared.getInstance().hideKeyboard(context)
    }

}
