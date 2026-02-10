package org.example.model;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "ust10c")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)

public class ust10c {
    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="cli")
    private String client;

    @Column(name="comp_profile")
    private String compProfile;

    @Column(name="version")
    private String version;

    @Column(name="sing_profile")
    private String singProfile;



}






