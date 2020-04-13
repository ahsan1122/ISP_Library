package utilities.data.applicants.dynamics

import utilities.data.Base
import java.io.Serializable


class DynamicStagesDAO : Base(), Serializable {


    var id: Int = 0
    var name: String? = null
    var completedOn: String? = null
    var totalWeight: Int = 0
    var createdBy: Int = 0
    var order: Int = 0
    var linkDefinitionId: Int = 0
    var applicationDefinationId: Int = 0
    var linkDefinitionNameCustomFieldId: Int = 0
    var linkDefinitionSectionId: Int = 0
    var isAll: Boolean = false
    var linkDefinitionCanApplyMultiple: Boolean = false
    var status: String? = null
    var criteriaCount: Int = 0
    var statusId: Int = 0
    var isSystem: Boolean = false
    var isEnabled: Boolean = false
    var localStatus: String? = null
    var type: String? = null
    var linkDefinitionValue: String? = null
    var linkDefinitionName: String? = null

    var criteriaList: List<DynamicStagesCriteriaListDAO>? = null

    companion object {
        var BUNDLE_KEY = "DynamicStagesDAO"
    }
}
