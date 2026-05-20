package se.mau.localzero.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A simple DTO representing a user's basic information and their community.
 * Used for user selection in the messaging system.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryDTO {
    private Long id;
    private String username;
    private String communityName;
    private boolean isRepresentative;
}
