package se.mau.localzero.sustainability.repository;

// Handles communication with the database for sustainability actions

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.mau.localzero.domain.SustainabilityAction;
import se.mau.localzero.domain.User;

import java.util.List;

@Repository
public interface SustainabilityActionRepository extends JpaRepository<SustainabilityAction, Long> {
    List<SustainabilityAction> findByUser(User user);
}
