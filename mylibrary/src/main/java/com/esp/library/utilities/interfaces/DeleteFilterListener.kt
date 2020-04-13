package utilities.interfaces

import utilities.data.applicants.addapplication.CategoryAndDefinationsDAO

interface DeleteFilterListener {

    fun deleteFilters(filtersList: CategoryAndDefinationsDAO)
}
