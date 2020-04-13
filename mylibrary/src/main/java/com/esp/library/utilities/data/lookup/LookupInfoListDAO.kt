package utilities.data.lookup

class LookupInfoListDAO {

    var id: Int = 0
    var titleCustomFieldId: Int = 0
    var fieldsCount: Int = 0
    lateinit var name: String
    lateinit var createdOn: String
    var isVisible: Boolean = false
    var isMain: Boolean = false
    var isShowToApplicant: Boolean = false
    var isVariable: Boolean = false
}
