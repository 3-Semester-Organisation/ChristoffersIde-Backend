package ccy.reactiveprogramingmonoandflux.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Country {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String countryId;
    private double probability;

    @ManyToMany(mappedBy = "countryList")
    private List<ProbableDemographicProfile> probableDemographicProfileList;

}
