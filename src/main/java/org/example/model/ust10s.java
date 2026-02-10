package org.example.model;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "ust10s")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)

public class ust10s {
    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="cli")
    private String client;

    @Column(name="profile")
    private String profile;

    @Column(name="version")
    private String version;

    @Column(name="auth_obj")
    private String authObj;

    @Column(name="usermaster_maint")
    private String usermasterMaint;

}






