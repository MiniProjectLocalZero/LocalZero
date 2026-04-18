package se.mau.localzero.initiative.dto;

import lombok.Getter;
import lombok.Setter;
import se.mau.localzero.domain.Category;
import se.mau.localzero.domain.Visibility;
import java.time.LocalDate;

/**
 * Data Transfer Object (DTO) for creating initiatives.
 * Seperates frontend from database Initiative.
 */

@Getter
@Setter
public class InitiativeDto {
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Category category;
    private Visibility visibility;

    private String communityName;
}