package utilities.data.applicants

import java.io.Serializable

import utilities.data.Base


class LinkApplicationsDAO : Base(), Serializable {


    var ApplicationsDAO: Int = 0
    var linkDefinitionNameCustomFieldId: Int= 0
    var linkDefinitionSectionId: Int= 0
    var applicationId: Int= 0
    var pendingLinkApplications: Int= 0
    var acceptedLinkApplications: Int= 0
    var rejectedLinkApplications: Int= 0
    var isSubmissionAllowed: Boolean = false

    var linkDefinitionCanApplyMultiple: String? = null
    var linkDefinitionName: String? = null



    companion object {
        var BUNDLE_KEY = "LinkApplicationsDAO"
    }
}
