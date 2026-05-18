package se.mau.localzero.auth.handler;

import se.mau.localzero.domain.User;

/**
 * Abstract class for the Chain of Responsibility pattern.
 * Handles the logic and sends it to the next link in the chain if needed
 */
public abstract class RegistrationHandler {
    private RegistrationHandler next;

    /**
     * Connects this handler with the next handler
     * @param next This is the next step in the validation chain
     * @return Next handler to allow chain of responsibilities.
     */
    public RegistrationHandler setNext(RegistrationHandler next) {
        this.next = next;
        return next;
    }

    /**
     * A control for checking if we can go to the next part of the chain
     * (If needed)
     */
    public boolean check(User user) {
        if(next == null) {
            return true; //We finish the chain here if everything works
        }
        return next.check(user);
    }
}
