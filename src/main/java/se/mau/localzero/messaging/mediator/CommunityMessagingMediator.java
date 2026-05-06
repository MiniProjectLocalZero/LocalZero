package se.mau.localzero.messaging.mediator;

import se.mau.localzero.domain.User;

public interface CommunityMessagingMediator {

   void sendMessage(User sender, User receiver, String content);
}
