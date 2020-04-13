package com.esp.library.exceedersesp.fragments.applications

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.esp.library.R

import com.esp.library.exceedersesp.BaseActivity

import kotlinx.android.synthetic.main.fragment_assessor_stages_detail.view.*
import utilities.adapters.setup.applications.ListStagesDetailAdapter
import utilities.data.applicants.ApplicationSingleton
import utilities.data.applicants.dynamics.DynamicStagesDAO


class AssesscorStagesDetailFragment : androidx.fragment.app.Fragment() {

    internal var context: BaseActivity? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = activity as BaseActivity?

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_assessor_stages_detail, container, false)
        initailize(v)

        if (ApplicationSingleton.instace.application != null) {
            DetailStagesView(v).execute()
        }

        return v
    }

    private fun initailize(v: View) {
        val mApplicationLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        v.app_list.setHasFixedSize(true)
        v.app_list.layoutManager = mApplicationLayoutManager
        v.app_list.itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()
    }


    private inner class DetailStagesView(vv: View) : AsyncTask<Void, Void, List<DynamicStagesDAO>>() {

        var v:View =vv

        override fun doInBackground(vararg param: Void): List<DynamicStagesDAO>? {
            var stages: List<DynamicStagesDAO>? = null

            if (ApplicationSingleton.instace.application != null) {
                stages = ApplicationSingleton.instace.application!!.stages

            }
            return stages
        }

        override fun onPostExecute(stages: List<DynamicStagesDAO>?) {
            super.onPostExecute(stages)

            if (stages != null && stages.size > 0) {
                val mApplicationAdapter = context?.let { ListStagesDetailAdapter(stages, it, "") }
                v.app_list.adapter = mApplicationAdapter
                mApplicationAdapter?.notifyDataSetChanged()

                v.detail_view.visibility = View.VISIBLE
                v.no_record.visibility = View.GONE
            } else {

                v.detail_view.visibility = View.GONE
                v.no_record.visibility = View.VISIBLE
            }

        }
    }

    companion object {
        fun newInstance(): AssesscorStagesDetailFragment {
            val fragment = AssesscorStagesDetailFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}
