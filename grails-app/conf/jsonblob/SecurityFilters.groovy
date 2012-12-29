package jsonblob

class SecurityFilters {
    def filters = {
        all(uri: "/account/**") {
            before = {
                // Ignore direct views (e.g. the default main index page).
                if (!controllerName) return true

                // Access control by convention.
                accessControl()
            }
        }
    }
}
