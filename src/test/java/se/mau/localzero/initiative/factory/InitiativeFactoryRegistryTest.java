package se.mau.localzero.initiative.factory;

import org.junit.jupiter.api.Test;
import se.mau.localzero.domain.Community;
import se.mau.localzero.domain.Initiative;
import se.mau.localzero.domain.User;
import se.mau.localzero.initiative.dto.InitiativeDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
/*
class InitiativeFactoryRegistryTest {

    @Test
    void create_usesFirstSupportingCreator() {
        InitiativeCreator creatorA = mock(InitiativeCreator.class);
        InitiativeCreator creatorB = mock(InitiativeCreator.class);
        InitiativeFactoryRegistry registry = new InitiativeFactoryRegistry(List.of(creatorA, creatorB));

        InitiativeDto dto = new InitiativeDto();
        User user = mock(User.class);
        Community community = mock(Community.class);
        Initiative expected = mock(Initiative.class);

        when(creatorA.supports(dto)).thenReturn(false);
        when(creatorB.supports(dto)).thenReturn(true);
        when(creatorB.create(dto, user, community)).thenReturn(expected);

        Initiative result = registry.create(dto, user, community);

        assertSame(expected, result);
        verify(creatorA, never()).create(dto, user, community);
        verify(creatorB).create(dto, user, community);
    }

    @Test
    void create_throwsWhenNoCreatorSupportsRequest() {
        InitiativeCreator creator = mock(InitiativeCreator.class);
        InitiativeFactoryRegistry registry = new InitiativeFactoryRegistry(List.of(creator));
        InitiativeDto dto = new InitiativeDto();

        when(creator.supports(dto)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> registry.create(dto, mock(User.class), mock(Community.class)));
        verify(creator, never()).create(any(), any(), any());
    }
}

 */


