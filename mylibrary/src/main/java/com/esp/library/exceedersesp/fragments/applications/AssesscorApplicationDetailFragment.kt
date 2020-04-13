package com.esp.library.exceedersesp.fragments.applications

import android.os.AsyncTask
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.esp.library.R
import com.esp.library.utilities.common.GetValues
import com.esp.library.utilities.common.Shared
import com.esp.library.utilities.common.SharedPreference
import com.esp.library.utilities.setup.applications.ListApplicationDetailAdapter
import com.esp.library.exceedersesp.BaseActivity
import kotlinx.android.synthetic.main.fragment_assessor_application_detail.view.*
import utilities.data.applicants.ApplicationDetailFieldsDAO
import utilities.data.applicants.ApplicationSingleton


class AssesscorApplicationDetailFragment : Fragment() {

    internal var TAG = "AssesscorApplicationDetailFragment"
    internal var context: BaseActivity? = null
    internal var pref: SharedPreference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_assessor_application_detail, container, false)

        initialize(v)
        setGravity(v)

        if (ApplicationSingleton.instace.application != null) {

            DetailView(v).execute()

        }

        return v
    }

    private fun initialize(v: View) {
        context = activity as BaseActivity?
        pref = SharedPreference(context!!)
        val mApplicationLayoutManager = LinearLayoutManager(context)
        v.app_list.setHasFixedSize(true)
        v.app_list.layoutManager = mApplicationLayoutManager
        v.app_list.itemAnimator = DefaultItemAnimator()
        v.txtnodata.text = getString(R.string.no) + " " + pref?.getlabels()?.application + " " + getString(R.string.data_available)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }

    override fun onResume() {
        super.onResume()
    }


    override fun onDetach() {
        super.onDetach()

    }


    override fun onDestroyView() {
        super.onDestroyView()
    }


    private inner class DetailView(vv: View) : AsyncTask<Void, Void, List<ApplicationDetailFieldsDAO>>() {
        internal var gv: GetValues? = null
        var v: View=vv;

        override fun onPreExecute() {
            super.onPreExecute()
            gv = GetValues()

        }

        override fun doInBackground(vararg voids: Void): List<ApplicationDetailFieldsDAO>? {
            var fields: List<ApplicationDetailFieldsDAO>? = null

            if (ApplicationSingleton.instace.application != null) {
                if (gv != null)
                    fields = gv!!.GetFields(ApplicationSingleton.instace.application)
            }


            return fields
        }

        override fun onPostExecute(fields: List<ApplicationDetailFieldsDAO>?) {
            super.onPostExecute(fields)

            v.submitted_on.text = getString(R.string.submittedon) + " " + Shared.getInstance().getDisplayDate(context, ApplicationSingleton.instace.application!!.applicationSubmittedDate, true)

            if (fields != null && fields.size > 0) {
                val mApplicationAdapter = ListApplicationDetailAdapter(fields, context, "")
                v.app_list.adapter = mApplicationAdapter
                mApplicationAdapter.notifyDataSetChanged()

                v.detail_view.visibility = View.VISIBLE
                v.no_record.visibility = View.GONE

            } else {

                v.detail_view.visibility = View.GONE
                v.no_record.visibility = View.VISIBLE

            }

        }
    }


    private fun setGravity(v: View) {
        if (pref?.language.equals("ar", ignoreCase = true)) {
            v.submitted_on.gravity = Gravity.RIGHT

        } else {
            v.submitted_on.gravity = Gravity.LEFT
        }
    }

    companion object {

        fun newInstance(): AssesscorApplicationDetailFragment {
            val fragment = AssesscorApplicationDetailFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

}// Required empty public constructor
