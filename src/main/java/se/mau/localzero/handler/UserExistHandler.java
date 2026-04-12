package se.mau.localzero.handler;

import se.mau.localzero.domain.User;
import se.mau.localzero.repository.UserRepository;

/**
 * A specific link in the registration chain.
 * Controls if the username is already taken in the database
 */

public class UserExistHandler extends RegistrationHandler{
    private final UserRepository userRepository;

    public UserExistHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean check(User user){
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username is already taken");
        }
        return super.check(user);
    }
}
