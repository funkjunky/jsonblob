package jsonblob

class User {
    String email
    String passwordHash
    String apiKey
    Set<String> blobIds = []

    static constraints = {
        email nullable: false, blank: false, unique: true, email: true
        apiKey unique: true
    }

    static mapping = {
        email index: true
        apiKey index: true
    }

    static mapWith = "mongo"
}
