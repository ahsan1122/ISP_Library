package utilities.data.applicants.dynamics

import utilities.data.Base
import java.io.Serializable
import java.util.*

class DynamicFormSectionDAO : Base(), Serializable {
    var id: Int = 0
    var defaultName: String? = null
    //var approveText: String? = null
   // var rejectText: String? = null
    var isMultipule: Boolean = false
    var isActive: Boolean = false
    var isDelete: Boolean = false
    var isDefault: Boolean = false
    var order: Int = 0
    var type: Int = 0
  //  var assessmentStatus: String? = null
    var lastUpdatedOn: String? = null
    var fields: List<DynamicFormSectionFieldDAO>? = null
    var isShowError = false
    var dynamicStagesCriteriaListDAO: DynamicStagesCriteriaListDAO? = null
    var stages: List<DynamicStagesDAO>? = null

    //For PostResponse
    var nestedSections: List<DynamicFormSectionDAO>? = null

    //For LocalUse
    internal var fieldsCardsList: List<DynamicFormSectionFieldsCardsDAO>? = null

    fun getFieldsCardsList(): List<DynamicFormSectionFieldsCardsDAO> {

        if (fieldsCardsList == null)
            fieldsCardsList = ArrayList()

        return fieldsCardsList as List<DynamicFormSectionFieldsCardsDAO>
    }

    fun setFieldsCardsList(fieldsCardsList: MutableList<DynamicFormSectionFieldsCardsDAO>?) {
        var fieldsCardsList = fieldsCardsList

        if (fieldsCardsList == null)
            fieldsCardsList = ArrayList()

        fieldsCardsList.add(DynamicFormSectionFieldsCardsDAO(fields!!))

        this.fieldsCardsList = fieldsCardsList
    }

    fun setRefreshFieldsCardsList(fieldsCardsList: List<DynamicFormSectionFieldsCardsDAO>) {
        this.fieldsCardsList = fieldsCardsList
    }

    companion object {

        var BUNDLE_KEY = "DynamicFormSectionDAO"
    }

}
