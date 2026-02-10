package org.example.model;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "agr_prof")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)

public class agr_prof {
    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="mandt")
    private String mandt;

    @Column(name="role")
    private String roleName;

    @Column(name="lang")
    private String language;

    @Column(name="profile")
    private String profile;

    @Column(name="text")
    private String text;
}






