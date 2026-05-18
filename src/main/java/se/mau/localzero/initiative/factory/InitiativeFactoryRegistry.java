package se.mau.localzero.initiative.factory;

import org.springframework.stereotype.Component;
import se.mau.localzero.domain.Community;
import se.mau.localzero.domain.Initiative;
import se.mau.localzero.domain.User;
import se.mau.localzero.initiative.dto.InitiativeDto;

import java.util.List;

@Component
public class InitiativeFactoryRegistry {
    private final List<InitiativeCreator> creators;

    public InitiativeFactoryRegistry(List<InitiativeCreator> creators) {
        this.creators = creators;
    }

    public Initiative create(InitiativeDto dto, User creator, Community community) {
        return creators.stream()
                .filter(c -> c.supports(dto))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No initiative creator supports visibility: "
                        + (dto == null ? "null" : dto.getVisibility())))
                .create(dto, creator, community);
    }
}

