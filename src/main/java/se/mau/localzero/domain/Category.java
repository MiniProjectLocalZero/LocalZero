package se.mau.localzero.domain;

public enum Category {
    TOOL_SHARING("Sharing Tools"),
    FOOD_SWAP("Food Swap"),
    GARDENING("Gardening"),
    RECYCLING("Recycling"),
    RIDE_SHARING("Car Pool"),
    ENERGY_SAVING("Energy Saving"),
    LOCAL_CLEANUP("Local Cleanup"),
    SKILL_SHARING("Skill Sharing"),
    URBAN_FARMING("Urban Farming"),
    CLOTHES_SWAP("Clothing Swap"),
    REPAIR_CAFE("Repair Cafe"),
    COMPOSTING("Composting"),
    WATER_CONSERVATION("Water Saving"),
    WILDLIFE_PROTECTION("Wildlife Care"),
    ZERO_WASTE("Zero Waste");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
