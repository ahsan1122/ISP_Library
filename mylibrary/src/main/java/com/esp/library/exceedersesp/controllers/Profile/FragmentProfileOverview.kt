package com.esp.library.exceedersesp.controllers.Profile


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.esp.library.R

import kotlinx.android.synthetic.main.fragment_profile_overview.view.*
import utilities.data.applicants.profile.ApplicationProfileDAO


class FragmentProfileOverview : androidx.fragment.app.Fragment() {

    internal var bundle: Bundle? = null
    internal var isprofile: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_profile_overview, container, false)
        bundle = arguments
        val dataapplicant = bundle!!.getSerializable("dataapplicant") as ApplicationProfileDAO
        dataapplicant.applicant

        isprofile = bundle!!.getBoolean("isprofile", false)
        if (isprofile)
            moveToNext()

        view.btcontinuee.setOnClickListener{
            moveToNext()
        }

        return view

    }

    private fun moveToNext() {
        val navHostFragment = FragmentProfileImage()
        navHostFragment.arguments = bundle
        val fm = activity!!.supportFragmentManager
        val ft = fm.beginTransaction()
        ft.setCustomAnimations(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left, R.anim.anim_slide_in_right, R.anim.anim_slide_out_right)
        ft.replace(R.id.request_fragment_org, navHostFragment)
        if (!isprofile)
            ft.addToBackStack("FragmentProfileOverview")
        ft.commit()
    }

}
