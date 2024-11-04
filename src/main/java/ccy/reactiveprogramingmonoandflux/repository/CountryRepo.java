package ccy.reactiveprogramingmonoandflux.repository;

import ccy.reactiveprogramingmonoandflux.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryRepo extends JpaRepository<Country, Integer> {
}
