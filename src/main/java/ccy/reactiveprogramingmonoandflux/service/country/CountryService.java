package ccy.reactiveprogramingmonoandflux.service.country;

import ccy.reactiveprogramingmonoandflux.entity.Country;
import ccy.reactiveprogramingmonoandflux.repository.CountryRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CountryService {

    private final CountryRepo repo;

    public void createCountry(Country country){
        repo.save(country);
    }
}
