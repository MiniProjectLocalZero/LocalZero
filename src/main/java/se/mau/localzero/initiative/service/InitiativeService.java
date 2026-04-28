package se.mau.localzero.initiative.service;

import org.springframework.stereotype.Service;
import se.mau.localzero.domain.Community;
import se.mau.localzero.domain.Initiative;
import se.mau.localzero.domain.User;
import se.mau.localzero.initiative.dto.InitiativeDto;
import se.mau.localzero.initiative.factory.InitiativeFactoryRegistry;
import se.mau.localzero.initiative.repository.InitiativeRepository;

import java.util.List;

//business logic, the factory and database is connected here

@Service
public class InitiativeService {
    private final InitiativeRepository initiativeRepository;
    private final InitiativeFactoryRegistry initiativeFactoryRegistry;

    public InitiativeService(InitiativeRepository repo, InitiativeFactoryRegistry initiativeFactoryRegistry) {
        this.initiativeRepository = repo;
        this.initiativeFactoryRegistry = initiativeFactoryRegistry;
    }

    public Initiative saveNewInitiative(InitiativeDto dto, User creator, Community community) {
        Initiative initiative = initiativeFactoryRegistry.create(dto, creator, community);
        return initiativeRepository.save(initiative);
    }

    public List<Initiative> getAllInitiatives() {
        return initiativeRepository.findAll();
    }
}
