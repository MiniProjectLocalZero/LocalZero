package se.mau.localzero.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) for user registration.
 * Increases security measures as it separates the frontend
 * from the database
 */

@Getter
@Setter
public class UserRegistrationDto {
    private String username;
    private String community;
    private String password;
}
