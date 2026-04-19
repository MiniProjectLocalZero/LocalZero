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

    public void joinInitiative(Long initiativeId, User user) {
        Initiative initiative = initiativeRepository.findById(initiativeId)
                .orElseThrow(() -> new RuntimeException("Initiative not found"));

        if (initiative.getParticipants().contains(user)) {
            throw new RuntimeException("You are already a participant in this initiative");
        }

        initiative.addParticipant(user);
        initiativeRepository.save(initiative);
    }

    public void leaveInitiative(Long initiativeId, User user) {
        Initiative initiative = initiativeRepository.findById(initiativeId)
                .orElseThrow(() -> new RuntimeException("Initiative not found"));

        if (!initiative.getParticipants().contains(user)) {
            throw new RuntimeException("You are not a participant in this initiative");
        }

        initiative.removeParticipant(user);
        initiativeRepository.save(initiative);
    }
}
