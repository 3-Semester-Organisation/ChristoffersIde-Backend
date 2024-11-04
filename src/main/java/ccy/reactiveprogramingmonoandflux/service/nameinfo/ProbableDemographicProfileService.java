package ccy.reactiveprogramingmonoandflux.service.nameinfo;

import ccy.reactiveprogramingmonoandflux.dto.NameInfoResponse;

public interface ProbableDemographicProfileService {

    NameInfoResponse create(NameInfoResponse nameInfoResponse);
    NameInfoResponse getNameInfoResponse(String name);
    boolean doesExist(String name);

    void deleteProfile(String name);
}
