package se.mau.localzero.initiative.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.mau.localzero.domain.Initiative;
import se.mau.localzero.domain.InitiativeComment;
import se.mau.localzero.domain.User;
import se.mau.localzero.initiative.repository.InitiativeCommentRepository;
import se.mau.localzero.initiative.repository.InitiativeRepository;

import java.util.List;

@Service
public class InitiativeCommentService {
    private final InitiativeCommentRepository initiativeCommentRepository;
    private final InitiativeRepository initiativeRepository;

    public InitiativeCommentService(InitiativeCommentRepository initiativeCommentRepository, InitiativeRepository initiativeRepository) {
        this.initiativeCommentRepository = initiativeCommentRepository;
        this.initiativeRepository = initiativeRepository;
    }

    @Transactional
    public InitiativeComment addComment(Long initiativeId, User author, String content) {
        Initiative initiative = initiativeRepository.findById(initiativeId)
                .orElseThrow(() -> new RuntimeException("Initiative not found"));

        InitiativeComment comment = new InitiativeComment(content, author, initiative);
        return initiativeCommentRepository.save(comment);
    }

    @Transactional(readOnly = true)
    public List<InitiativeComment> getCommentsByInitiative(Long initiativeId) {
        return initiativeCommentRepository.findByInitiativeIdOrderByCreatedAtDesc(initiativeId);
    }
}
