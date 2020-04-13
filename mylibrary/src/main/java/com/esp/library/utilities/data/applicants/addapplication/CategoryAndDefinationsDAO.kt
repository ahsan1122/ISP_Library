package utilities.data.applicants.addapplication

import utilities.data.Base
import java.io.Serializable


class CategoryAndDefinationsDAO : Base(), Serializable {


    var id: Int = 0
    var name: String? = null
    var isActive: Boolean = false
    var typeId: Int = 0
    var description: String? = null
    var category: String? = null
    var isPublished: Boolean = false
    var isChecked: Boolean = false
    var createdOn: String? = null
    var iconName: String? = null
    var parentApplicationInfo: SubDefintionParentDAO? = null

    companion object {
        var BUNDLE_KEY = "CategoriesDAO"
    }
}
