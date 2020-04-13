package utilities.data.setup

import utilities.data.Base

class IdenediAuthDAO : Base() {

    var AccessToken: String? = null
    var RefreshToken: String? = null
    var EmailAddress: String? = null
    var IdenediId: String? = null


    companion object {

        var BUNDLE_KEY = "IdenediAuthDAO"
    }
}
