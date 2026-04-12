package se.mau.localzero.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.mau.localzero.domain.User;

import java.util.Optional;

/**
 * Interface for database operations concerning the User-entity
 * Spring Data JPA automatically uses this during runtime
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
