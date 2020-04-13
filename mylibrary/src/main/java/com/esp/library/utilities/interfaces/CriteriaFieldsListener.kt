package utilities.interfaces

import utilities.data.applicants.dynamics.DynamicStagesCriteriaListDAO

interface CriteriaFieldsListener {

    fun validateCriteriaFields(listDAO: DynamicStagesCriteriaListDAO)

}
