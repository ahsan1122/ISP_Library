package utilities.data.applicants.dynamics

import java.io.Serializable

import utilities.data.Base


class DynamicCriteriaCustomFieldsDAO : Base(), Serializable {

    var id: Int = 0
    var objectId: Int = 0
    var label: String? = null
    var value: String? = null
    var isRequired: Boolean = false
    var isCommon: Boolean = false
    var isVisible: Boolean = false
    var minVal: Int = 0
    var maxVal: Int = 0
    var minDate: String? = null
    var maxDate: String? = null
    var order: Int = 0
    var createdBy: Int = 0
    var type: Int = 0
    var createdOn: String? = null
    var isSystem: Boolean = false
    var allowedValuesCriteria: String? = null
    var selectedCurrencyId: Int = 0
    var selectedCurrencySymbol: String? = null
    var sectionTemplateFiledId: Int = 0
    var sectionCustomFieldId: Int = 0
    var lookUpId: Int = 0
    var lookupValue: String? = null
    var isTitleField: Boolean = false
    var isCanDisabled: Boolean = false

    var lookupValues: List<DynamicCriteriaCustomFieldsLookUpsDAO>? = null
    var details: DynamicCriteriaCustomFieldsDetailDAO? = null

    companion object {
        var BUNDLE_KEY = "DynamicCriteriaCustomFieldsDAO"
    }
}
