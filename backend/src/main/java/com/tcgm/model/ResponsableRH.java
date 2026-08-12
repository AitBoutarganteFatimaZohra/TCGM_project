
package com.tcgm.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(name = "responsables_rh")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class ResponsableRH extends User {

    @Column(name = "departement", length = 100)
    private String departement;

    @Column(name = "fonction", length = 100)
    private String fonction;

    @Column(name = "date_embauche")
    private LocalDate dateEmbauche;
}

