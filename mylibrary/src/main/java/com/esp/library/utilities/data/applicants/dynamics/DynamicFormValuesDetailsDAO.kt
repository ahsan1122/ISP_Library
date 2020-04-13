package utilities.data.applicants.dynamics

import java.util.HashMap

import utilities.data.Base
import java.io.Serializable

class DynamicFormValuesDetailsDAO : Base(), Serializable {
    var name: String? = null
    var mimeType: String? = null
    var createdOn: String? = null
    var downloadUrl: String? = null

    companion object {

        var BUNDLE_KEY = "DynamicFormValuesDetailsDAO"
    }
}
