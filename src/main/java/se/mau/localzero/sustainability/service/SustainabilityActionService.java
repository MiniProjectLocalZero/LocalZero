package se.mau.localzero.sustainability.service;

import org.springframework.stereotype.Service;
import se.mau.localzero.domain.Category;
import se.mau.localzero.domain.SustainabilityAction;
import se.mau.localzero.domain.User;
import se.mau.localzero.sustainability.dto.SustainabilityActionDto;
import se.mau.localzero.sustainability.repository.SustainabilityActionRepository;
import se.mau.localzero.sustainability.strategy.CommunityAnalyticsStrategy;
import se.mau.localzero.sustainability.strategy.PersonalAnalyticsStrategy;
import se.mau.localzero.sustainability.strategy.SustainabilityAnalyticsStrategy;

@Service
public class SustainabilityActionService {
    private final SustainabilityActionRepository repository;
    private final SustainabilityAnalyticsStrategy personalStrategy;
    private final SustainabilityAnalyticsStrategy communityStrategy;

    public SustainabilityActionService(SustainabilityActionRepository repository) {
        this.repository = repository;
        this.personalStrategy = new PersonalAnalyticsStrategy(repository);
        this.communityStrategy = new CommunityAnalyticsStrategy(repository);
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

    public double getPersonalImpact(User user) {
        return personalStrategy.calculateTotalSavings(user);
    }

    public double getCommunityImpact(User user) {
        return communityStrategy.calculateTotalSavings(user);
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
