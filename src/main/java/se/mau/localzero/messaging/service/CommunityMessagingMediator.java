package se.mau.localzero.messaging.service;

import se.mau.localzero.domain.Message;
import se.mau.localzero.domain.User;

public interface CommunityMessagingMediator {

   boolean canSendMessage(User sender, User receiver);

   Message sendMessage(User sender, User receiver, String content);

   boolean requiresRepresentative(User sender, User receiver);
}
