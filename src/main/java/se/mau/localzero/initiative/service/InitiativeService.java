package se.mau.localzero.initiative.service;

import org.springframework.stereotype.Service;
import se.mau.localzero.domain.Initiative;
import se.mau.localzero.domain.User;
import se.mau.localzero.domain.Community;
import se.mau.localzero.initiative.dto.InitiativeDto;
import se.mau.localzero.initiative.factory.InitiativeFactory;
import se.mau.localzero.initiative.repository.InitiativeRepository;

import java.util.List;

//business logic, the factory and database is connected here

@Service
public class InitiativeService {
    private final InitiativeRepository initiativeRepository;
    private final InitiativeFactory initiativeFactory;

    public InitiativeService(InitiativeRepository repo, InitiativeFactory factory) {
        this.initiativeRepository = repo;
        this.initiativeFactory = factory;
    }

    public Initiative saveNewInitiative(InitiativeDto dto, User creator, Community community) {
        Initiative initiative = initiativeFactory.createInitiative(dto, creator, community);
        return initiativeRepository.save(initiative);
    }

    public List<Initiative> getAllInitiatives() {
        return initiativeRepository.findAll();
    }
}
