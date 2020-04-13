package utilities.data.applicants.dynamics

import utilities.data.Base
import utilities.data.lookup.LookupValuesDAO
import java.io.Serializable

class DynamicFormValuesDAO : Base(), Serializable {

    var id: Int = 0
    var customFieldLookupId: Int = 0
    var itemid: Int = 0
    var createdOn: String? = null
    var sectionCustomFieldId: Int = 0
    var value: String? = null
    var sectionId: Int = 0
    var details: DynamicFormValuesDetailsDAO? = null
    var selectedLookupText: String? = null
    var label: String? = null
    var type: Int = 0
    var lookupValuesDAO: LookupValuesDAO? = null
    var filterLookup: String? = null

    companion object {

        var BUNDLE_KEY = "DynamicFormValuesDAO"
    }
}
