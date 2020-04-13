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
import android.view.inputmethod.InputMethodManager
import com.esp.library.R
import com.esp.library.utilities.common.Shared

import com.esp.library.exceedersesp.BaseActivity
import kotlinx.android.synthetic.main.fragment_assessor_stages_criteria_comments.view.*

import utilities.adapters.setup.applications.ListApplicationStageCriteriaCommentsAdapter
import utilities.data.applicants.ApplicationSingleton
import utilities.data.applicants.dynamics.DynamicStagesCriteriaCommentsListDAO
import utilities.data.applicants.dynamics.DynamicStagesCriteriaListDAO


class AssesscorApplicationStagesCriteriaCommentsFragment : androidx.fragment.app.Fragment() {

    internal var context: BaseActivity? = null
    internal var imm: InputMethodManager? = null

    internal var mCriteria: DynamicStagesCriteriaListDAO? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = activity as BaseActivity?

        if (arguments != null) {
            mCriteria = arguments!!.getSerializable(DynamicStagesCriteriaListDAO.BUNDLE_KEY) as DynamicStagesCriteriaListDAO

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_assessor_stages_criteria_comments, container, false)
        initailize(v)
        if (ApplicationSingleton.instace.application != null) {
            DetailStagesCriteriaCommentsView(v).execute()
        }

        return v
    }

    private fun initailize(v: View) {
        val mApplicationLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        v.app_list.setHasFixedSize(true)
        v.app_list.layoutManager = mApplicationLayoutManager
        v.app_list.itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()
        imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }


    fun UpdateCriteriaCommentsList(v: View?) {
        if (ApplicationSingleton.instace.application != null) {
            v?.let { DetailStagesCriteriaCommentsView(it).execute() }
        }
    }

    inner class DetailStagesCriteriaCommentsView(vv: View) : AsyncTask<Void, Void, List<DynamicStagesCriteriaCommentsListDAO>>() {

        var v: View =vv

        override fun doInBackground(vararg param: Void): List<DynamicStagesCriteriaCommentsListDAO>? {
            var stages_criteria: List<DynamicStagesCriteriaCommentsListDAO>? = null

            if (mCriteria != null) {
                stages_criteria = Shared.getInstance().GetCriteriaCommentsListByCriteriaId(mCriteria!!.id)
            }
            return stages_criteria
        }

        override fun onPostExecute(stages_criteria: List<DynamicStagesCriteriaCommentsListDAO>?) {
            super.onPostExecute(stages_criteria)

            // UpdateView();

            if (stages_criteria != null && stages_criteria.size > 0) {
                val mApplicationAdapter = context?.let { ListApplicationStageCriteriaCommentsAdapter(stages_criteria, it, "") }
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

        fun newInstance(criteria: DynamicStagesCriteriaListDAO): AssesscorApplicationStagesCriteriaCommentsFragment {
            val fragment = AssesscorApplicationStagesCriteriaCommentsFragment()
            val args = Bundle()
            args.putSerializable(DynamicStagesCriteriaListDAO.BUNDLE_KEY, criteria)
            fragment.arguments = args
            return fragment
        }
    }


}// Required empty public constructor
