package ccy.reactiveprogramingmonoandflux.repository;

import ccy.reactiveprogramingmonoandflux.entity.ProbableDemographicProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ProbableDemographicProfileRepo extends JpaRepository<ProbableDemographicProfile, Integer> {
    Optional<ProbableDemographicProfile> findByName(String name);
    boolean existsByName(String name);
    void deleteByCreatedDateBefore(LocalDateTime dateTime);
}
