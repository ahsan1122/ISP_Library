package utilities.data.applicants.profile

import utilities.data.Base
import utilities.data.applicants.dynamics.DyanmicFormSectionFieldDetailsDAO
import utilities.data.applicants.dynamics.DynamicFormSectionDAO
import java.io.Serializable

class ApplicationProfileDAO : Base(), Serializable {


    lateinit var applicant: Applicant
    lateinit var sections: List<DynamicFormSectionDAO>

    inner class Applicant : Base(), Serializable {

        var createdOn: String? = null
        var emailAddress: String? = null
        var imageUrl: String? = null
        var invitedOn: String? = null
        var name: String? = null
        var profileTemplateString: String? = null
        var status: String? = null
        var id: Int = 0
        var applicantStatus: Int = 0
        var version: Int = 0
        var idenediKey: String? = null
        var isProfileSubmitted: Boolean = false
        var applicantSections: List<ApplicationSection>? = null
    }

    inner class ApplicationSection : Base(), Serializable {

        var sectionId: Int = 0
        var index: Int = 0
        var lastUpdatedOn: String? = null
        var values: List<Values>? = null

    }

    class Values : Base(), Serializable {
        var value: String? = null
        var type:Int=0
        var sectionFieldId: Int = 0
        var lookupValue: String? = null
        var details: DyanmicFormSectionFieldDetailsDAO? = null
    }

}
