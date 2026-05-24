package se.mau.localzero.initiative.service;

import org.junit.jupiter.api.Test;
import se.mau.localzero.auth.repository.UserRepository;
import se.mau.localzero.domain.Community;
import se.mau.localzero.domain.Initiative;
import se.mau.localzero.domain.User;
import se.mau.localzero.domain.UserRole;
import se.mau.localzero.initiative.dto.InitiativeDto;
import se.mau.localzero.initiative.factory.InitiativeFactoryRegistry;
import se.mau.localzero.initiative.repository.InitiativeRepository;
import se.mau.localzero.messaging.mediator.NotificationMediator;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class InitiativeServiceTest {

    @Test
    void deleteInitiative_authorizedAsCreator_deletesSuccessfully() {
        InitiativeRepository repository = mock(InitiativeRepository.class);
        InitiativeFactoryRegistry registry = mock(InitiativeFactoryRegistry.class);
        NotificationMediator notificationMediator = mock(NotificationMediator.class);
        UserRepository userRepository = mock(UserRepository.class);
        InitiativeService service = new InitiativeService(repository, registry, notificationMediator, userRepository);

        User creator = mock(User.class);
        when(creator.getId()).thenReturn(1L);

        Initiative initiative = mock(Initiative.class);
        when(initiative.getCreatedBy()).thenReturn(creator);
        when(repository.findById(10L)).thenReturn(Optional.of(initiative));

        service.deleteInitiative(10L, creator);

        verify(repository).delete(initiative);
    }

    @Test
    void deleteInitiative_authorizedAsRepresentative_deletesSuccessfully() {
        InitiativeRepository repository = mock(InitiativeRepository.class);
        InitiativeFactoryRegistry registry = mock(InitiativeFactoryRegistry.class);
        NotificationMediator notificationMediator = mock(NotificationMediator.class);
        UserRepository userRepository = mock(UserRepository.class);
        InitiativeService service = new InitiativeService(repository, registry, notificationMediator, userRepository);

        Community community = mock(Community.class);
        when(community.getId()).thenReturn(5L);

        User creator = mock(User.class);
        when(creator.getId()).thenReturn(1L);

        User representative = mock(User.class);
        when(representative.getId()).thenReturn(2L);
        when(representative.getRoles()).thenReturn(Set.of(UserRole.REPRESENTATIVE));
        when(representative.getCommunity()).thenReturn(community);

        Initiative initiative = mock(Initiative.class);
        when(initiative.getCreatedBy()).thenReturn(creator);
        when(initiative.getCommunity()).thenReturn(community);
        when(repository.findById(10L)).thenReturn(Optional.of(initiative));

        service.deleteInitiative(10L, representative);

        verify(repository).delete(initiative);
    }

    @Test
    void deleteInitiative_unauthorized_throwsException() {
        InitiativeRepository repository = mock(InitiativeRepository.class);
        InitiativeFactoryRegistry registry = mock(InitiativeFactoryRegistry.class);
        NotificationMediator notificationMediator = mock(NotificationMediator.class);
        UserRepository userRepository = mock(UserRepository.class);
        InitiativeService service = new InitiativeService(repository, registry, notificationMediator, userRepository);

        User creator = mock(User.class);
        when(creator.getId()).thenReturn(1L);

        User otherUser = mock(User.class);
        when(otherUser.getId()).thenReturn(2L);
        when(otherUser.getRoles()).thenReturn(Set.of(UserRole.RESIDENT));

        Initiative initiative = mock(Initiative.class);
        when(initiative.getCreatedBy()).thenReturn(creator);
        when(repository.findById(10L)).thenReturn(Optional.of(initiative));

        assertThrows(RuntimeException.class, () -> service.deleteInitiative(10L, otherUser));
        verify(repository, never()).delete(any());
    }

    @Test
    void toggleOfficialStatus_authorizedAsRepresentative_togglesSuccessfully() {
        InitiativeRepository repository = mock(InitiativeRepository.class);
        InitiativeFactoryRegistry registry = mock(InitiativeFactoryRegistry.class);
        NotificationMediator notificationMediator = mock(NotificationMediator.class);
        UserRepository userRepository = mock(UserRepository.class);
        InitiativeService service = new InitiativeService(repository, registry, notificationMediator, userRepository);

        Community community = mock(Community.class);
        when(community.getId()).thenReturn(5L);

        User representative = mock(User.class);
        when(representative.getRoles()).thenReturn(Set.of(UserRole.REPRESENTATIVE));
        when(representative.getCommunity()).thenReturn(community);

        Initiative initiative = mock(Initiative.class);
        when(initiative.getCommunity()).thenReturn(community);
        when(initiative.isOfficial()).thenReturn(false);
        when(repository.findById(10L)).thenReturn(Optional.of(initiative));

        service.toggleOfficialStatus(10L, representative);

        verify(initiative).setOfficial(true);
        verify(repository).save(initiative);
    }

    @Test
    void toggleOfficialStatus_unauthorized_throwsException() {
        InitiativeRepository repository = mock(InitiativeRepository.class);
        InitiativeFactoryRegistry registry = mock(InitiativeFactoryRegistry.class);
        NotificationMediator notificationMediator = mock(NotificationMediator.class);
        UserRepository userRepository = mock(UserRepository.class);
        InitiativeService service = new InitiativeService(repository, registry, notificationMediator, userRepository);

        User resident = mock(User.class);
        when(resident.getRoles()).thenReturn(Set.of(UserRole.RESIDENT));

        Initiative initiative = mock(Initiative.class);
        when(repository.findById(10L)).thenReturn(Optional.of(initiative));

        assertThrows(RuntimeException.class, () -> service.toggleOfficialStatus(10L, resident));
        verify(repository, never()).save(any());
    }
}
