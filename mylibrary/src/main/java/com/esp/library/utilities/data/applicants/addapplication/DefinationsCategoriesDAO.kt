package utilities.data.applicants.addapplication

import utilities.data.Base
import java.io.Serializable


class DefinationsCategoriesDAO : Base(), Serializable {


    var id: Int = 0
    var name: String? = null
    var definitions: List<CategoryAndDefinationsDAO>? = null

    companion object {
        var BUNDLE_KEY = "DefinationsCategoriesDAO"
    }
}
