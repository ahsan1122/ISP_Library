package utilities.data.filters

import utilities.data.Base
import java.io.Serializable

class FilterDAO : Base(), Serializable {

    var isFilterApplied: Boolean = false
    var search: String? = null
    var statuses: List<String>? = null
    var definitionIds: List<Int>? = null
    var pageNo: Int = 0
    var recordPerPage: Int = 0
    var isMySpace: Boolean = false
    var myApplications: Boolean = false
    var sortBy: Int = 1
    var applicantId: String? = null
    var parentApplicationId: String? = null
    var definationId: String? = null
    var categoreis: List<String>? = null

    companion object {

        var BUNDLE_KEY = "FilterDAO"
    }
}
