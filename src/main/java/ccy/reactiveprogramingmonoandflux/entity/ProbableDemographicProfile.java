package ccy.reactiveprogramingmonoandflux.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;


@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProbableDemographicProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;
    private String gender;
    private double genderProbability;
    private int age;
    private LocalDateTime createdDate;

    @ManyToMany(cascade = CascadeType.REMOVE)
    @JoinTable(
            name = "profile_country", // Name of the join table
            joinColumns = @JoinColumn(name = "profile_id"), // Column in the join table referencing ProbableDemographicProfile
            inverseJoinColumns = @JoinColumn(name = "country_id") // Column in the join table referencing Country
    )
    private List<Country> countryList;
}
