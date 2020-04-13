package utilities.interfaces

import utilities.data.applicants.dynamics.DynamicStagesCriteriaListDAO
import utilities.data.applicants.dynamics.DynamicStagesDAO

interface FeedbackSubmissionClick {

    fun feedbackClick(b: Boolean, criteriaListDAO: DynamicStagesCriteriaListDAO?, dynamicStagesDAO: DynamicStagesDAO?, position: Int)
}
