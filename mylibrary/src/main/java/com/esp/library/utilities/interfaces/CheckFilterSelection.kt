package utilities.interfaces

import utilities.data.applicants.addapplication.CategoryAndDefinationsDAO

interface CheckFilterSelection {

    fun checkFilterSelection(mApplications: List<CategoryAndDefinationsDAO>)
}
