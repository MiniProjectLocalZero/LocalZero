package se.mau.localzero.initiative.factory;

import org.springframework.stereotype.Component;
import se.mau.localzero.domain.Community;
import se.mau.localzero.domain.Initiative;
import se.mau.localzero.domain.User;
import se.mau.localzero.initiative.dto.InitiativeDto;

/**
 * Backward-compatible facade for initiative creation.
 * Delegates selection to strategy-based creators in InitiativeFactoryRegistry.
 */
@Component
public class InitiativeFactory {
    private final InitiativeFactoryRegistry registry;

    public InitiativeFactory(InitiativeFactoryRegistry registry) {
        this.registry = registry;
    }

    public Initiative createInitiative(InitiativeDto dto, User creator, Community community) {
        return registry.create(dto, creator, community);
    }
}