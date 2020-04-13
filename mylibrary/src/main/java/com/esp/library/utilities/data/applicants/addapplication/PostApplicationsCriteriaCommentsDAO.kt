package utilities.data.applicants.addapplication

import java.io.Serializable

import utilities.data.Base


class PostApplicationsCriteriaCommentsDAO : Base(), Serializable {

    var id: Int = 0
    var commentId: Int = 0
    var assessmentId: Int = 0
    var comments: String? = null

    companion object {
        var BUNDLE_KEY = "PostApplicationsCriteriaCommentsDAO"
    }


}
