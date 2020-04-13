package utilities.data.applicants.dynamics

import java.io.Serializable

import utilities.data.Base


class DynamicCriteriaCustomFieldsLookUpsDAO : Base(), Serializable {

    var id: Int = 0
    var label: String? = null
    var isSelected: Boolean = false
    var customFieldId: Int = 0
    var order: Int = 0

    companion object {
        var BUNDLE_KEY = "DynamicCriteriaCustomFieldsLookUpsDAO"
    }
}
