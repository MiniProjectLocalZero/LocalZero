package se.mau.localzero.initiative.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.mau.localzero.domain.Initiative;
import se.mau.localzero.domain.InitiativeLike;
import se.mau.localzero.domain.User;
import se.mau.localzero.initiative.repository.InitiativeLikeRepository;
import se.mau.localzero.initiative.repository.InitiativeRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class InitiativeLikeService {
    private final InitiativeLikeRepository initiativeLikeRepository;
    private final InitiativeRepository initiativeRepository;

    public InitiativeLikeService(InitiativeLikeRepository initiativeLikeRepository, InitiativeRepository initiativeRepository) {
        this.initiativeLikeRepository = initiativeLikeRepository;
        this.initiativeRepository = initiativeRepository;
    }

    @Transactional
    public void toggleLike(Long initiativeId, User user) {
        Initiative initiative = initiativeRepository.findById(initiativeId)
                .orElseThrow(() -> new RuntimeException("Initiative not found"));

        initiativeLikeRepository.findByUserIdAndInitiativeId(user.getId(), initiativeId)
                .ifPresentOrElse(
                        initiativeLikeRepository::delete,
                        () -> initiativeLikeRepository.save(new InitiativeLike(user, initiative))
                );
    }

    @Transactional(readOnly = true)
    public long countLikes(Long initiativeId) {
        return initiativeLikeRepository.countByInitiativeId(initiativeId);
    }

    @Transactional(readOnly = true)
    public boolean hasUserLiked(Long initiativeId, Long userId) {
        return initiativeLikeRepository.findByUserIdAndInitiativeId(userId, initiativeId).isPresent();
    }

    @Transactional(readOnly = true)
    public Set<Long> findLikedInitiativeIdsByUser(Long userId) {
        return initiativeLikeRepository.findLikedInitiativeIdsByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Map<Long, Long> countLikesByInitiativeIds(List<Long> initiativeIds) {
        List<Object[]> results = initiativeLikeRepository.countLikesByInitiativeIds(initiativeIds);
        Map<Long, Long> map = new HashMap<>();
        for (Object[] result : results) {
            map.put((Long) result[0], (Long) result[1]);
        }
        for (Long id : initiativeIds) {
            map.putIfAbsent(id, 0L);
        }
        return map;
    }
}
