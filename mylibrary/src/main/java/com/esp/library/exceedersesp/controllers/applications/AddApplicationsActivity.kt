package com.esp.library.exceedersesp.controllers.applications

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Rect
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.view.Gravity
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.esp.library.R
import com.esp.library.exceedersesp.fragments.applications.AddApplicationCategoryAndDefinationsFragment
import com.esp.library.utilities.common.SharedPreference
import com.esp.library.exceedersesp.BaseActivity
import kotlinx.android.synthetic.main.activity_add_applications.*
import utilities.adapters.setup.applications.ListApplicationCategoryAndDefinationAdapter
import utilities.data.applicants.addapplication.CategoryAndDefinationsDAO


class AddApplicationsActivity : BaseActivity(), ListApplicationCategoryAndDefinationAdapter.CategorySelection {


    internal var context: BaseActivity? = null
    internal var fm: androidx.fragment.app.FragmentManager? = null
    internal var submit_request: AddApplicationCategoryAndDefinationsFragment? = null
    internal var imm: InputMethodManager? = null


    override fun StatusChange(update: CategoryAndDefinationsDAO) {
        if (submit_request != null) {
            submit_request!!.UpdateDefincation(update)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_applications)
        initailize()
        setGravity()

        val ft = fm!!.beginTransaction()
        ft.add(R.id.request_fragment, submit_request!!)
        ft.commit()


    }

    private fun initailize() {
        context = this@AddApplicationsActivity
        imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        setSupportActionBar(toolbar)
        supportActionBar?.setTitle("")
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_nav_back)
        toolbar.navigationIcon?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        fm = getSupportFragmentManager()
        submit_request = AddApplicationCategoryAndDefinationsFragment.newInstance()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == 2) {

            if (data != null) {
                val bnd = data.extras

                if (bnd != null) {
                    if (bnd.getBoolean("whatodo")) {
                        val bnd_ = Bundle()
                        bnd_.putBoolean("whatodo", true)
                        val intent = Intent()
                        intent.putExtras(bnd_)
                        setResult(2, intent)
                        finish()

                    }
                }
            }
        }


        super.onActivityResult(requestCode, resultCode, data)

    }

    private fun setGravity() {
        val pref = SharedPreference(context)
        if (pref.language.equals("ar", ignoreCase = true)) {
            definitionName.gravity = Gravity.RIGHT

        } else {
            definitionName.gravity = Gravity.LEFT
        }
    }

    companion object {
        var ACTIVITY_NAME = "controllers.applications.AddApplicationsActivity"
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (currentFocus != null)
            imm?.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = getCurrentFocus()
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }
}
