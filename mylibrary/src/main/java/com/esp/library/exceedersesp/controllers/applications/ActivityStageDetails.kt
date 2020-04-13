package com.esp.library.exceedersesp.controllers.applications

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.StrictMode
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.esp.library.R
import com.esp.library.exceedersesp.BaseActivity
import com.esp.library.exceedersesp.controllers.feedback.FeedbackForm
import com.esp.library.ipaulpro.afilechooser.utils.FileUtils
import com.esp.library.utilities.common.*
import com.esp.library.utilities.customevents.EventOptions
import com.esp.library.utilities.setup.applications.ApplicationCriteriaAdapter
import com.esp.library.utilities.setup.applications.ApplicationFieldsRecyclerAdapter
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_stage_detail.*
import okhttp3.MultipartBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import utilities.data.applicants.CalculatedMappedFieldsDAO
import utilities.data.applicants.addapplication.LookUpDAO
import utilities.data.applicants.addapplication.PostApplicationsStatusDAO
import utilities.data.applicants.addapplication.ResponseFileUploadDAO
import utilities.data.applicants.dynamics.*
import utilities.interfaces.CriteriaFieldsListener
import utilities.interfaces.FeedbackSubmissionClick
import java.io.File
import java.util.*


class ActivityStageDetails : BaseActivity(), CriteriaFieldsListener,
        FeedbackSubmissionClick, ApplicationFieldsRecyclerAdapter.ApplicationDetailFieldsAdapterListener {

    var TAG: String = "ActivityStageDetails"

    var context: BaseActivity? = null
    var criteriaAdapter: ApplicationCriteriaAdapter? = null;
    var dynamicStagesDAO: DynamicStagesDAO? = null
    var criteriaListCollections = ArrayList<DynamicStagesCriteriaListDAO?>()
    internal var fieldToBeUpdated: DynamicFormSectionFieldDAO? = null
    internal var pDialog: AlertDialog? = null
    private val REQUEST_CHOOSER = 12345
    private val REQUEST_LOOKUP = 2
    var pref: SharedPreference? = null
    var actualResponseJson: String? = null
    var actualResponseJsonsubmitJson: DynamicResponseDAO? = null
    var actualResponseJsonsubmitJsonTemp: DynamicResponseDAO? = null
    internal var isServiceRunning: Boolean = false
    internal var isCalculatedField: Boolean = false
    internal var isKeyboardVisible: Boolean = false

    private var valuesForCalculatedValues: DynamicResponseDAO? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stage_detail)
        initailize()
        updateTopView()
        setGravity()

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        //  criteriaListCollections.clear()
        for (i in 0 until dynamicStagesDAO?.criteriaList!!.size) {
            val getList = dynamicStagesDAO?.criteriaList?.get(i);
            val isArrayHasValue = criteriaListCollections.any { x -> x?.assessmentId == getList?.assessmentId }
            if (!isArrayHasValue) {
                if (getList?.isEnabled!!)
                    criteriaListCollections.add(getList)
            }
        }

        when (criteriaListCollections.size == 0) {
            true -> txtcriteria.visibility = View.GONE
        }

        rvCrietrias.setHasFixedSize(true)
        rvCrietrias.isNestedScrollingEnabled = false
        rvCrietrias.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        criteriaAdapter = ApplicationCriteriaAdapter(criteriaListCollections, context!!, rvCrietrias)
        criteriaAdapter?.getStage(dynamicStagesDAO!!)
        criteriaAdapter?.getActualResponse(intent.getStringExtra("actualResponseJson"))
        rvCrietrias.adapter = criteriaAdapter

        start_loading_animation()
        val handler = Handler()
        handler.postDelayed({ stop_loading_animation() }, 3000)

        KeyboardUtils.addKeyboardToggleListener(bContext) { isVisible -> isKeyboardVisible = isVisible }

    }

    private fun initailize() {
        context = this
        if (ApplicationDetailScreenActivity.criteriaWasLoaded)
            ApplicationDetailScreenActivity.criteriaWasLoaded = false
        pref = SharedPreference(context)
        pDialog = Shared.getInstance().setProgressDialog(bContext)
        actualResponseJson = intent.getStringExtra("actualResponseJson")
        actualResponseJsonsubmitJson = Gson().fromJson<DynamicResponseDAO>(actualResponseJson, DynamicResponseDAO::class.java)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowHomeEnabled(false)
        supportActionBar!!.title = ""
        toolbar.navigationIcon = ContextCompat.getDrawable(context!!, R.drawable.ic_nav_back)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        toolbar.navigationIcon!!.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
    }

    private fun updateTopView() {
        dynamicStagesDAO = intent.getSerializableExtra("dynamicStagesDAO") as DynamicStagesDAO
        txtStagename.text = dynamicStagesDAO?.name
        sequencetextvalue.text = dynamicStagesDAO?.order.toString()

        if (dynamicStagesDAO?.type.equals(getString(R.string.link), ignoreCase = true)) {
            rlcondition.visibility = View.GONE
            rlacceptreject.visibility = View.GONE
        } else {
            if (dynamicStagesDAO != null && dynamicStagesDAO!!.isAll) {

                conditiontextvalue.text = context?.getString(R.string.all)
            } else {
                conditiontextvalue.text = context?.getString(R.string.any)
            }

            if (dynamicStagesDAO?.criteriaList != null && dynamicStagesDAO?.criteriaList!!.isNotEmpty()) {
                acceptencetextvalue.text = dynamicStagesDAO?.criteriaList!!.size.toString()
            } else {
                acceptencetextvalue.text = "0"
            }
        }

    }


    override fun validateCriteriaFields(dynamicStagesCriteriaList: DynamicStagesCriteriaListDAO) {

        var adapter_list: List<DynamicFormSectionFieldDAO>? = null
        if (criteriaAdapter != null) {
            adapter_list = criteriaAdapter?.getAllCriteriaFields()
        }
        var isAllFieldsValidateTrue = true

        val criteriaId = dynamicStagesCriteriaList.id

        for (i in 0 until dynamicStagesCriteriaList.form.sections!!.size) {
            val dynamicFormSectionDAO = dynamicStagesCriteriaList.form.sections!![i]

            for (k in 0 until dynamicFormSectionDAO.fields!!.size) {
                val id = dynamicFormSectionDAO.fields!![k].id

                if (adapter_list != null && adapter_list.size > 0) {

                    for (dynamicFormSectionFieldDAO in adapter_list) {

                        if (dynamicFormSectionFieldDAO.id == id) {

                            if (dynamicFormSectionFieldDAO.isRequired) {
                                if (!dynamicFormSectionFieldDAO.isValidate) {
                                    isAllFieldsValidateTrue = false
                                    break
                                }

                            }


                        }
                    }
                }
            }


        }

        for (q in 0 until criteriaAdapter?.criteriaList!!.size) {
            val id = criteriaAdapter?.criteriaList?.get(q)?.id
            if (criteriaId == id) {
                criteriaAdapter?.criteriaList!!.get(q)?.isValidate = isAllFieldsValidateTrue
                try {
                    criteriaAdapter?.notifyChangeIfAny(criteriaId)
                } catch (e: Exception) {

                }
            }

        }

    }

    override fun onFieldValuesChanged() {

    }


    override fun feedbackClick(isAccepted: Boolean, criteriaListDAO: DynamicStagesCriteriaListDAO?, dynamicStagesDAO: DynamicStagesDAO?, position: Int) {


        var isApproved = isAccepted

        if (dynamicStagesDAO != null) {
            val count: Int = 0
            val dynamicStagesDAO1 = actualResponseJsonsubmitJson?.stages?.get(actualResponseJsonsubmitJson?.stages!!.size - 1)

            if (dynamicStagesDAO.id == dynamicStagesDAO1?.id) {
                val size = dynamicStagesDAO1.criteriaList!!.size
                if (dynamicStagesDAO.isAll) // if stage status is ALL then take feedback only on last criteria weather approve or reject
                {

                    //if last stage and last criteria then open feedback on approve button
                    //if last stage and any criteria then open feedback on reject button

                    val getCount = criteriaCount(dynamicStagesDAO1, count, size)
                    if (getCount == size - 1 && isApproved)
                        isApproved = false

                } else {

                    //if last stage and last criteria then open feedback on reject button
                    //if last stage and any criteria then open feedback on approve button

                    if (!isApproved) {
                        val getCount = criteriaCount(dynamicStagesDAO1, count, size)
                        if (getCount != size - 1)
                            isApproved = true
                    } else if (dynamicStagesDAO.id == dynamicStagesDAO1.id) {
                        isApproved = false
                    }
                }
            }
        }


        if (!isApproved) {
            val intent = Intent(this, FeedbackForm::class.java)
            intent.putExtra("actualResponseJson", actualResponseJson)
            intent.putExtra("criteriaListDAO", criteriaListDAO)
            intent.putExtra("isAccepted", isAccepted)
            startActivity(intent)
        } else {
            // CustomLogs.displayLogs(TAG+" Approved")
            SubmitStageRequest(isAccepted, criteriaListDAO!!, position)
        }
    }

    private fun criteriaCount(dynamicStagesDAO1: DynamicStagesDAO, countt: Int, size: Int): Int {
        var count = countt
        for (i in 0 until size) {
            if (dynamicStagesDAO1.criteriaList != null) {
                val dynamicStagesCriteriaListDAO = dynamicStagesDAO1.criteriaList!![i]

                if (dynamicStagesCriteriaListDAO.assessmentStatus != null && (dynamicStagesCriteriaListDAO.assessmentStatus!!.equals(getString(R.string.accepted), ignoreCase = true) || dynamicStagesCriteriaListDAO.assessmentStatus!!.equals(getString(R.string.rejected), ignoreCase = true))) {
                    count++
                }
            }
        }

        return count
    }

    fun getFormValues(criteriaListDAO: DynamicStagesCriteriaListDAO): ArrayList<DynamicFormValuesDAO> {
        var sectionId = 0
        val formValuesList = ArrayList<DynamicFormValuesDAO>()
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
                            if (!finalValue.isNullOrEmpty()) {
                                finalValue += ":" + dynamicFormSectionFieldDAO.selectedCurrencyId + ":" + dynamicFormSectionFieldDAO.selectedCurrencySymbol
                            }
                            value.value = finalValue

                        }
                        formValuesList.add(value)
                    }
                }
            }
        }
        return formValuesList
    }

    fun SubmitStageRequest(isAccepted: Boolean, criteriaListDAO: DynamicStagesCriteriaListDAO, position: Int) {

        //Shared.getInstance().CloneAddFormWithForm(actual_response);
        val formValuesList = getFormValues(criteriaListDAO)
        // CustomLogs.displayLogs(ACTIVITY_NAME + " post.ApplicationSingleton(): " + ApplicationSingleton.getInstace().getApplication().getApplicationId());
        val post = PostApplicationsStatusDAO()

        criteriaListDAO.formValues = formValuesList
        post.isAccepted = isAccepted
        post.applicationId = actualResponseJsonsubmitJson?.applicationId!!
        post.assessmentId = criteriaListDAO.assessmentId
        post.comments = ""
        post.stageId = criteriaListDAO.stageId
        post.values = formValuesList


        CustomLogs.displayLogs(TAG + " post.getApplicationStatus(): " + post.toJson() + " toString: " + post.toString())
        stagefeedbackSubmitForm(post, criteriaListDAO, position)


    }//END SubmitRequest

    fun stagefeedbackSubmitForm(post: PostApplicationsStatusDAO, criteriaListDAO: DynamicStagesCriteriaListDAO, position: Int) {


        start_loading_animation()
        try {


            val status_call = Shared.getInstance().retroFitObject(context).AcceptRejectApplication(post)


            status_call.enqueue(object : Callback<Int> {
                override fun onResponse(call: Call<Int>, response: Response<Int>) {


                    CustomLogs.displayLogs("$TAG stagefeedbackSubmitForm: $response")
                    //  onBackPressed()
                    GetApplicationDetail(post.applicationId.toString(), criteriaListDAO, position)


                }

                override fun onFailure(call: Call<Int>, t: Throwable?) {
                    stop_loading_animation()
                    if (t != null && bContext != null) {
                        Shared.getInstance().showAlertMessage(pref?.getlabels()?.application, getString(R.string.some_thing_went_wrong), bContext)
                    }
                }
            })

        } catch (e: Exception) {
            stop_loading_animation()

            Shared.getInstance().showAlertMessage(pref?.getlabels()?.application, getString(R.string.some_thing_went_wrong), bContext)


        }

    }

    fun GetApplicationDetail(id: String, criteriaListDAO: DynamicStagesCriteriaListDAO, position: Int) {

        start_loading_animation()
        try {

            val detail_call = Shared.getInstance().retroFitObject(context).GetApplicationDetailv2(id)
            detail_call.enqueue(object : Callback<DynamicResponseDAO> {
                override fun onResponse(call: Call<DynamicResponseDAO>, response: Response<DynamicResponseDAO>?) {

                    if (response != null && response.body() != null) {

                        stop_loading_animation()

                        for (i in 0 until response.body().stages!!.size) {
                            for (j in 0 until response.body().stages?.get(i)?.criteriaList!!.size) {

                                actualResponseJsonsubmitJson = response.body()
                                val getCriteria = response.body().stages?.get(i)?.criteriaList?.get(j);
                                if (getCriteria?.id == criteriaListDAO.id) {

                                    for (k in 0 until criteriaAdapter?.criteriaList!!.size) {
                                        if (getCriteria.id == criteriaAdapter?.criteriaList?.get(k)?.id) {
                                            setValues(criteriaAdapter?.criteriaList?.get(k), getCriteria)
                                        }
                                    }
                                    criteriaAdapter?.notifyItemChanged(position)

                                }
                            }
                        }

                    } else {
                        stop_loading_animation()
                        Shared.getInstance().showAlertMessage(pref?.getlabels()?.application, getString(R.string.some_thing_went_wrong), bContext)
                    }
                }

                override fun onFailure(call: Call<DynamicResponseDAO>, t: Throwable) {
                    t.printStackTrace()
                    stop_loading_animation()
                    Shared.getInstance().showAlertMessage(pref?.getlabels()?.application, getString(R.string.some_thing_went_wrong), bContext)
                    return
                }
            })

        } catch (ex: Exception) {
            ex.printStackTrace()
            stop_loading_animation()
            Shared.getInstance().showAlertMessage(pref?.getlabels()?.application, getString(R.string.some_thing_went_wrong), bContext)
            return
        }

    }

    fun setValues(dynamicStagesCriteriaList: DynamicStagesCriteriaListDAO?, criteria: DynamicStagesCriteriaListDAO): DynamicStagesCriteriaListDAO? {

        dynamicStagesCriteriaList?.assessmentStatus = criteria.assessmentStatus
        dynamicStagesCriteriaList?.formValues = criteria.formValues
        dynamicStagesCriteriaList?.form = criteria.form
        dynamicStagesCriteriaList?.isOwner = criteria.isOwner
        dynamicStagesCriteriaList?.isValidate = criteria.isValidate
        dynamicStagesCriteriaList?.approveText = criteria.approveText
        dynamicStagesCriteriaList?.assessmentId = criteria.assessmentId
        dynamicStagesCriteriaList?.rejectText = criteria.rejectText
        return dynamicStagesCriteriaList
    }

    override fun onAttachmentFieldClicked(fieldDAO: DynamicFormSectionFieldDAO?, position: Int) {
        fieldToBeUpdated = fieldDAO
        fieldToBeUpdated?.updatePositionAttachment = position

        var getAllowedValuesCriteria = fieldToBeUpdated?.allowedValuesCriteria
        getAllowedValuesCriteria = getAllowedValuesCriteria!!.replace("\\.".toRegex(), "")


        val values = getAllowedValuesCriteria.split(",".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()

        val valuesList = ArrayList(Arrays.asList(*values))
        val refineValuesList = ArrayList<String>()
        for (j in valuesList.indices) {
            if (valuesList[j] != "-2")
                refineValuesList.add(valuesList[j])
        }

        val mimeTypes = arrayOfNulls<String>(refineValuesList.size)
        for (i in refineValuesList.indices) {
            val type = refineValuesList[i]
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(type.toLowerCase())
            if (mimeType != null)
                mimeTypes[i] = mimeType
            else
                mimeTypes[i] = type
        }

        // Intent getContentIntent = FileUtils.createGetContentIntent();

        val getContentIntent = Intent(Intent.ACTION_GET_CONTENT)
        // The MIME data type filter

        getContentIntent.type = "*/*"
        if (getAllowedValuesCriteria!!.length > 0)
            getContentIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        // Only return URIs that can be opened with ContentResolver
        getContentIntent.addCategory(Intent.CATEGORY_OPENABLE)

        val intent = Intent.createChooser(getContentIntent, getString(R.string.selectafile))
        startActivityForResult(intent, REQUEST_CHOOSER)
    }

    override fun onLookupFieldClicked(fieldDAO: DynamicFormSectionFieldDAO?, position: Int, isCalculatedMappedField: Boolean) {
        if (!pDialog!!.isShowing()) {
            fieldToBeUpdated = fieldDAO
            fieldToBeUpdated?.updatePositionAttachment = position

            val bundle = Bundle()
            bundle.putSerializable(DynamicFormSectionFieldDAO.BUNDLE_KEY, fieldToBeUpdated)


            val chooseLookupOption = Intent(bContext, ChooseLookUpOption::class.java)
            chooseLookupOption.putExtras(bundle)
            startActivityForResult(chooseLookupOption, REQUEST_LOOKUP)
        }

    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == REQUEST_CHOOSER && data != null) {

                val uri = data.data

                if (uri != null) {
                    val getMaxVal = fieldToBeUpdated!!.maxVal
                    var isFileSizeValid = true
                    if (getMaxVal > 0)
                        isFileSizeValid = Shared.getInstance().getFileSize(RealPathUtil.getPath(bContext, uri), getMaxVal)


                    if (isFileSizeValid) {

                        try {
                            UpdateLoadImageForField(fieldToBeUpdated, uri)

                        } catch (e: Exception) {
                            Shared.getInstance().messageBox(getString(R.string.pleasetryagain), bContext)
                        }

                    } else {

                        Shared.getInstance().showAlertMessage("", getString(R.string.sizeshouldbelessthen) + " " + getMaxVal + " " + getString(R.string.mb), bContext)
                    }
                }


            } else if (requestCode == REQUEST_LOOKUP && data != null) {

                val dfs = data.extras!!.getSerializable(DynamicFormSectionFieldDAO.BUNDLE_KEY) as DynamicFormSectionFieldDAO
                val lookup = data.extras!!.getSerializable(LookUpDAO.BUNDLE_KEY) as LookUpDAO
                if (fieldToBeUpdated != null && lookup != null) {
                    SetUpLookUpValues(fieldToBeUpdated!!, lookup)
                }

            }

        }

    }

    fun UpdateLoadImageForField(field: DynamicFormSectionFieldDAO?, uri: Uri) {
        if (field != null) {

            var body: MultipartBody.Part? = null

            try {

                body = Shared.getInstance().prepareFilePart(uri, bContext)
                UpLoadFile(field, body, uri)

            } catch (e: Exception) {
                Shared.getInstance().errorLogWrite("FILE", e.message)
            }

        }
    }

    private fun UpLoadFile(field: DynamicFormSectionFieldDAO?,
                           body: MultipartBody.Part, uri: Uri) {

        start_loading_animation()
        try {
            val call_upload = Shared.getInstance().retroFitObject(context).upload(body)
            call_upload.enqueue(object : Callback<ResponseFileUploadDAO> {
                override fun onResponse(call: Call<ResponseFileUploadDAO>, response: Response<ResponseFileUploadDAO>?) {
                    stop_loading_animation()
                    if (response != null && response.body() != null) {

                        if (field != null) {

                            try {

                                //File file = FileUtils.getFile(bContext, uri);

                                var file: File? = null
                                val path_arslan = RealPathUtil.getPath(bContext, uri)
                                file = File(path_arslan)


                                val attachmentFileSize = Shared.getInstance().getAttachmentFileSize(file)
                                val detail = DyanmicFormSectionFieldDetailsDAO()
                                detail.name = file.name
                                detail.downloadUrl = response.body().downloadUrl
                                detail.mimeType = FileUtils.getMimeType(file)
                                detail.createdOn = Shared.getInstance().GetCurrentDateTime()
                                detail.fileSize = attachmentFileSize
                                field.details = detail


                                field.value = response.body().fileId


                                if (criteriaAdapter != null)
                                    fieldToBeUpdated?.updatePositionAttachment?.let { criteriaAdapter?.notifyOnly(it) }


                                for (i in dynamicStagesDAO?.criteriaList!!.indices) {
                                    val getList = dynamicStagesDAO?.criteriaList?.get(i);
                                    val formValues = getFormValues(getList!!)
                                    dynamicStagesDAO?.criteriaList?.get(i)?.formValues = formValues

                                    for (j in dynamicStagesDAO?.criteriaList?.get(i)?.formValues!!.indices) {
                                        val dynamicFormValuesDAO = dynamicStagesDAO?.criteriaList?.get(i)?.formValues?.get(j)
                                        if (dynamicFormValuesDAO?.sectionCustomFieldId == field.sectionCustomFieldId) {
                                            dynamicFormValuesDAO.value = field.value
                                        }
                                    }
                                }

                                if (field.isTigger) {
                                    callService()
                                }

                            } catch (e: Exception) {
                            }

                        }

                    } else {

                        Shared.getInstance().messageBox(getString(R.string.some_thing_went_wrong), bContext)
                    }

                }

                override fun onFailure(call: Call<ResponseFileUploadDAO>, t: Throwable) {
                    stop_loading_animation()
                    Shared.getInstance().showAlertMessage(getString(R.string.error), getString(R.string.some_thing_went_wrong), bContext)
                    // UploadFileInformation(fileDAO);
                }
            })

        } catch (ex: Exception) {
            stop_loading_animation()
            if (ex != null) {
                Shared.getInstance().showAlertMessage(getString(R.string.error), getString(R.string.some_thing_went_wrong), bContext)
                //UploadFileInformation(fileDAO);

            }
        }

    }//LoggedInUser end

    private fun start_loading_animation() {
        try {
            if (!pDialog!!.isShowing())
                pDialog?.show()
        } catch (e: java.lang.Exception) {
        }
    }

    private fun stop_loading_animation() {

        try {
            if (pDialog!!.isShowing())
                pDialog?.dismiss()
        } catch (e: java.lang.Exception) {
        }
    }

    fun SetUpLookUpValues(field: DynamicFormSectionFieldDAO, lookup: LookUpDAO) {

        field.value = lookup.id.toString()
        field.lookupValue = lookup.name
        field.id = lookup.id


        if (criteriaAdapter != null)
            fieldToBeUpdated?.updatePositionAttachment?.let { criteriaAdapter?.notifyOnly(it) }

        if (field.isTigger) {
            callService()
        }
    }

    companion object {

        var isGoBAck: Boolean = false


    }

    override fun onResume() {
        super.onResume()

        if (isGoBAck) {
            isGoBAck = false
            onBackPressed()
        } else
            registerReciever()
    }

    override fun onDestroy() {
        super.onDestroy()
        isGoBAck = false
    }

    override fun onPause() {
        super.onPause()
        unRegisterReciever()
    }


    /* var mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
         override fun onReceive(context: Context, intent: Intent) {
             //            CustomLogs.displayLogs(ACTIVITY_NAME + " BroadcastReceiver" + " " + intent.getStringExtra("position"));
             callService()
         }
     }

     private fun callService() {

         if (!isServiceRunning) {
             unRegisterReciever()
             isServiceRunning = true
             getCalculatedValues()


         }
     }
 */
    private fun registerReciever() {
//        isServiceRunning = false
//        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
//                IntentFilter("getcalculatedvalues"))
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
    }

    private fun unRegisterReciever() {
        //LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver)
        EventBus.getDefault().unregister(this)
    }

    fun getValuesForCalculatedValues(): DynamicResponseDAO? {
        for (i in dynamicStagesDAO?.criteriaList!!.indices) {
            val getList = dynamicStagesDAO?.criteriaList?.get(i);
            val formValues = getFormValues(getList!!)
            dynamicStagesDAO?.criteriaList?.get(i)?.formValues = formValues
        }

        actualResponseJsonsubmitJsonTemp = actualResponseJsonsubmitJson

        val criteriaList = dynamicStagesDAO?.criteriaList

        for (k in actualResponseJsonsubmitJsonTemp?.stages!!.indices) {

            val dynamicStagesDAO1 = actualResponseJsonsubmitJsonTemp?.stages?.get(k)
            if (dynamicStagesDAO1?.id == dynamicStagesDAO?.id) {

                for (p in criteriaList!!.indices) {

                    for (j in criteriaList.get(p).form.sections!!.indices) {
                        criteriaList.get(p).form.sections?.get(j)?.dynamicStagesCriteriaListDAO = null
                    }
                }


                actualResponseJsonsubmitJsonTemp?.stages?.get(k)?.criteriaList = criteriaList

            }

        }

        return actualResponseJsonsubmitJsonTemp

    }


    fun getCalculatedValues() {
        if (isKeyboardVisible)
            isCalculatedField = true

        try {
            // start_loading_animation()
            val valuesForCalculatedValues = getValuesForCalculatedValues()
            val calculatedSubmitCall = Shared.getInstance().retroFitObject(context).getCalculatedValues(valuesForCalculatedValues!!)
            calculatedSubmitCall.enqueue(object : Callback<List<CalculatedMappedFieldsDAO>> {
                override fun onResponse(call: Call<List<CalculatedMappedFieldsDAO>>, response: Response<List<CalculatedMappedFieldsDAO>>?) {

                    if (response != null && response.body() != null) {

                        val calculatedMappedFieldsList = response.body()

                        for (i in calculatedMappedFieldsList.indices) {
                            val calculatedMappedFieldsDAO = calculatedMappedFieldsList[i]
                            for (y in dynamicStagesDAO?.criteriaList!!.indices) {
                                val getList = dynamicStagesDAO?.criteriaList?.get(y);

                                val form = getList?.form
                                if (form?.sections != null) {
                                    for (sections in form.sections!!) {
                                        var sectionId = sections.id

                                        if (sections.fields != null) {
                                            for (t in sections.fields!!.indices) {
                                                val dynamicFormSectionFieldDAO = sections.fields?.get(t)

                                                if (dynamicFormSectionFieldDAO?.sectionCustomFieldId == calculatedMappedFieldsDAO.sectionCustomFieldId) {
                                                    val targetFieldType = calculatedMappedFieldsDAO.targetFieldType

                                                    if (targetFieldType == 13) {
                                                        val servicelookupItems = calculatedMappedFieldsDAO.lookupItems
                                                        //  if (dynamicFormSectionFieldDAO.lookupValue != null && !dynamicFormSectionFieldDAO.lookupValue!!.isEmpty()) {

                                                        try {
                                                            val servicelookupItemsTemp = ArrayList<String>()
                                                            if (servicelookupItems != null) {
                                                                for (s in servicelookupItems.indices) {
                                                                    servicelookupItemsTemp.add(servicelookupItems.get(s).name!!)
                                                                }
                                                                if (!servicelookupItemsTemp.contains(dynamicFormSectionFieldDAO.lookupValue!!))
                                                                    dynamicFormSectionFieldDAO.lookupValue = ""
                                                            }
                                                        } catch (e: java.lang.Exception) {
                                                        }

                                                        //    }
                                                        calculatedMappedFieldsDAO.lookupItems = servicelookupItems
                                                        Shared.getInstance().saveLookUpItems(calculatedMappedFieldsDAO.sectionCustomFieldId, servicelookupItems)
                                                    }

                                                    if (targetFieldType == 7 || targetFieldType == 15) {

                                                        dynamicFormSectionFieldDAO.isMappedCalculatedField = true
                                                        dynamicFormSectionFieldDAO.type = calculatedMappedFieldsDAO.targetFieldType
                                                        dynamicFormSectionFieldDAO.value = calculatedMappedFieldsDAO.value
                                                        var attachmentFileSize = ""
                                                        if (calculatedMappedFieldsDAO.details != null) {
                                                            val getOutputMediaFile = Shared.getInstance().getOutputMediaFile(calculatedMappedFieldsDAO.details!!.name)!!.path
                                                            val isFileExist = Shared.getInstance().isFileExist(getOutputMediaFile, bContext)
                                                            if (isFileExist) {
                                                                var file: File? = null
                                                                file = File(getOutputMediaFile)
                                                                attachmentFileSize = Shared.getInstance().getAttachmentFileSize(file)
                                                            }

                                                            calculatedMappedFieldsDAO.details!!.fileSize = attachmentFileSize
                                                        }
                                                        dynamicFormSectionFieldDAO.details = calculatedMappedFieldsDAO.details
                                                    } else if (targetFieldType == 4) {
                                                        val calculatedDisplayDate = Shared.getInstance().getDisplayDate(bContext, calculatedMappedFieldsDAO.value, false)
                                                        dynamicFormSectionFieldDAO.value = calculatedDisplayDate


                                                    } else if (targetFieldType == 11) {
                                                        val fieldDAO = Shared.getInstance().populateCurrency(calculatedMappedFieldsDAO.value)
                                                        val concateValue = fieldDAO.value + " " + fieldDAO.selectedCurrencySymbol
                                                        dynamicFormSectionFieldDAO.value = concateValue
                                                        if (!fieldDAO.value.isNullOrEmpty() && !fieldDAO.selectedCurrencySymbol.isNullOrEmpty())
                                                            calculatedMappedFieldsDAO.value = concateValue

                                                    } else
                                                        dynamicFormSectionFieldDAO.value = calculatedMappedFieldsDAO.value


                                                    //  if (!dynamicFormSectionFieldDAO.value.isNullOrEmpty() || dynamicFormSectionFieldDAO.hasValue) {
                                                    //       dynamicFormSectionFieldDAO.hasValue = !dynamicFormSectionFieldDAO.hasValue
                                                    //         criteriaAdapter?.notifyItemChanged(t)
                                                    //    }

                                                }


                                            }
                                        }

                                    }
                                }


                            }
                            //  }

                            for (j in dynamicStagesDAO?.criteriaList!!.indices) {
                                val dynamicStagesCriteriaListDAO = dynamicStagesDAO?.criteriaList?.get(j)
                                for (s in dynamicStagesCriteriaListDAO?.formValues!!.indices) {
                                    val dynamicFormValuesDAO = dynamicStagesCriteriaListDAO.formValues.get(s)
                                    val sectionCustomFieldId = dynamicFormValuesDAO.sectionCustomFieldId
                                    if (calculatedMappedFieldsDAO.sectionCustomFieldId == sectionCustomFieldId) {
                                        dynamicFormValuesDAO.value = calculatedMappedFieldsDAO.value
                                    }
                                }

                            }


                        }


                        criteriaAdapter?.notifyDataSetChanged()


                        val handler = Handler()
                        handler.postDelayed({
                            registerReciever()
                            // stop_loading_animation()
                        }, 1000)

                        if (ChooseLookUpOption.isOpen)
                            EventBus.getDefault().post(EventOptions.EventTriggerController())
                    } else {
                        // stop_loading_animation()
                        CustomLogs.displayLogs(TAG + " null response")
                        registerReciever()
                    }


                }

                override fun onFailure(call: Call<List<CalculatedMappedFieldsDAO>>, t: Throwable) {
                    t.printStackTrace()
                    //  stop_loading_animation()
                    CustomLogs.displayLogs(TAG + " failure response")
                    registerReciever()
                }
            })

        } catch (ex: Exception) {
            ex.printStackTrace()
            //   stop_loading_animation()
            registerReciever()
        }

    }//LoggedInUser end


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun dataRefreshEvent(eventTriggerController: EventOptions.EventTriggerController) {
        callService()

    }

    private fun callService() {
        unRegisterReciever()
        val handler = Handler()
        handler.postDelayed({
            getCalculatedValues()
        }, 1000)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                    if (isCalculatedField) {
                        isCalculatedField = false
                        val handler = Handler()
                        handler.postDelayed({
                            if (isCalculatedField)
                                criteriaAdapter?.notifyDataSetChanged()
                        }, 500)
                    }
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    private fun setGravity() {
        if (pref!!.language.equals("ar", ignoreCase = true)) {
            txtStagename.gravity = Gravity.RIGHT
            conditiontext.gravity =(Gravity.RIGHT)
            conditiontextvalue.gravity =(Gravity.RIGHT)
            sequencetext.gravity =(Gravity.RIGHT)
            sequencetextvalue.gravity =(Gravity.RIGHT)
            acceptencetext.gravity =(Gravity.RIGHT)
            acceptencetextvalue.gravity =(Gravity.RIGHT)
            txtcriteria.gravity =(Gravity.RIGHT)
        } else {
            txtStagename.gravity =(Gravity.LEFT)
            conditiontext.gravity =(Gravity.LEFT)
            conditiontextvalue.gravity =(Gravity.LEFT)
            sequencetext.gravity =(Gravity.LEFT)
            sequencetextvalue.gravity =(Gravity.LEFT)
            acceptencetext.gravity =(Gravity.LEFT)
            acceptencetextvalue.gravity =(Gravity.LEFT)
            txtcriteria.gravity =(Gravity.LEFT)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (ApplicationDetailScreenActivity.criteriaWasLoaded)
            ApplicationDetailScreenActivity.criteriaWasLoaded = false
    }

}
