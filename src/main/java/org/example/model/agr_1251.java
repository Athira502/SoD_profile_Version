package org.example.model;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "agr_1251")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class agr_1251 {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="mandt")
    private String mandt;

    @Column(name="agr_name")
    private String agrName;

    @Column(name="counter")
    private Integer counter;

    @Column(name="object")
    private String object;

    @Column(name="auth")
    private String auth;

    @Column(name="variant")
    private String variant;

    @Column(name="field")
    private String field;

    @Column(name="low")
    private String low;

    @Column(name="high")
    private String high;

    @Column(name="modified")
    private String modified;

    @Column(name="deleted")
    private String deleted;

    @Column(name="copied")
    private String copied;

    @Column(name="neu")
    private String neu;

    @Column(name="node")
    private String node;

}

