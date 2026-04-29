package se.mau.localzero.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.mau.localzero.domain.Community;
import se.mau.localzero.domain.User;

import java.util.List;
import java.util.Optional;

/**
 * Interface for database operations concerning the User-entity
 * Spring Data JPA automatically uses this during runtime
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<List<User>> findByCommunity(Community community);
}
