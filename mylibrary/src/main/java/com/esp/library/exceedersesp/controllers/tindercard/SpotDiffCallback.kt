package com.esp.library.exceedersesp.controllers.tindercard

import androidx.recyclerview.widget.DiffUtil
import utilities.data.applicants.ApplicationsDAO

class SpotDiffCallback(
        private val old: List<ApplicationsDAO>,
        private val new: List<ApplicationsDAO>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return old.size
    }

    override fun getNewListSize(): Int {
        return new.size
    }

    override fun areItemsTheSame(oldPosition: Int, newPosition: Int): Boolean {
        return old[oldPosition].id == new[newPosition].id
    }

    override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
        return old[oldPosition] == new[newPosition]
    }

}
