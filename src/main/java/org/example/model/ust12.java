package org.example.model;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "ust12")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)

public class ust12 {
    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="cli")
    private String client;

    @Column(name="auth_obj")
    private String authObj;


    @Column(name="usermaster_maint")
    private String usermasterMaint;

    @Column(name="version")
    private String version;

    @Column(name="auth_field")
    private String authField;

    @Column(name="low_field")
    private String low;

    @Column(name="high_field")
    private String high;
}






