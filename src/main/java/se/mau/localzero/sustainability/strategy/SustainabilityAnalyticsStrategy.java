package se.mau.localzero.sustainability.strategy;

import se.mau.localzero.domain.User;

/**
 * Strategy interface for calculating sustainability analytics.
 */
public interface SustainabilityAnalyticsStrategy {
    /**
     * Calculates the total carbon savings for a given context.
     * 
     * @param user The user for whom to calculate savings (used as personal reference or community reference).
     * @return The total carbon savings.
     */
    double calculateTotalSavings(User user);
}
