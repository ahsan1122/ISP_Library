package utilities.data.filters

class FilterDefinitionSortDAO() {

    var name: String? = null
    var isCheck: Boolean = false
    var id:Int=0


    constructor(name: String?, isCheck: Boolean, id: Int) : this() {
        this.name = name
        this.isCheck = isCheck
        this.id = id
    }
}
