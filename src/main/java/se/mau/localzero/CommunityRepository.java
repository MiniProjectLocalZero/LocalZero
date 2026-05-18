package se.mau.localzero;

import org.springframework.data.jpa.repository.JpaRepository;
import se.mau.localzero.domain.Community;

import java.util.Optional;

public interface CommunityRepository extends JpaRepository<Community, Long> {
    Optional<Community> findFirstByName(String name);
}
