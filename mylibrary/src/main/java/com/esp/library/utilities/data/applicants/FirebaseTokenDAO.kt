package utilities.data.applicants

class FirebaseTokenDAO {

    var fbTokenId: Int = 0
    var personaId: Int = 0
    var organizationId: Int = 0
    var token: String? = null
    var deviceId: String? = null
    lateinit var data: Any
    lateinit var errorMessage: String
    var status: Boolean = false
}
