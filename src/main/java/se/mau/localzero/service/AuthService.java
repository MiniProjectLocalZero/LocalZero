package se.mau.localzero.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import se.mau.localzero.domain.User;
import se.mau.localzero.domain.UserRole;
import se.mau.localzero.handler.RegistrationHandler;
import se.mau.localzero.handler.UserExistHandler;
import se.mau.localzero.handler.ValidationHandler;
import se.mau.localzero.repository.UserRepository;

/**
 * Service for handling authentication business logic
 * Builds the Chain of Responsibilties for new users,
 */
@Service
public class AuthService {
    public final UserRepository userRepository;
    public final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Tries to register a new user by running the validation chain.
     * @param username User chosen Username
     * @param community User chosen Community
     * @param unhashedPass Plaintext password
     */
    public void registerNewUser(String username, String community, String unhashedPass) {
        User newUser = new User(username, community, unhashedPass);

        RegistrationHandler validation = new ValidationHandler();
        RegistrationHandler checkExist = new UserExistHandler(userRepository);

        /**
         * Here we tell validation that the next part of the chain is checkExist
         */
        validation.setNext(checkExist);

        if (validation.check(newUser)) {
            newUser.setPassword(passwordEncoder.encode(unhashedPass));

            newUser.getRoles().add(UserRole.USER);

            userRepository.save(newUser);
        }
    }
}
