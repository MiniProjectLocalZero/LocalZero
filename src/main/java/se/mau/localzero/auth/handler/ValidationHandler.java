package se.mau.localzero.auth.handler;

import se.mau.localzero.domain.User;

/**
 * A specific link in the registration chain
 * Controls that the user has entered both Username and Password
 */

public class ValidationHandler extends RegistrationHandler {
    @Override
    public boolean check(User user) {
        if (user.getUsername() == null || user.getPassword() == null) {
            throw new IllegalArgumentException("Username or Password is missing");
        }
        return super.check(user); //Go to the next part of the chain
    }
}
