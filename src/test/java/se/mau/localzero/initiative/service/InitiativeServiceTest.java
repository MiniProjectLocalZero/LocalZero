package se.mau.localzero.initiative.service;

import org.junit.jupiter.api.Test;
import se.mau.localzero.domain.Community;
import se.mau.localzero.domain.Initiative;
import se.mau.localzero.domain.User;
import se.mau.localzero.initiative.dto.InitiativeDto;
import se.mau.localzero.initiative.factory.InitiativeFactoryRegistry;
import se.mau.localzero.initiative.repository.InitiativeRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
/*
class InitiativeServiceTest {

    @Test
    void saveNewInitiative_createsViaRegistryThenSaves() {
        InitiativeRepository repository = mock(InitiativeRepository.class);
        InitiativeFactoryRegistry registry = mock(InitiativeFactoryRegistry.class);
        InitiativeService service = new InitiativeService(repository, registry);

        InitiativeDto dto = new InitiativeDto();
        User creator = mock(User.class);
        Community community = mock(Community.class);
        Initiative created = mock(Initiative.class);

        when(registry.create(dto, creator, community)).thenReturn(created);
        when(repository.save(created)).thenReturn(created);

        Initiative result = service.saveNewInitiative(dto, creator, community);

        assertSame(created, result);
        verify(registry).create(dto, creator, community);
        verify(repository).save(created);
    }

    @Test
    void getAllInitiatives_returnsRepositoryResult() {
        InitiativeRepository repository = mock(InitiativeRepository.class);
        InitiativeFactoryRegistry registry = mock(InitiativeFactoryRegistry.class);
        InitiativeService service = new InitiativeService(repository, registry);

        List<Initiative> expected = List.of(mock(Initiative.class));
        when(repository.findAll()).thenReturn(expected);

        List<Initiative> result = service.getAllInitiatives();

        assertSame(expected, result);
        verify(repository).findAll();
    }
}

 */

