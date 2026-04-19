package se.mau.localzero.messaging.validator;

import org.springframework.stereotype.Service;
import se.mau.localzero.domain.Message;
import se.mau.localzero.messaging.exception.InvalidMessageException;

/**
 * Chain of responsibility pattern for message validation.
 * This class manages the validation chain, ensuring that messages are validated in the correct order.
 */
@Service
public class ValidationChain {

    private final ContentValidator contentValidator;
    private final UserAccessValidator userAccessValidator;
    private final CommunityValidator communityValidator;

    public ValidationChain(
            ContentValidator contentValidator,
            UserAccessValidator userAccessValidator,
            CommunityValidator communityValidator
    ) {
        this.contentValidator = contentValidator;
        this.userAccessValidator = userAccessValidator;
        this.communityValidator = communityValidator;

        buildChain();
    }

    private void buildChain() {
        contentValidator.setNext(userAccessValidator);
        userAccessValidator.setNext(communityValidator);
    }
    
    /**
     * Execute the entire validation chain
     * @return true if all validators pass
     * @throws InvalidMessageException if any validator fails
     */
    public boolean validateMessage(Message message) {
        return contentValidator.checkAndPassToNext(message);
    }
}