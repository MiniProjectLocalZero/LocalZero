package se.mau.localzero.initiative.factory;

import org.springframework.stereotype.Component;
import se.mau.localzero.domain.Initiative;
import se.mau.localzero.domain.User;
import se.mau.localzero.domain.Community;
import se.mau.localzero.initiative.dto.InitiativeDto;

/**
 * Factory for creating Initiative objects.
 * Uses InitiativeDto to map data from frontend to domain models
 */
@Component
public class InitiativeFactory {

    public Initiative createInitiative(InitiativeDto dto, User creator, Community community) {
        return new Initiative(
                dto.getTitle(),
                dto.getDescription(),
                dto.getStartDate(),
                dto.getEndDate(),
                dto.getCategory(),
                dto.getVisibility(),
                creator,
                community
        );
    }
}