package ccy.reactiveprogramingmonoandflux.dto;

import java.util.List;

public record UserSpecifications(
        List<String> ingredients,
        DietaryRequirement dietaryRequirements
) {
}
