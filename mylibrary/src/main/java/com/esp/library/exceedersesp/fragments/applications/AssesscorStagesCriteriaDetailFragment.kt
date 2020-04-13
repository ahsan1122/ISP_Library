package com.esp.library.exceedersesp.fragments.applications

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.esp.library.exceedersesp.BaseActivity

import com.esp.library.R
import com.esp.library.utilities.common.Shared
import kotlinx.android.synthetic.main.fragment_assessor_stages_criteria_detail.view.*
import utilities.adapters.setup.applications.ListStagesCriteriaDetailAdapter
import utilities.data.applicants.ApplicationSingleton
import utilities.data.applicants.dynamics.DynamicStagesCriteriaListDAO
import utilities.data.applicants.dynamics.DynamicStagesDAO


class AssesscorStagesCriteriaDetailFragment : androidx.fragment.app.Fragment() {

    internal var context: BaseActivity? = null
    private var mStageAdapter: androidx.recyclerview.widget.RecyclerView.Adapter<*>? = null
    private var mStageLayoutManager: androidx.recyclerview.widget.RecyclerView.LayoutManager? = null
    internal var mStage: DynamicStagesDAO? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = activity as BaseActivity?

        if (arguments != null) {
            mStage = arguments!!.getSerializable(DynamicStagesDAO.BUNDLE_KEY) as DynamicStagesDAO

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_assessor_stages_criteria_detail, container, false)
        initailize(v)


        if (ApplicationSingleton.instace.application != null) {
            DetailStagesCriteriaView(v).execute()
        }

        return v
    }

    private fun initailize(v: View) {
        mStageLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        v.app_list.setHasFixedSize(true)
        v.app_list.layoutManager = mStageLayoutManager
        v.app_list.itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()
    }

    fun UpdateCriteriaList(v: View?) {
        if (ApplicationSingleton.instace.application != null) {
            v?.let { DetailStagesCriteriaView(it).execute() }
        }
    }

    inner class DetailStagesCriteriaView(vv: View) : AsyncTask<Void, Void, List<DynamicStagesCriteriaListDAO>>() {

        var v:View =vv

        override fun doInBackground(vararg param: Void): List<DynamicStagesCriteriaListDAO>? {
            var stages_criteria: List<DynamicStagesCriteriaListDAO>? = null

            if (mStage != null) {
                stages_criteria = Shared.getInstance().GetCriteriaListByStageId(mStage!!.id)
            }
            return stages_criteria
        }

        override fun onPostExecute(stages_criteria: List<DynamicStagesCriteriaListDAO>?) {
            super.onPostExecute(stages_criteria)



            if (stages_criteria != null && stages_criteria.size > 0) {
                mStageAdapter = context?.let { ListStagesCriteriaDetailAdapter(stages_criteria,mStage, it, "") }
                v.app_list.adapter = mStageAdapter
                mStageAdapter!!.notifyDataSetChanged()
                v.detail_view.visibility = View.VISIBLE
                v.no_record.visibility = View.GONE
                UpdateView(v)
            } else {
                v.detail_view.visibility = View.GONE
                v.no_record.visibility = View.VISIBLE
            }

        }
    }

    private fun UpdateView(v: View) {
        if (mStage != null) {
            if (mStage!!.isAll) {
                v.condition_value.text = getString(R.string.all)
            } else {
                v.condition_value.text = getString(R.string.any)
            }

            v.sequence_value.text = mStage!!.order.toString() + ""

            if (mStage!!.criteriaList != null && mStage!!.criteriaList!!.size > 0) {
                v.criteria_count.text = mStage!!.criteriaList!!.size.toString() + ""
            } else {
                v.criteria_count.text = "0"
            }

        }
    }

    companion object {
        fun newInstance(stages: DynamicStagesDAO): AssesscorStagesCriteriaDetailFragment {
            val fragment = AssesscorStagesCriteriaDetailFragment()
            val args = Bundle()
            args.putSerializable(DynamicStagesDAO.BUNDLE_KEY, stages)
            fragment.arguments = args
            return fragment
        }
    }

}// Required empty public constructor
