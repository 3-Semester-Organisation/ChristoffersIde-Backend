package ccy.reactiveprogramingmonoandflux.service;

import ccy.reactiveprogramingmonoandflux.dto.NameInfoResponse;

public interface ProbableDemographicProfileService {

    NameInfoResponse create(NameInfoResponse nameInfoResponse);
    NameInfoResponse getNameInfoResponse(String name);
    boolean doesExist(String name);

    void deleteProfile(String name);
}
