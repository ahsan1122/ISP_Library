package utilities.data.applicants.dynamics

import java.util.HashMap

import utilities.data.Base

class DynamicFormSectionFieldLookupValuesDAO : Base() {

    var id: Int = 0
    var label: String? = null
    var isSelected: Boolean = false
    var customFieldId: Int = 0
    var order: Int = 0

    companion object {

        var BUNDLE_KEY = "DynamicFormSectionFieldLookupValuesDAO"
    }
}
