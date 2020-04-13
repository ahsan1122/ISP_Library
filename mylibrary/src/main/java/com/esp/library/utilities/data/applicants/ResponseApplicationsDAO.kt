package utilities.data.applicants

import java.io.Serializable

import utilities.data.Base

class ResponseApplicationsDAO : Base(), Serializable {

    var totalRecords: Int = 0
    var applications: List<ApplicationsDAO>? = null

    companion object {

        var BUNDLE_KEY = "ResponseApplicationsDAO"
    }
}
