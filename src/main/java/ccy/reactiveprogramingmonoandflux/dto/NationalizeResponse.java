package ccy.reactiveprogramingmonoandflux.dto;

import java.util.List;

public record NationalizeResponse(
        List<CountryDto> country
) {
}
