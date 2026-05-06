package se.mau.localzero.initiative.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.mau.localzero.domain.InitiativeLike;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface InitiativeLikeRepository extends JpaRepository<InitiativeLike, Long> {
    Optional<InitiativeLike> findByUserIdAndInitiativeId(Long userId, Long initiativeId);

    long countByInitiativeId(Long initiativeId);

    @Query("SELECT il.initiative.id FROM InitiativeLike il WHERE il.user.id = :userId")
    Set<Long> findLikedInitiativeIdsByUserId(@Param("userId") Long userId);

    @Query("SELECT il.initiative.id, COUNT(il) FROM InitiativeLike il WHERE il.initiative.id IN :ids GROUP BY il.initiative.id")
    List<Object[]> countLikesByInitiativeIds(@Param("ids") List<Long> ids);
}
