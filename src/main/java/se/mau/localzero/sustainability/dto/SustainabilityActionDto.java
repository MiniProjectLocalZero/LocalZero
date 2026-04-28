package se.mau.localzero.sustainability.dto;

import lombok.Getter;
import lombok.Setter;
import se.mau.localzero.domain.Category;

/**
 * Data Transfer Object (DTO) for creating initiatives.
 * Seperates frontend from database Initiative.
 */

@Getter
@Setter
public class SustainabilityActionDto {
    private String title;
    private String description;
    private Category category;
}
