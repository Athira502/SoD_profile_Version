package org.example.model;

import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "ust04")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)

public class ust04 {
    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="mandt")
    private String mandt;

    @Column(name="b_name")
    private String bName;


    @Column(name="profile")
    private String profile;
}





