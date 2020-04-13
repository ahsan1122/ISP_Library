package utilities.data.applicants.dynamics

import java.io.Serializable

import utilities.data.Base


class DynamicStagesCriteriaListDAO : Base(), Serializable {

    var id: Int = 0
    var name: String? = null
    var stageId: Int = 0
    var ownerId: Int = 0
    var ownerIdJobRole: Int = 0
    var ownerIdUserLookupCustomFieldPath: String? = null
    var isOwner: Boolean = false
    var isValidate: Boolean = false
    var isEnabled: Boolean = false
    var reassignAssessments: Boolean = false
    var hasApplication: Boolean = false
    var isExpended: Boolean = false
    var weight: Int = 0
    var daysToComplete: Int = 0
    var subApplicantId: Int = 0
    var subApplicationId: Int = 0
    var subDefinitionId: Int = 0
    var ownerName: String? = null
    var ownerEmailAddress: String? = null
    var reminder: String? = null
    var assessmentStatus: String? = null
    var approveText: String? = null
    var rejectText: String? = null
    var type: String? = null
    var assessmentId: Int = 0
    var comments: List<DynamicStagesCriteriaCommentsListDAO>? = null
    var customFields:  List<DynamicFormSectionFieldDAO>? = null
    var customFieldsCount: Int = 0
    var isSystem: Boolean = false
    lateinit var form: DynamicFormDAO
    lateinit var formValues: List<DynamicFormValuesDAO>

    companion object {
        var BUNDLE_KEY = "DynamicStagesCriteriaListDAO"
    }

    /*public List<DynamicCriteriaCustomFieldsDAO> getCustomFields() {
		return customFields;
	}

	public void setCustomFields(List<DynamicCriteriaCustomFieldsDAO> customFields) {
		this.customFields = customFields;
	}*/
}
