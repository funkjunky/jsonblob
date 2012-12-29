package jsonblob

import org.apache.shiro.crypto.hash.Sha256Hash

class AccountController {

    def create() {
        String emailAddress = params.emailAddress
        String password = params.password

        String passwordHash = new Sha256Hash(password).toHex()

        User newUser = new User(email: emailAddress, passwordHash: passwordHash)
        newUser.save()
    }


}
