package ccy.reactiveprogramingmonoandflux.dto;

public record DietaryRequirement(
        boolean vegan,
        boolean vegetarian,
        boolean lactoseIntolerant,
        boolean glutenIntolerant
) {
}
