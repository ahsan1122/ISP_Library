package utilities.data.applicants.addapplication

import java.io.Serializable

import utilities.data.Base


class ResponseFileUploadDAO : Base(), Serializable {


    var fileId: String? = null
    var downloadUrl: String? = null

    companion object {
        var BUNDLE_KEY = "ResponseFileUploadDAO"
    }
}
