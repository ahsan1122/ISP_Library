package utilities.data.applicants.dynamics

import java.util.HashMap

import utilities.data.Base

class DynamicFormSectionFieldsCardsDAO : Base {

    var sectionId: Int = 0

    var fields: List<DynamicFormSectionFieldDAO>? = null
    var values: List<DynamicSectionValuesDAO.Instance.Value>? = null

    var valuesHashMap = HashMap<Int, List<DynamicSectionValuesDAO.Instance.Value>>()

    lateinit var tag: String

    constructor() {

    }

    constructor(fields: List<DynamicFormSectionFieldDAO>) {
        this.fields = fields
    }

    companion object {

        var BUNDLE_KEY = "DynamicFormSectionFieldsCardsDAO"
    }
}
