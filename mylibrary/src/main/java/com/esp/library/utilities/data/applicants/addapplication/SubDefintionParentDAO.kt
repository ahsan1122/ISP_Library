package utilities.data.applicants.addapplication

import utilities.data.Base
import java.io.Serializable

class SubDefintionParentDAO : Base(), Serializable {

    var mainApplicationNumber: String? = null
    var submittedBy: String? = null
    var applicantEmail: String? = null
    var submittedOn: String? = null
    var titleFieldValue: String? = null
    var descriptionFieldValue: String? = null
    var applicationId: Int = 0
}
