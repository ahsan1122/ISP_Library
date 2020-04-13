package utilities.data.applicants.addapplication

import java.io.Serializable

import utilities.data.Base
import utilities.data.applicants.dynamics.DynamicFormValuesDAO


class PostApplicationsStatusDAO : Base(), Serializable {


    var applicationId: Int = 0
    var assessmentId: Int = 0
    var comments: String? = null
    var isAccepted: Boolean = false
    var stageId: Int = 0
    var values: List<DynamicFormValuesDAO>? = null

    companion object {
        var BUNDLE_KEY = "PostApplicationsStatusDAO"
    }
}
