package se.mau.localzero.initiative.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import se.mau.localzero.domain.Initiative;
import java.util.List;

//handles communication with the database for initiatives

@Repository
public interface InitiativeRepository extends JpaRepository<Initiative, Long> {

    //gets public initiative or initiatives from the same community
    @Query("SELECT i FROM Initiative i WHERE i.visibility = 'PUBLIC' OR i.community.id = :communityId")
    List<Initiative> findVisibleInitiatives(Long communityId);
}
