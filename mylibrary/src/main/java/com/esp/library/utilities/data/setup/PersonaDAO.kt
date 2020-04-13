package utilities.data.setup

import java.io.Serializable

import utilities.data.Base

class PersonaDAO : Base(), Serializable {

    var type: String? = null
    var id: String? = null
    var orgId: String? = null
    var orgImageUrl: String? = null
    var orgName: String? = null
    var locales: String? = null

    var refresh_token: String? = null

    companion object {

        var BUNDLE_KEY = "PersonaDAO"
    }
}
