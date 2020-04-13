package utilities.data.lookup

import utilities.data.Base
import java.io.Serializable

class LookupValuesDAO : Serializable {

    lateinit var title: String
    lateinit var employeeName: String
    lateinit var label: String
    lateinit var value: String
}
