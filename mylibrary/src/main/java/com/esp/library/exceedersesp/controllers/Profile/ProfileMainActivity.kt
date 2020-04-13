package com.esp.library.exceedersesp.controllers.Profile

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.esp.library.R
import com.esp.library.utilities.common.Shared


import com.esp.library.exceedersesp.controllers.applications.ApplicationsActivityDrawer

import utilities.data.applicants.profile.ApplicationProfileDAO

class ProfileMainActivity : AppCompatActivity() {
    var navHostFragment: FragmentProfileOverview? = null
    var bContext: Context? = null
    internal var ischeckerror: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_main_activity)
        bContext = this@ProfileMainActivity


        val dataapplicant = intent.getSerializableExtra("dataapplicant") as ApplicationProfileDAO
        dataapplicant.applicant.isProfileSubmitted

        ischeckerror = intent.getBooleanExtra("ischeckerror", false)
        val bundle = Bundle()
        bundle.putSerializable("dataapplicant", dataapplicant)
        bundle.putBoolean("ischeckerror", ischeckerror)
        bundle.putBoolean("isprofile", intent.getBooleanExtra("isprofile", false))
        navHostFragment = FragmentProfileOverview()
        navHostFragment?.arguments = bundle
        val fm = supportFragmentManager
        val ft = fm.beginTransaction()
        ft.setCustomAnimations(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left, R.anim.anim_slide_in_right, R.anim.anim_slide_out_right)
        ft.add(R.id.request_fragment_org, navHostFragment!!)
        ft.commit()

    }

    override fun onBackPressed() {

        if (ischeckerror)
            Shared.getInstance().callIntentClearAllActivities(ApplicationsActivityDrawer::class.java, bContext as Activity, null)
        else
            super.onBackPressed()
    }
}
