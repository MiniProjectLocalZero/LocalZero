package se.mau.localzero.initiative.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.mau.localzero.domain.InitiativeComment;

import java.util.List;

@Repository
public interface InitiativeCommentRepository extends JpaRepository<InitiativeComment, Long> {
    List<InitiativeComment> findByInitiativeIdOrderByCreatedAtDesc(Long initiativeId);
}
