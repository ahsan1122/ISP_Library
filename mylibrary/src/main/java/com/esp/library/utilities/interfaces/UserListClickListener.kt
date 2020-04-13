package utilities.interfaces

import utilities.data.applicants.UsersListDAO

interface UserListClickListener {

    fun userClick(userslistDAO: UsersListDAO?)
}
