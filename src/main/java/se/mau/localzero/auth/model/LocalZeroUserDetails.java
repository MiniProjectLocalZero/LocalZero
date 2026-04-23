package se.mau.localzero.auth.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import se.mau.localzero.domain.User;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Custom UserDetails implementation that wraps the LocalZero User entity.
 * This allows us to embed domain-specific information directly in the authentication principal,
 * avoiding the need to fetch the user again from the database in every controller method.
 */
public class LocalZeroUserDetails implements UserDetails {

    private final User user;

    public LocalZeroUserDetails(User user) {
        this.user = user;
    }

    /**
     * Returns the User entity associated with this authentication principal.
     * Allows easy access to the full User object with all its relationships.
     *
     * @return the User entity
     */
    public User getUser() {
        return user;
    }

    /**
     * Returns the user's ID from the wrapped User entity.
     * Useful for quick access without needing to call getUser().getId()
     *
     * @return the user's ID
     */
    public Long getUserId() {
        return user.getId();
    }

    /**
     * Returns the user's community ID.
     * Useful for community-specific operations.
     *
     * @return the community ID
     */
    public Long getCommunityId() {
        return user.getCommunity().getId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }
}