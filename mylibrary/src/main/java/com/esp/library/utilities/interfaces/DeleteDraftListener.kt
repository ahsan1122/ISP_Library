package utilities.interfaces

import utilities.data.applicants.ApplicationsDAO

interface DeleteDraftListener {

    fun deletedraftApplication(applicationsDAO: ApplicationsDAO)

}
