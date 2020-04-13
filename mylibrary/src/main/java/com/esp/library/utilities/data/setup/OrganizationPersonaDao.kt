package utilities.data.setup

class OrganizationPersonaDao {

    var id: Int = 0
    lateinit var name: String
    lateinit var supportedLocales: String
    lateinit var persoans: List<Personas>

    inner class Personas {
        lateinit var type: String
        lateinit var aspNetUserId: String
        lateinit var emailAddress: String
        lateinit var name: String
        lateinit var imagerUrl: String
        var statusId: Int = 0
        var id: Int = 0
    }
}
