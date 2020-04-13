package com.esp.library.exceedersesp.fragments.setup

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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

import com.esp.library.R
import kotlinx.android.synthetic.main.select_organization_fragment.view.*
import utilities.data.setup.PersonaDAO
import utilities.data.setup.TokenDAO


class SelectOrganizationFragment : androidx.fragment.app.Fragment() {

    internal var personas: TokenDAO? = null
    internal var context: BaseActivity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = activity as BaseActivity?
        if (arguments != null) {
            personas = arguments!!.getSerializable(TokenDAO.BUNDLE_KEY) as TokenDAO

            if (personas != null && personas!!.personas != null && personas!!.personas.length > 0) {
                LoadPersonas().execute(personas)
            }


        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.select_organization_fragment, container, false)
        initailize(v)
        return v
    }

    private fun initailize(v:View) {
        val mOrgLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
        v.org_list.setHasFixedSize(true)
        v.org_list.layoutManager = mOrgLayoutManager
        v.org_list.itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()
    }

    private inner class LoadPersonas : AsyncTask<TokenDAO, Void, List<PersonaDAO>>() {

        override fun doInBackground(vararg listPersonaDAOS: TokenDAO): List<PersonaDAO>? {

            var list: List<PersonaDAO>? = null

            if (listPersonaDAOS[0] != null) {
                val personas = listPersonaDAOS[0].personas
                val gson = Gson()
                list = gson.fromJson<List<PersonaDAO>>(personas, object : TypeToken<List<PersonaDAO>>() {

                }.type)
            }

            return list
        }

        override fun onPostExecute(personaDAOS: List<PersonaDAO>?) {
            super.onPostExecute(personaDAOS)
        }
    }

    companion object {

        fun newInstance(persona: TokenDAO): SelectOrganizationFragment {
            val fragment = SelectOrganizationFragment()
            val args = Bundle()
            args.putSerializable(TokenDAO.BUNDLE_KEY, persona)
            fragment.arguments = args
            return fragment
        }
    }
}
