package se.mau.localzero.sustainability.strategy;

import se.mau.localzero.domain.SustainabilityAction;
import se.mau.localzero.domain.User;
import se.mau.localzero.sustainability.repository.SustainabilityActionRepository;

import java.util.List;

/**
 * Strategy for calculating community-wide sustainability impact.
 */
public class CommunityAnalyticsStrategy implements SustainabilityAnalyticsStrategy {
    private final SustainabilityActionRepository repository;

    public CommunityAnalyticsStrategy(SustainabilityActionRepository repository) {
        this.repository = repository;
    }

    @Override
    public double calculateTotalSavings(User user) {
        if (user == null || user.getCommunity() == null) {
            return 0.0;
        }
        List<SustainabilityAction> actions = repository.findByUser_Community(user.getCommunity());
        return actions.stream()
                .mapToDouble(SustainabilityAction::getCarbonSaving)
                .sum();
    }
}
