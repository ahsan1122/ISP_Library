package utilities.interfaces

import utilities.data.CriteriaRejectionfeedback.FeedbackDAO

interface FeedbackConfirmationListener {

    fun isClickable(feedbackList: List<FeedbackDAO>)
    fun editComment(feedbackDAO: FeedbackDAO)
}
