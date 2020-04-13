package utilities.interfaces

import utilities.data.filters.FilterDefinitionSortDAO

interface ApplicationsFilterListener {

    fun selectedValues(filterDefinitionSortList: List<FilterDefinitionSortDAO>, position: Int, checked: Boolean);
    fun selectedSortValues(filterDefinitionSortList: FilterDefinitionSortDAO, filterSortByListSort: List<FilterDefinitionSortDAO>, position: Int);
}
