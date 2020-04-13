package utilities.data.setup

import java.io.Serializable

import utilities.data.Base

class PostTokenDAO : Base(), Serializable {

    var grant_type: String? = null
    var username: String? = null
    var password: String? = null
    var client_id: String? = null
    var scope: String? = null
    var refresh_token: String? = null

    companion object {

        var BUNDLE_KEY = "PostTokenDAO"
    }
}
