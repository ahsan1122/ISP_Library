package utilities.data.applicants

import utilities.data.Base
import utilities.data.applicants.addapplication.LookUpDAO
import utilities.data.applicants.dynamics.DyanmicFormSectionFieldDetailsDAO
import java.io.Serializable

class CalculatedMappedFieldsDAO : Base(),Serializable {

    var sectionId: Int = 0
    var sectionIndex: Int = 0
    var sectionCustomFieldId: Int = 0
    var targetFieldType: Int = 0
    var value: String = ""
    var details: DyanmicFormSectionFieldDetailsDAO? = null
    var lookupItems: List<LookUpDAO>? = null
}
