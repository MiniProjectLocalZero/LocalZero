package se.mau.localzero.initiative.factory;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import se.mau.localzero.domain.Community;
import se.mau.localzero.domain.Initiative;
import se.mau.localzero.domain.User;
import se.mau.localzero.domain.Visibility;
import se.mau.localzero.initiative.dto.InitiativeDto;

@Component
@Order(1)
public class PublicInitiativeCreator implements InitiativeCreator {

    @Override
    public boolean supports(InitiativeDto dto) {
        return dto != null && dto.getVisibility() == Visibility.PUBLIC;
    }

    @Override
    public Initiative create(InitiativeDto dto, User creator, Community community) {
        return new Initiative(
                dto.getTitle(),
                dto.getDescription(),
                dto.getStartDate(),
                dto.getEndDate(),
                dto.getCategory(),
                Visibility.PUBLIC,
                creator,
                community
        );
    }
}

