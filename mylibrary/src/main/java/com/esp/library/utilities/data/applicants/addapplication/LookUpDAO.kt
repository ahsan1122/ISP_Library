package utilities.data.applicants.addapplication

import utilities.data.Base
import java.io.Serializable




class LookUpDAO : Base(), Serializable {

    var applicantName: String? = null
    var id: Int = 0
    var name: String? = null

    companion object {
        var BUNDLE_KEY = "LookUpDAO"
    }

}
