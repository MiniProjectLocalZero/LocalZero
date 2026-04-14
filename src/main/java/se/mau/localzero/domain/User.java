package se.mau.localzero.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Entityclass representing the user in the system.
 * This class is connected to the User table in the PostgreSQL database
 */

@Entity
@Getter
@Setter
@Table(name = "localzero_users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false,  unique = true)
    private String email;

    @Column(nullable = false)
    private String community;

    @Column(nullable = false)
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"))
    private Set<UserRole> roles = new HashSet<>();

    protected User() {
    }

    public User(String username, String email, String community, String password) {
        this.username = username;
        this.email = email;
        this.community = community;
        this.password = password;
    }
}
