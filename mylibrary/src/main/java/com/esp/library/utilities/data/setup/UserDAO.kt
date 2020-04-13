package utilities.data.setup

import utilities.data.Base
import java.io.Serializable

class UserDAO : Base(), Serializable {

    var loginResponse: TokenDAO? = null
    var profileStatus: String? = null
    var role: String? = null


    companion object {
        var BUNDLE_KEY = "UserDAO"
    }
}
