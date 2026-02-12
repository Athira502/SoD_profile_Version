package org.example.model;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "agr_define")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)

public class agr_define {
    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="mandt")
    private String mandt;

    @Column(name="role")
    private String roleName;


}






