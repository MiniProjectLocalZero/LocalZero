package se.mau.localzero.initiative.service;

import org.springframework.stereotype.Service;
import se.mau.localzero.auth.repository.UserRepository;
import se.mau.localzero.domain.Community;
import se.mau.localzero.domain.Initiative;
import se.mau.localzero.domain.User;
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
}
