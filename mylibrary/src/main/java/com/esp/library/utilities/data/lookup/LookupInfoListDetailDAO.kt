package utilities.data.lookup

import utilities.data.applicants.dynamics.DynamicFormSectionDAO
import utilities.data.applicants.dynamics.DynamicFormValuesDAO

class LookupInfoListDetailDAO {

    lateinit var lookupTemplate: LookupTemplate
    var pageNumber: Int = 0
    var pageSize: Int = 0
    var totalCount: Int = 0
    lateinit var search: String
    lateinit var items: List<Items>

    inner class LookupTemplate {
        lateinit var form: Form
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

    inner class Form {
        var id: Int = 0
        lateinit var sections: List<DynamicFormSectionDAO>
    }

    inner class Items {
        var id: Int = 0
        var isVisible: Boolean = false
        var importKey: String?=null
        var values: List<DynamicFormValuesDAO>?=null
        var formSectionValues: List<FormSectionValues>?=null
    }

    inner class FormSectionValues {
        lateinit var label: String
        lateinit var value: String
        var isRealTime: Boolean = false
    }

}
