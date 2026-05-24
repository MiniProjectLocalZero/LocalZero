package se.mau.localzero.initiative.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.mau.localzero.domain.Initiative;
import se.mau.localzero.domain.User;
import se.mau.localzero.initiative.repository.InitiativeRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class InitiativeLikeService {
    private final InitiativeRepository initiativeRepository;

    public InitiativeLikeService(InitiativeRepository initiativeRepository) {
        this.initiativeRepository = initiativeRepository;
    }

    @Transactional
    public void toggleLike(Long initiativeId, User user) {
        Initiative initiative = initiativeRepository.findById(initiativeId)
                .orElseThrow(() -> new RuntimeException("Initiative not found"));

        boolean alreadyLiked = initiative.getLikes().stream()
                .anyMatch(u -> u.getId().equals(user.getId()));

        if (alreadyLiked) {
            initiative.removeLike(user);
        } else {
            initiative.addLike(user);
        }
        initiativeRepository.save(initiative);
    }

    @Transactional(readOnly = true)
    public long countLikes(Long initiativeId) {
        Initiative initiative = initiativeRepository.findById(initiativeId)
                .orElseThrow(() -> new RuntimeException("Initiative not found"));
        return initiative.getLikes().size();
    }

    @Transactional(readOnly = true)
    public boolean hasUserLiked(Long initiativeId, Long userId) {
        Initiative initiative = initiativeRepository.findById(initiativeId)
                .orElseThrow(() -> new RuntimeException("Initiative not found"));
        return initiative.getLikes().stream()
                .anyMatch(u -> u.getId().equals(userId));
    }

    @Transactional(readOnly = true)
    public Set<Long> findLikedInitiativeIdsByUser(Long userId) {
        return initiativeRepository.findAll().stream()
                .filter(i -> i.getLikes().stream().anyMatch(u -> u.getId().equals(userId)))
                .map(Initiative::getId)
                .collect(Collectors.toSet());
    }

    @Transactional(readOnly = true)
    public Map<Long, Long> countLikesByInitiativeIds(List<Long> initiativeIds) {
        Map<Long, Long> map = new HashMap<>();
        for (Long id : initiativeIds) {
            Initiative initiative = initiativeRepository.findById(id).orElse(null);
            map.put(id, initiative != null ? (long) initiative.getLikes().size() : 0L);
        }
        return map;
    }
}
