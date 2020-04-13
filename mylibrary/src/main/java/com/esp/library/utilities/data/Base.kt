package utilities.data

import com.google.gson.Gson

import java.io.Serializable

open class Base : Serializable {

    lateinit var error: String
    lateinit var error_description: String

    fun toJson(): String {
        val gson = Gson()
        return gson.toJson(this)

    }


}
