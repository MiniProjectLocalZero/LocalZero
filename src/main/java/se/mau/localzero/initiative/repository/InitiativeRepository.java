package se.mau.localzero.initiative.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.mau.localzero.domain.Initiative;
import se.mau.localzero.domain.User;

import java.util.List;
import java.util.Set;

/**
 * Repository interface for Initiative entity.
 * Provides methods to retrieve initiatives and query relationships between users and initiatives.
 */
@Repository
public interface InitiativeRepository extends JpaRepository<Initiative, Long> {

    List<Initiative> findByCommunityId(Long communityId);

    /**
     * Find all initiatives that both users are participants in.
     * Used to determine if users from different communities can communicate.
     *
     * @param user1 The first user
     * @param user2 The second user
     * @return Set of shared initiatives
     */
    @Query("SELECT i FROM Initiative i WHERE :user1 MEMBER OF i.participants AND :user2 MEMBER OF i.participants")
    Set<Initiative> findSharedInitiatives(@Param("user1") User user1, @Param("user2") User user2);
}
