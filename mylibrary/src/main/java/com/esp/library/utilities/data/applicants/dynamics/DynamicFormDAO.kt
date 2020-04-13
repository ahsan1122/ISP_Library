package utilities.data.applicants.dynamics

import utilities.data.Base

class DynamicFormDAO : Base() {
    var id: Int = 0
    var sections: List<DynamicFormSectionDAO>? = null

    companion object {

        var BUNDLE_KEY = "DynamicFormDAO"
    }
}
