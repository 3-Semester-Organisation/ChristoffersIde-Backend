package ccy.reactiveprogramingmonoandflux.dto;

import java.util.List;

public record NameInfoResponse(
        String name,
        String gender,
        double genderProbability,
        int age,
        List<CountryDto> countryList
) {
}