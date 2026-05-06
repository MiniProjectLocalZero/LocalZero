package se.mau.localzero.messaging.mediator;

import se.mau.localzero.domain.User;

public interface CommunityMessagingMediator {

   boolean sendMessage(User sender, User receiver, String content);
}
