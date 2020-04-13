package utilities.data.applicants

import utilities.data.Base
import java.io.Serializable


class ApplicationsDAO : Base(), Serializable {


    var id: Int = 0
    var applicantName: String? = null

    var category: String? = null
    var definitionName: String? = null
    var applicationNumber: String? = null
    var status: String? = null
    var statusId: Int = 0
    var submittedOn: String? = null
    var createdOn: String? = null
    var startedOn: String? = null
    var assessedOn: String? = null
    var isOverDue: Boolean=false
    var dueDate: String? = null
    var definitionVersion: Int = 0
    var stageStatuses: List<String>? = null


    companion object {
        var BUNDLE_KEY = "ApplicationsDAO"
    }
}
