package utilities.data.lookup

import utilities.data.applicants.dynamics.DynamicFormValuesDAO

class LookupItemDetailDAO {

    var id: Int = 0
    var isVisible: Boolean = false
    lateinit var importKey: String
    lateinit var lookup: LookupInfoListDetailDAO.LookupTemplate
    lateinit var values: List<DynamicFormValuesDAO>
}
