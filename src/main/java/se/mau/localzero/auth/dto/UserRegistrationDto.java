package se.mau.localzero.auth.dto;

import lombok.Getter;
import lombok.Setter;
import se.mau.localzero.domain.UserRole;

import java.util.Set;

/**
 * Data Transfer Object (DTO) for user registration.
 * Increases security measures as it separates the frontend
 * from the database
 */

@Getter
@Setter
public class UserRegistrationDto {
    private String username;
    private String email;
    private String community;
    private String password;
    private Set<UserRole> roles;
}
