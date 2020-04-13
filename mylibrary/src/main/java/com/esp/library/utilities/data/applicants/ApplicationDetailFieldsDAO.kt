package utilities.data.applicants

import java.io.Serializable

import utilities.data.Base
import utilities.data.applicants.dynamics.DynamicCriteriaCustomFieldsDetailDAO
import utilities.data.applicants.dynamics.DynamicCriteriaCustomFieldsLookUpsDAO
import utilities.data.applicants.dynamics.DynamicFormSectionFieldDAO
import utilities.data.applicants.dynamics.DynamicFormSectionFieldLookupValuesDAO
import utilities.data.applicants.dynamics.DynamicFormValuesDetailsDAO


class ApplicationDetailFieldsDAO : Base(), Serializable {

    var type: Int = 0
    var fieldName: String? = null
    var fieldvalue: String? = null
    var downloadURL: String? = null
    var photo_detail: DynamicFormValuesDetailsDAO? = null
    var singleSelection: List<DynamicFormSectionFieldLookupValuesDAO>? = null
    var multiSelection: List<DynamicFormSectionFieldLookupValuesDAO>? = null

    var photo_detailCriteria: DynamicCriteriaCustomFieldsDetailDAO? = null
    var singleSelectionCriteria: List<DynamicCriteriaCustomFieldsLookUpsDAO>? = null
    var multiselectionCriteria: List<DynamicCriteriaCustomFieldsLookUpsDAO>? = null
    var isViewGenerated: Boolean = false
    var isFileDownloaded: Boolean = false
    var isFileDownling: Boolean = false
    var isSection: Boolean = false
    var sectionname: String? = null
    var fieldsDAO: DynamicFormSectionFieldDAO? = null

    lateinit var tag: String
    var sectionId: Int = 0
    var lookupId: Int = 0

    companion object {
        var BUNDLE_KEY = "ApplicationDetailFieldsDAO"
    }
}
