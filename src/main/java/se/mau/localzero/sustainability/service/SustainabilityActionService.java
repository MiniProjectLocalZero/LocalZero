package se.mau.localzero.sustainability.service;

import org.springframework.stereotype.Service;
import se.mau.localzero.domain.Category;
import se.mau.localzero.domain.SustainabilityAction;
import se.mau.localzero.domain.User;
import se.mau.localzero.sustainability.dto.SustainabilityActionDto;
import se.mau.localzero.sustainability.repository.SustainabilityActionRepository;

@Service
public class SustainabilityActionService {
    private final SustainabilityActionRepository repository;

    public SustainabilityActionService(SustainabilityActionRepository repository) {
        this.repository = repository;
    }

    public void logAction(SustainabilityActionDto dto, User currentUser) {
        String title = dto.getTitle();
        String description = dto.getDescription();
        Category category = dto.getCategory();
        double calculatedCarbonSaving = generateMockCarbonSaving(dto.getCategory());

        SustainabilityAction action = new SustainabilityAction(title, description, category, calculatedCarbonSaving,currentUser);

        // Save repo to database
        repository.save(action);
    }

    private double generateMockCarbonSaving(Category category) {
        // If user didn't choose a category, set value to 0
        if (category == null) return 0;

        switch (category) {
            case RIDE_SHARING:
                return 5;
            case TOOL_SHARING:
                return 2.5;
            case RECYCLING:
                return 0.5;
            case FOOD_SWAP:
                return 1.2;
            case GARDENING:
                return 0.8;
            default:
                return 0.0;
        }
    }
}
