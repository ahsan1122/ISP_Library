package utilities.data.setup

import com.google.gson.annotations.SerializedName
import utilities.data.Base

class TokenDAO : Base() {

    var access_token: String? = null
    var token_type: String? = null
    var expires_in: String? = null
    var refresh_token: String? = null
    var email: String? = null
    var password: String? = null
    var applicantPersonaId: String? = null
    var role: String? = null
    var name: String? = null
    var imageUrl: String? = null
    var idenedi: String? = null
    lateinit var personas: String
    /*public List<PersonaDAO> getPersonas() {
        return personas;
    }

    public void setPersonas(List<PersonaDAO> personas) {
        this.personas = personas;
    }*/

    var organizationId: String? = null


    @SerializedName("as:client_id")
    var client_id: String? = null

    @SerializedName(".expires")
    var expires: String? = null

    @SerializedName(".issued")
    var issued: String? = null

    companion object {

        var BUNDLE_KEY = "TokenDAO"
    }
}
