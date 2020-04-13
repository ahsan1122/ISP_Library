package com.esp.library.exceedersesp.controllers.applications

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.esp.library.R
import com.esp.library.exceedersesp.fragments.applications.AddApplicationFragment
import com.esp.library.ipaulpro.afilechooser.utils.FileUtils
import com.esp.library.utilities.common.*
import com.esp.library.utilities.setup.applications.ApplicationFieldsRecyclerAdapter
import com.esp.library.utilities.setup.applications.ListAddApplicationAdapter
import com.esp.library.exceedersesp.BaseActivity
import kotlinx.android.synthetic.main.activity_add_applications_form.*
import kotlinx.android.synthetic.main.custom_alert_view.view.*
import org.greenrobot.eventbus.EventBus
import utilities.data.applicants.addapplication.CategoryAndDefinationsDAO
import utilities.data.applicants.dynamics.DynamicFormSectionFieldDAO


class AddApplicationsFromScreenActivity : BaseActivity(), ListAddApplicationAdapter.CategorySelection,
        AlertActionWindow.ActionInterface {


    internal var TAG = javaClass.simpleName
    internal var context: BaseActivity? = null
    internal var fm: androidx.fragment.app.FragmentManager? = null
    internal var submit_request: AddApplicationFragment? = null
    internal var definationsDAO: CategoryAndDefinationsDAO? = null
    internal var imm: InputMethodManager? = null
    internal var upload_file: DynamicFormSectionFieldDAO? = null
    internal var pref: SharedPreference? = null

    override fun StatusChange(update: DynamicFormSectionFieldDAO) {
        upload_file = update

        val getContentIntent = FileUtils.createGetContentIntent()
        val intent = Intent.createChooser(getContentIntent, getString(R.string.selectafile))
        startActivityForResult(intent, REQUEST_CHOOSER)
    }

    override fun SingleSelection(update: DynamicFormSectionFieldDAO) {

        if (submit_request != null) {
            submit_request!!.SingleSelection(update)
        }
    }

    override fun LookUp(update: DynamicFormSectionFieldDAO) {

        upload_file = update

        val bundle = Bundle()
        bundle.putSerializable(DynamicFormSectionFieldDAO.BUNDLE_KEY, update)
        Shared.getInstance().callIntentWithResult(ChooseLookUpOption::class.java, context, bundle, 2)

    }

    override fun mActionTo(whattodo: String) {
        if (whattodo == getString(R.string.draft)) {
            if (submit_request != null) {
                submit_request!!.SubmitRequest(getString(R.string.draft))
            }
        } else {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_applications_form)
        initialize()
        setGravity()
        val bnd = intent.extras
        //  var appStatus: String? = null
        if (bnd != null) {
            definationsDAO = bnd.getSerializable(CategoryAndDefinationsDAO.BUNDLE_KEY) as CategoryAndDefinationsDAO
            /*if (definationsDAO != null)
                appStatus = bnd.getString("appStatus")*/
            categorytitle.text = definationsDAO?.name
            if (definationsDAO?.description.isNullOrEmpty()) {
                definitionDescription.text = definationsDAO?.parentApplicationInfo?.descriptionFieldValue
                definitionNameTitle.text = definationsDAO?.parentApplicationInfo?.titleFieldValue
            } else {
                definitionDescription.text = definationsDAO?.description
                definitionNameTitle.text = definationsDAO?.name
            }
        }



        definitionDescription.setOnClickListener {

            if (definitionDescription.maxLines == 3)
                definitionDescription.maxLines = 50
            else
                definitionDescription.maxLines = 3

        }
        CustomLogs.displayLogs("$TAG appStatus ")


        fm = this.context?.getSupportFragmentManager()
        if (definationsDAO != null)
            submit_request = AddApplicationFragment.newInstance(definationsDAO, submit_btn)

        val ft = fm!!.beginTransaction()
        ft.add(R.id.request_fragment, submit_request!!)
        ft.commit()

        submit_btn.setOnClickListener {
            if (submit_request != null) {
                val curr_view = currentFocus
                if (curr_view != null) {
                    imm!!.hideSoftInputFromWindow(curr_view.windowToken, 0)
                }

                submit_request!!.SubmitRequest(getString(R.string.submit))
            }
        }

        KeyboardUtils.addKeyboardToggleListener(this,
                object : KeyboardUtils.SoftKeyboardToggleListener {
                    override fun onToggleSoftKeyboard(isVisible: Boolean) {
                        submit_request?.refreshAdapter(isVisible)
                    }
                })

    }

    private fun initialize() {
        context = this@AddApplicationsFromScreenActivity
        pref = SharedPreference(context)
        submit_btn.isEnabled = false
        submit_btn.alpha = 0.5f
        definitionName.text = getString(R.string.add) + " " + pref?.getlabels()?.application

        imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        setSupportActionBar(toolbar)
        supportActionBar?.setTitle("")
        supportActionBar?.setDisplayShowHomeEnabled(true)

        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_nav_close)
        toolbar.navigationIcon?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        toolbar.setNavigationOnClickListener {
            val view = currentFocus
            if (view != null) {
                imm!!.hideSoftInputFromWindow(view.windowToken, 0)
            }
            val action_window = AlertActionWindow.newInstance(getString(R.string.save_draft), getString(R.string.your) + " " + pref?.getlabels()?.application + " " + getString(R.string.wasnotsubmitted), getString(R.string.save_draft_ok), getString(R.string.discard) + " " + pref?.getlabels()?.application, getString(R.string.draft))
            action_window.show(supportFragmentManager, "")
            action_window.isCancelable = true
        }
    }


    override fun onBackPressed() {

        val action_window = AlertActionWindow.newInstance(getString(R.string.save_draft), getString(R.string.your) + " " + pref?.getlabels()?.application + " " + getString(R.string.wasnotsubmitted), getString(R.string.save_draft_ok), getString(R.string.discard) + " " + pref?.getlabels()?.application, getString(R.string.draft))
        action_window.show(supportFragmentManager, "")
        action_window.isCancelable = true
    }

    private fun setGravity() {
        if (pref?.language.equals("ar", ignoreCase = true)) {
            definitionName.gravity = Gravity.RIGHT

        } else {
            definitionName.gravity = Gravity.LEFT
        }
    }

    companion object {
        var ACTIVITY_NAME = "controllers.applications.AddApplicationsFromScreenActivity"
        private val REQUEST_CHOOSER = 1234
    }


    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0)

                    if (AddApplicationFragment.isCalculatedField) {
                        AddApplicationFragment.isCalculatedField = false
                        val handler = Handler()
                        handler.postDelayed({
                            submit_request?.mApplicationSectionsAdapter?.notifyDataSetChanged()
                        }, 500)
                    }


                }
            }
        }

        return super.dispatchTouchEvent(event)
    }


    override fun onDestroy() {
        super.onDestroy()
        ApplicationFieldsRecyclerAdapter.isCalculatedMappedField = false
        AddApplicationFragment.isCalculatedField = false
    }


}
