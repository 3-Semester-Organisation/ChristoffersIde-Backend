package ccy.reactiveprogramingmonoandflux.service;

import ccy.reactiveprogramingmonoandflux.dto.CountryDto;
import ccy.reactiveprogramingmonoandflux.dto.NameInfoResponse;
import ccy.reactiveprogramingmonoandflux.entity.Country;
import ccy.reactiveprogramingmonoandflux.entity.ProbableDemographicProfile;
import ccy.reactiveprogramingmonoandflux.repository.CountryRepo;
import ccy.reactiveprogramingmonoandflux.repository.ProbableDemographicProfileRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProbableDemographicProfileServiceImpl implements ProbableDemographicProfileService{

    private final ProbableDemographicProfileRepo repo;
    private final CountryRepo countryRepo;

    @Override
    public NameInfoResponse create(NameInfoResponse nameInfoResponse) {

        //map the countryDtoList into the CountryEntity
        List<Country> countryList = nameInfoResponse.countryList().stream()
                .map(countryDto -> Country.builder()
                        .countryId(countryDto.country_id())
                        .probability(countryDto.probability())
                        .build())
                .map(countryRepo::save)
                .toList();


        //map the NameInfoResponse into probableDemographicProfile
        ProbableDemographicProfile probableDemographicProfile = ProbableDemographicProfile.builder()
                .name(nameInfoResponse.name())
                .gender(nameInfoResponse.gender())
                .genderProbability(nameInfoResponse.genderProbability())
                .age(nameInfoResponse.age())
                .countryList(countryList)
                .createdDate(LocalDateTime.now())
                .build();

        //save for caching
        ProbableDemographicProfile response = repo.save(probableDemographicProfile);

        return new NameInfoResponse(
                response.getName(),
                response.getGender(),
                response.getGenderProbability(),
                response.getAge(),
                response.getCountryList().stream()
                        .map(country -> new CountryDto(country.getCountryId(), country.getProbability()))
                        .toList()
        );
    }

    @Override
    public NameInfoResponse getNameInfoResponse(String name) {

        ProbableDemographicProfile profile = repo.findByName(name).orElseThrow();

        return new NameInfoResponse(
                profile.getName(),
                profile.getGender(),
                profile.getGenderProbability(),
                profile.getAge(),
                profile.getCountryList().stream()
                        .map(country -> new CountryDto(country.getCountryId(), country.getProbability()))
                        .toList()
        );
    }

    @Override
    public boolean doesExist(String name) {
        return repo.existsByName(name);
    }

    @Scheduled(fixedRate = 86400000)
    @Override
    public void deleteProfile(String name) {
        LocalDateTime threshold = LocalDateTime.now().minus(1, ChronoUnit.DAYS);
        repo.deleteByCreatedDateBefore(threshold);
    }
}
