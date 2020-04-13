package com.esp.library.exceedersesp.controllers.Profile


import android.Manifest.permission.*
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.provider.MediaStore
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.esp.library.R
import com.esp.library.exceedersesp.ESPApplication
import com.esp.library.exceedersesp.controllers.WebViewScreenActivity
import com.esp.library.exceedersesp.controllers.applications.ApplicationsActivityDrawer
import com.esp.library.utilities.common.Constants
import com.esp.library.utilities.common.CustomLogs
import com.esp.library.utilities.common.Shared
import com.esp.library.utilities.common.SharedPreference
import com.google.gson.GsonBuilder
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_profile_image.view.*
import kotlinx.android.synthetic.main.fragment_profile_image.view.idenedi_login_btn
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import utilities.adapters.setup.applications.SectionListAdapter
import utilities.data.apis.APIs
import utilities.data.applicants.dynamics.DyanmicFormSectionFieldDetailsDAO
import utilities.data.applicants.dynamics.DynamicFormSectionDAO
import utilities.data.applicants.dynamics.DynamicFormSectionFieldDAO
import utilities.data.applicants.dynamics.DynamicFormSectionFieldsCardsDAO
import utilities.data.applicants.profile.ApplicationProfileDAO
import utilities.interfaces.Itemclick
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class FragmentProfileImage : androidx.fragment.app.Fragment(), Itemclick {

    internal var TAG = javaClass.simpleName
    internal var bundle: Bundle? = null
    internal var profile_image: CircleImageView? = null
    private var mApplicationLayoutManager: androidx.recyclerview.widget.RecyclerView.LayoutManager? = null
    internal var adapter: SectionListAdapter? = null
    internal var context: Context? = null
    internal var dataapplicant: ApplicationProfileDAO? = null
    internal var ischeckerror = false
    internal var file: Uri? = null
    internal var pictureFilePath: String? = null
    internal var pDialog: AlertDialog? = null
    internal var idenedi_login_btn: Button? = null
    internal var etxtidenediID: TextView? = null
    internal var rlidenedikey: RelativeLayout? = null
    val PIC_CROP = 3
    internal var pref: SharedPreference? = null

    val outputMediaFile: File?
        get() {
            val mediaStorageDir = File(Environment.getExternalStorageDirectory().path + "/ESP/")

            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    return null
                }
            }

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())


            return File(mediaStorageDir.path + File.separator +
                    timeStamp + ".jpg")
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_profile_image, container, false)
        initailize(view)
        if (Build.VERSION.SDK_INT >= 24) {
            val builder = StrictMode.VmPolicy.Builder()
            StrictMode.setVmPolicy(builder.build())
        }// for camera above API 24
        if (!checkPermission()) {
            requestPermission()
        }

        bundle = arguments
        ischeckerror = bundle!!.getBoolean("ischeckerror")

        if (ischeckerror)
            view.ivback.visibility = View.VISIBLE
        else
            view.ivback.visibility = View.GONE

        updateTop(view)
        populateData(view)


        view.profile_image.setOnClickListener { showPictureDialog() }
        view.ivback.setOnClickListener {
            val intent = Intent(context, ApplicationsActivityDrawer::class.java)
            val cn = intent.getComponent()
            val mainIntent = Intent.makeRestartActivityTask(cn)
            startActivity(mainIntent)

            // activity?.finish()
        }



        view.idenedi_login_btn.setOnClickListener {
            val bn = Bundle()
            bn.putString("heading", getString(R.string.logineithidenedi))

               bn.putString("url", "https://app.idenedi.com/app_permission/?response_type=code&client_id=" + pref?.getidenediClientId() + "&redirect_uri=https://isp.exceedgulf.com/login")
             //  bn.putString("url", "https://app.idenedi.com/app_permission/?response_type=code&client_id=" + pref?.getidenediClientId() + "&redirect_uri=https://esp.exceeders.com/login")
          //  bn.putString("url", "https://idenedi-prod-stag.azurewebsites.net/app_permission/?response_type=code&client_id=" + pref?.getidenediClientId() + "&redirect_uri=https://qaesp.azurewebsites.net/login")
            bn.putBoolean("isIdenedi", true)
            Shared.getInstance().callIntent(WebViewScreenActivity::class.java, context as Activity?, bn)
        }

        if (pref?.getidenediClientId().isNullOrEmpty()) {
            view.idenedi_login_btn.visibility = View.GONE
        } else if (dataapplicant?.applicant?.idenediKey.isNullOrEmpty())
            view.idenedi_login_btn.visibility = View.VISIBLE
        else
            setIdenediKey(dataapplicant?.applicant?.idenediKey!!)




        return view

    }

    private fun initailize(view: View) {
        context = activity
        pDialog = Shared.getInstance().setProgressDialog(context)
        idenedi_login_btn=view.findViewById(R.id.idenedi_login_btn)
        etxtidenediID=view.findViewById(R.id.etxtidenediID)
        rlidenedikey=view.findViewById(R.id.rlidenedikey)
        pref = SharedPreference(context)
        profile_image = view.findViewById(R.id.profile_image)
        mApplicationLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
        view.app_list_sections.setHasFixedSize(true)
        view.app_list_sections.layoutManager = mApplicationLayoutManager
        view.app_list_sections.itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()
    }

    private fun setIdenediKey(idenediKey: String) {

        val reasonTextConcate = context?.getString(R.string.idenediid) + " " + idenediKey
        val wordtoSpan: Spannable = SpannableString(reasonTextConcate)
        wordtoSpan.setSpan(ForegroundColorSpan(ContextCompat.getColor(context!!, R.color.black)), 0, 11, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        wordtoSpan.setSpan(ForegroundColorSpan(ContextCompat.getColor(context!!, R.color.coolgrey)), 12, reasonTextConcate.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);


        idenedi_login_btn?.visibility = View.GONE
        rlidenedikey?.visibility = View.VISIBLE
        etxtidenediID?.text = wordtoSpan
    }

    private fun updateTop(view: View) {

        try {
            dataapplicant = bundle!!.getSerializable("dataapplicant") as ApplicationProfileDAO
            val dataApplicantTemp = dataapplicant
            view.etxtusername.text = dataApplicantTemp?.applicant?.name
            view.etxtemail.text = dataApplicantTemp?.applicant?.emailAddress
            if (dataApplicantTemp?.applicant?.imageUrl?.replace("\\s".toRegex(), "")?.length!! > 0)
                profile_image?.let {
                    Glide.with(activity!!).load(dataApplicantTemp.applicant.imageUrl)
                            .error(R.drawable.default_profile_picture)
                            .into(it)
                }
        } catch (e: java.lang.Exception) {
        }
    }

    private fun populateData(view: View) {
        val sectionstemp = ArrayList<DynamicFormSectionDAO>()
        val applicantSections = dataapplicant?.applicant?.applicantSections
        val sections = dataapplicant!!.sections
        for (i in sections.indices) {
            val section = sections[i]

            if (section.type == 1 || section.type == 2) {

                val id = section.id
                val cardsList = ArrayList<DynamicFormSectionFieldsCardsDAO>()
                for (applicationsection in applicantSections!!) {
                    val sectionId = applicationsection.sectionId
                    if (id == sectionId) {
                        val displayDate = Shared.getInstance().getDisplayDate(context, applicationsection.lastUpdatedOn, true)
                        section.lastUpdatedOn = displayDate
                        val finalFields = ArrayList<DynamicFormSectionFieldDAO>()
                        val values = applicationsection.values
                        val fields = section.fields

                        for (o in fields!!.indices) {
                            val parentSectionField = fields[o]
                            val tempField = Shared.getInstance().setObjectValues(parentSectionField)
                            if (tempField.isVisible) {
                                finalFields.add(tempField)
                            }
                        }

                        for (j in 0 until finalFields.size) {
                            val getFinalFields = finalFields.get(j)
                            val sectionCustomFieldId = getFinalFields.sectionCustomFieldId
                            for (k in values!!.indices) {
                                val getVal = values[k]
                                var getValue: String? = getVal.value
                                val getSectionFieldId = getVal.sectionFieldId

                                if (getSectionFieldId == sectionCustomFieldId) {
                                    getFinalFields.value = getValue
                                    if (getFinalFields.type == 13)
                                    // lookupvalue
                                    {
                                        getValue = getVal.lookupValue
                                        if (getValue == null) getValue = getVal.value
                                        getFinalFields.lookupValue = getValue
                                        if (getVal.value != null && Shared.getInstance().isNumeric(getVal.value)) getFinalFields.id = getVal.value!!.toInt()
                                        getFinalFields.value = getValue
                                    } else if (getFinalFields.type == 7) { // for attachments only
                                        try {
                                            val details = DyanmicFormSectionFieldDetailsDAO()
                                            details.downloadUrl = getVal.details?.downloadUrl
                                            details.mimeType = getVal.details?.mimeType
                                            details.createdOn = getVal.details?.createdOn
                                            details.name = getVal.details?.name
                                            getFinalFields.details = details
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }

                                    } else if (getFinalFields.type == 19 || getFinalFields.type == 18) {
                                        if (getVal.type == 11) {
                                            val fieldDAO = Shared.getInstance().populateCurrency(getValue)
                                            val concateValue = fieldDAO.value + " " + fieldDAO.selectedCurrencySymbol
                                            getFinalFields.value = concateValue
                                        } else if (getVal.type == 7) { // for attachments only
                                            try {
                                                getFinalFields.type = getVal.type
                                                val details = DyanmicFormSectionFieldDetailsDAO()
                                                details.downloadUrl = getVal.details?.downloadUrl
                                                details.mimeType = getVal.details?.mimeType
                                                details.createdOn = getVal.details?.createdOn
                                                details.name = getVal.details?.name
                                                getFinalFields.details = details
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }

                                        } else if (getVal.type == 13)
                                        // lookupvalue
                                        {

                                            getValue = getVal.lookupValue
                                            if (getValue == null)
                                                getValue = getVal.value

                                            getFinalFields.lookupValue = getValue

                                            //if (getVal.value != null && getVal.value.isNullOrEmpty())
                                            if (getVal.value != null && Shared.getInstance().isNumeric(getVal.value))
                                                getFinalFields.id = Integer.parseInt(getVal.value!!)
                                        }
                                    }


                                }

                            }


                        }

                        if (finalFields.size == 0) {

                            for (f in 0 until section.fields!!.size) {
                                if (section.fields!![f].isVisible)
                                    finalFields.add(section.fields!![f])
                            }
                        }



                        cardsList.add(DynamicFormSectionFieldsCardsDAO(finalFields))



                        if (ischeckerror) {
                            if (values != null) {
                                //  for (f in 0 until section.fields!!.size) {
                                // if ((section.fields!![f].isVisible && section.fields!![f].isRequired)
                                section.isShowError = values.size == 0
                                // }
                            }
                        }


                    }

                }
                section.setRefreshFieldsCardsList(cardsList)

                if (section.fields!!.size == 0 && section.isDefault) {
                    section.isShowError = false
                    sectionstemp.add(section)

                }

                for (h in section.getFieldsCardsList().indices) {
                    for (p in section.getFieldsCardsList()[h].fields!!.indices) {
                        if (section.getFieldsCardsList()[h].fields!![p].isVisible && section.getFieldsCardsList()[h].fields!![p].isRequired
                                && (section.getFieldsCardsList()[h].fields!![p].value == null || TextUtils.isEmpty(section.getFieldsCardsList()[h].fields!![p].value))
                                && section.type == 1 && ischeckerror) {
                            section.isShowError = true
                            break
                        } else
                            section.isShowError = false
                    }
                    sectionstemp.add(section)
                }


            }
        }


        val sectionsFinalArray = ArrayList<DynamicFormSectionDAO>()
        for (i in 0 until sectionstemp.size) {
            val isArrayHasValue = sectionsFinalArray.any { x -> x.id == sectionstemp[i].id }
            if (!isArrayHasValue)
                sectionsFinalArray.add(sectionstemp[i])
        }




        adapter = context?.let { SectionListAdapter(sectionsFinalArray, it, this) }
        view.app_list_sections.adapter = adapter
    }



    private fun choosePhotoFromGallary() {
        val getContentIntent = Intent(Intent.ACTION_GET_CONTENT)
        getContentIntent.type = "image/*"
        getContentIntent.addCategory(Intent.CATEGORY_OPENABLE)
        val intent = Intent.createChooser(getContentIntent, getString(R.string.selectafile))
        startActivityForResult(intent, GALLERY_REQUEST)
    }

    private fun openCameraIntent() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        file = Uri.fromFile(outputMediaFile)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, file)
        startActivityForResult(intent, CAMERA_REQUEST)
    }

    private fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(context)
        pictureDialog.setTitle(getString(R.string.choose))
        val pictureDialogItems = arrayOf(getString(R.string.camera), getString(R.string.gallery))
        pictureDialog.setItems(pictureDialogItems
        ) { _, which ->
            when (which) {
                0 -> openCameraIntent()
                1 -> choosePhotoFromGallary()
            }
        }
        pictureDialog.show()
    }

    private fun performCrop(picUri: Uri) {
        //call the standard crop action intent (the user device may not support it)
        val cropIntent = Intent("com.android.camera.action.CROP")
//indicate image type and Uri
        cropIntent.setDataAndType(picUri, "image/*")
//set crop properties
        cropIntent.putExtra("crop", "true")
//indicate aspect of desired crop
        cropIntent.putExtra("aspectX", 1)
        cropIntent.putExtra("aspectY", 1)
//indicate output X and Y
        cropIntent.putExtra("outputX", 256)
        cropIntent.putExtra("outputY", 256)
//retrieve data on return
        cropIntent.putExtra("return-data", true)
//start the activity - we handle returning in onActivityResult
        startActivityForResult(cropIntent, PIC_CROP)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {

            if (requestCode == GALLERY_REQUEST && data != null) {

                val uri = data.data

                if (uri != null) {
                    /*    val path = RealPathUtil.getPath(context, uri)
                        CustomLogs.displayLogs("$TAG imageuri: $path")*/

                    UpdateLoadImageForField(uri, context)

                }


            } else if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
                pictureFilePath = file?.path
                val f = File(pictureFilePath)
                pictureFilePath = decodeFile(f)
                val bitmap = createBitmap(pictureFilePath)


                val rotateImage = rotateImage(bitmap, 90f)

                val imageUri = getImageUri(context, rotateImage)
                performCrop(imageUri!!)

                //UpdateLoadImageForField(imageUri, context)

            } else if (requestCode == PIC_CROP) {
                val uri = data?.data

                if (uri != null) {
                    //   val path = RealPathUtil.getPath(context, uri)
                    UpdateLoadImageForField(uri, context)
                }

            }
        }

    }

    fun rotateImage(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height,
                matrix, true)
    }

    fun getImageUri(inContext: Context?, inImage: Bitmap): Uri? {
        try {
            val bytes = ByteArrayOutputStream()
            inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
            val path = MediaStore.Images.Media.insertImage(inContext!!.contentResolver, inImage, "Title", null)
            return Uri.parse(path)
        } catch (e: Exception) {
            e.printStackTrace()
            CustomLogs.displayLogs(TAG + " getImageUri Exception: " + e.message)
        }

        return null
    }

    private fun createBitmap(path: String?): Bitmap {
        return BitmapFactory.decodeFile(path)
    }

    fun decodeFile(f: File): String? {
        var b: Bitmap? = null
        try {
            // Decode image size
            val o = BitmapFactory.Options()
            o.inJustDecodeBounds = true

            var fis = FileInputStream(f)
            try {
                BitmapFactory.decodeStream(fis, null, o)
            } finally {
                fis.close()
            }

            var scale = 1
            val size = Math.max(o.outHeight, o.outWidth)
            while (size shr scale - 1 > 800) {
                ++scale
            }

            // Decode with inSampleSize
            val o2 = BitmapFactory.Options()
            o2.inSampleSize = scale
            fis = FileInputStream(f)
            try {
                b = BitmapFactory.decodeStream(fis, null, o2)
            } finally {
                fis.close()
            }
        } catch (e: IOException) {
        }

        return persistImage(b, f.path)
    }

    fun persistImage(bitmap: Bitmap?, fpath: String): String? {


        val fnewpath = File(fpath)
        val os: OutputStream
        try {
            os = FileOutputStream(fnewpath)
            bitmap!!.compress(Bitmap.CompressFormat.JPEG, 50, os)
            os.flush()
            os.close()
        } catch (e: Exception) {
            e.printStackTrace()
            CustomLogs.displayLogs("$TAG persistImage Error writing bitmap")
            return null
        }

        return fnewpath.path
    }

    fun UpdateLoadImageForField(uri: Uri?, context: Context?) {

        try {
            var body = Shared.getInstance().prepareFilePart(uri, context)
            UpLoadFile(body)

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    private fun UpLoadFile(body: MultipartBody.Part?) {
        start_loading_animation()
        try {
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY
            val httpClient = OkHttpClient.Builder()
            httpClient.addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                val request = requestBuilder.build()
                chain.proceed(request)
            }
            if (Constants.WRITE_LOG) {
                httpClient.addInterceptor(logging)
            }
            httpClient.addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                        .header("locale", Shared.getInstance().getLanguageSimpleContext(activity))
                        .header("Authorization", "bearer " + ESPApplication.getInstance().user.loginResponse?.access_token)
                val request = requestBuilder.build()
                chain.proceed(request)
            }


            httpClient.connectTimeout(5, TimeUnit.MINUTES)
            httpClient.readTimeout(5, TimeUnit.MINUTES)
            httpClient.writeTimeout(5, TimeUnit.MINUTES)

            /* retrofit builder and call web service*/
            val retrofit = Retrofit.Builder()
                    .baseUrl(Constants.base_url)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build()

            /* APIs Mapping respective Object*/
            val apis = retrofit.create(APIs::class.java)

            val call_upload = apis.picture(body)
            call_upload.enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>?) {
                    if (response != null && response.body() != null) {

                        stop_loading_animation()

                        CustomLogs.displayLogs(TAG + " response.body(): " + response.body())
                        profile_image?.let {
                            Glide.with(context!!).load(response.body()).placeholder(R.drawable.default_profile_picture)
                                    .error(R.drawable.default_profile_picture).into(it)
                        }


                    } else {
                        stop_loading_animation()
                        Shared.getInstance().showAlertMessage(getString(R.string.error), getString(R.string.pleasetryagain), context)
                    }

                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    stop_loading_animation()
                    Shared.getInstance().showAlertMessage(getString(R.string.error), getString(R.string.some_thing_went_wrong), context)
                    // UploadFileInformation(fileDAO);
                }
            })

        } catch (ex: Exception) {
            stop_loading_animation()
            ex.printStackTrace()
            Shared.getInstance().showAlertMessage(getString(R.string.error), getString(R.string.some_thing_went_wrong), context)
        }

    }//LoggedInUser end

    private fun checkPermission(): Boolean {

        val permissionCamera = ContextCompat.checkSelfPermission(context!!, CAMERA)
        val permissionReadStorage = ContextCompat.checkSelfPermission(context!!, READ_EXTERNAL_STORAGE)
        val permissionWriteStorage = ContextCompat.checkSelfPermission(context!!, WRITE_EXTERNAL_STORAGE)

        val listPermissionsNeeded = ArrayList<String>()

        if (permissionCamera != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(CAMERA)
        }
        if (permissionReadStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(READ_EXTERNAL_STORAGE)
        }

        if (permissionWriteStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(WRITE_EXTERNAL_STORAGE)
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(activity!!, listPermissionsNeeded.toTypedArray(), 1122)
            return false
        }
        return true
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(activity!!, arrayOf(CAMERA, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.size > 0) {
                val locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                if (locationAccepted) {
                    //"Permission Granted, Now you can access location data."
                    //    openCameraIntent();
                    try {
                        Shared.getInstance().createFolder(Constants.FOLDER_PATH, Constants.FOLDER_NAME, context)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
            }
        }

    }

    private fun sendIdendiCode() {

        start_loading_animation()


        try {
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY
            val httpClient = OkHttpClient.Builder()
            httpClient.addInterceptor(logging)

            httpClient.addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                //   .header("Content-Type ", "application/x-www-form-urlencoded")
                val request = requestBuilder.build()
                chain.proceed(request)
            }



            httpClient.connectTimeout(10, TimeUnit.SECONDS)
            httpClient.readTimeout(10, TimeUnit.SECONDS)
            httpClient.writeTimeout(10, TimeUnit.SECONDS)

            val gson = GsonBuilder()
                    .setLenient()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                    .create()

            val retrofit = Retrofit.Builder()
                    .baseUrl(Constants.base_url)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(httpClient.build())

                    .build()


            val apis = Shared.getInstance().retroFitObject(context)
            // var call_idenediToken = apis.getIdenediToken()

            var call_idenediToken = apis.linkIdenediProfile(pref?.idenediCode!!)


            call_idenediToken.enqueue(object : Callback<Any> {
                override fun onResponse(call: Call<Any>?, response: Response<Any>?) {

                    stop_loading_animation()


                    pref?.saveIdenediCode(null)
                    if (response?.body() != null) {

                        CustomLogs.displayLogs(TAG + " response?.body(): " + response.body())
                        setIdenediKey(response.body() as String)
                    } else
                        Shared.getInstance().messageBox(getString(R.string.some_thing_went_wrong), context as Activity?)
                }


                override fun onFailure(call: Call<Any>, t: Throwable) {
                    pref?.saveIdenediCode(null)
                    stop_loading_animation()
                    Shared.getInstance().messageBox(getString(R.string.some_thing_went_wrong), context as Activity?)

                }
            })

        } catch (ex: Exception) {
            pref?.saveIdenediCode(null)
            ex.printStackTrace()
            stop_loading_animation()
            Shared.getInstance().messageBox(getString(R.string.some_thing_went_wrong), context as Activity?)

        }

    }

    override fun itemclick(dynamicFormSectionDAO: DynamicFormSectionDAO) {

        val i = Intent(context, SectionDetailScreen::class.java)
        i.putExtra("data", dynamicFormSectionDAO)
        i.putExtra("dataapplicant", dataapplicant)
        i.putExtra("ischeckerror", ischeckerror)
        i.putExtra("isprofile", bundle!!.getBoolean("isprofile"))
        startActivity(i)
    }

    companion object {
        private val CAMERA_REQUEST = 1
        private val GALLERY_REQUEST = 2
        private val PERMISSION_REQUEST_CODE = 200
    }

    private fun start_loading_animation() {
        if (!pDialog!!.isShowing())
            pDialog!!.show()
    }

    private fun stop_loading_animation() {
        if (pDialog!!.isShowing())
            pDialog!!.dismiss()
    }

    override fun onResume() {
        super.onResume()

        if (!pref?.idenediCode.isNullOrEmpty())
            sendIdendiCode()

    }

}
