package se.mau.localzero.profile.mediator;

import se.mau.localzero.profile.dto.ProfileDTO;

public interface ProfileMediator {
    ProfileDTO getUserProfile(Long userId);
    void updateUserProfile(Long userId, ProfileDTO profileDTO);
    void userJoinInitiative(Long userId, Long initiativeId);
    void userLeaveInitiative(Long userId, Long initiativeId);
    int getUserPostCount(Long userId);
    int getUserInitiativeCount(Long userId);
}

