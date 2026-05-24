package se.mau.localzero.sustainability.strategy;

import se.mau.localzero.domain.SustainabilityAction;
import se.mau.localzero.domain.User;
import se.mau.localzero.sustainability.repository.SustainabilityActionRepository;

import java.util.List;

/**
 * Strategy for calculating personal sustainability impact.
 */
public class PersonalAnalyticsStrategy implements SustainabilityAnalyticsStrategy {
    private final SustainabilityActionRepository repository;

    public PersonalAnalyticsStrategy(SustainabilityActionRepository repository) {
        this.repository = repository;
    }

    @Override
    public double calculateTotalSavings(User user) {
        if (user == null) {
            return 0.0;
        }
        List<SustainabilityAction> actions = repository.findByUser(user);
        return actions.stream()
                .mapToDouble(SustainabilityAction::getCarbonSaving)
                .sum();
    }
}
