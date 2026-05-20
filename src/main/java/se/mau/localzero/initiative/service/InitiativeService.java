package se.mau.localzero.initiative.service;

import org.springframework.stereotype.Service;
import se.mau.localzero.auth.repository.UserRepository;
import se.mau.localzero.domain.Community;
import se.mau.localzero.domain.Initiative;
import se.mau.localzero.domain.User;
import se.mau.localzero.domain.UserRole;
import se.mau.localzero.initiative.dto.InitiativeDto;
import se.mau.localzero.initiative.factory.InitiativeFactoryRegistry;
import se.mau.localzero.initiative.repository.InitiativeRepository;
import se.mau.localzero.messaging.mediator.NotificationMediator;

import java.util.List;
import java.util.stream.Collectors;

//business logic, the factory and database is connected here

@Service
public class InitiativeService {
    private final InitiativeRepository initiativeRepository;
    private final InitiativeFactoryRegistry initiativeFactoryRegistry;
    private final NotificationMediator notificationMediator;
    private final UserRepository userRepository;

    public InitiativeService(InitiativeRepository repo, InitiativeFactoryRegistry initiativeFactoryRegistry, NotificationMediator notificationMediator, UserRepository userRepository) {
        this.initiativeRepository = repo;
        this.initiativeFactoryRegistry = initiativeFactoryRegistry;
        this.notificationMediator = notificationMediator;
        this.userRepository = userRepository;
    }

    public Initiative saveNewInitiative(InitiativeDto dto, User creator, Community community) {
        Initiative initiative = initiativeFactoryRegistry.create(dto, creator, community);
        
        // Auto-join creator
        initiative.addParticipant(creator);
        
        Initiative savedInitiative = initiativeRepository.save(initiative);
        List<User> recipients = userRepository.findByCommunity(community).orElse(List.of())
                .stream()
                .filter(user -> !user.getId().equals(creator.getId()))
                .collect(Collectors.toList());

        if (!recipients.isEmpty()) {
            notificationMediator.sendInitiativeNotification(recipients, savedInitiative);
        }
        return savedInitiative;
    }

    public List<Initiative> getAllInitiatives() {
        return initiativeRepository.findAll();
    }

    public void deleteInitiative(Long id, User currentUser) {
        Initiative initiative = initiativeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Initiative not found"));

        boolean isCreator = initiative.getCreatedBy().getId().equals(currentUser.getId());
        boolean isRepresentativeInSameCommunity = currentUser.getRoles().contains(UserRole.REPRESENTATIVE)
                && initiative.getCommunity().getId().equals(currentUser.getCommunity().getId());

        if (isCreator || isRepresentativeInSameCommunity) {
            initiativeRepository.delete(initiative);
        } else {
            throw new RuntimeException("You are not authorized to delete this initiative");
        }
    }

    public void toggleOfficialStatus(Long id, User currentUser) {
        Initiative initiative = initiativeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Initiative not found"));

        boolean isRepresentativeInSameCommunity = currentUser.getRoles().contains(UserRole.REPRESENTATIVE)
                && initiative.getCommunity().getId().equals(currentUser.getCommunity().getId());

        if (isRepresentativeInSameCommunity) {
            initiative.setOfficial(!initiative.isOfficial());
            initiativeRepository.save(initiative);
        } else {
            throw new RuntimeException("Only representatives of this community can change official status");
        }
    }
}
