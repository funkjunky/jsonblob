package jsonblob

import org.apache.shiro.authc.AccountException
import org.apache.shiro.authc.IncorrectCredentialsException
import org.apache.shiro.authc.UnknownAccountException
import org.apache.shiro.authc.SimpleAccount
import org.apache.shiro.authz.permission.WildcardPermission

class DbRealm {
    static authTokenClass = org.apache.shiro.authc.UsernamePasswordToken

    def credentialMatcher

    def authenticate(authToken) {
        log.debug "Attempting to authenticate '${authToken.username}'"
        def emailAddress = authToken.username

        // Null email is invalid
        if (emailAddress == null) {
            throw new AccountException("Null email addresses are not allowed")
        }

        // Get the user with the given email. If the user is not
        // found, then they don't have an account and we throw an
        // exception.
        def user = User.findByEmail(emailAddress)
        if (!user) {
            throw new UnknownAccountException("No account found for user '${emailAddress}'")
        }

        log.debug "Found user '${user.email}'"

        // Now check the user's password against the hashed value stored
        // in the database.
        def account = new SimpleAccount(emailAddress, user.passwordHash, "DbRealm")
        if (!credentialMatcher.doCredentialsMatch(authToken, account)) {
            log.debug "Invalid password for user '${user.email}'"
            throw new IncorrectCredentialsException("Invalid password for user '${emailAddress}'")
        }

        return account
    }

    def hasRole(principal, roleName) {
        true //short circuit because we're not using Roles
    }

    def hasAllRoles(principal, roles) {
        true //short circuit because we're not using Roles
    }

    def isPermitted(principal, requiredPermission) {
        true //short circuit because we're not using Permissions
    }
}
