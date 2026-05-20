package se.mau.localzero.initiative.factory;

import se.mau.localzero.domain.Community;
import se.mau.localzero.domain.Initiative;
import se.mau.localzero.domain.User;
import se.mau.localzero.initiative.dto.InitiativeDto;

public interface InitiativeCreator {
    boolean supports(InitiativeDto dto);

    Initiative create(InitiativeDto dto, User creator, Community community);
}

