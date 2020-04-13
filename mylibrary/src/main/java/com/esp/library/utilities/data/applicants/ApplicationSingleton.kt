package utilities.data.applicants


import utilities.data.applicants.dynamics.DynamicResponseDAO

class ApplicationSingleton {

    var application: DynamicResponseDAO? = null

    companion object {

        var applicationSingleton: ApplicationSingleton? = null
        val instace: ApplicationSingleton
            get() {
                if (applicationSingleton == null) {
                    applicationSingleton = ApplicationSingleton()
                }
                return applicationSingleton as ApplicationSingleton
            }
    }


}
