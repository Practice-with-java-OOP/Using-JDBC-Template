package com.jidian.cosalon.migration.pos365.domainpos365;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "p365_products_ims_chemical")
public class Pos365ProductImsChemical {
    @Id
    @Column(name = "p365_products_id")
    protected Long p365ProductId;
    @Column(name = "ims_chemical_id")
    protected Long imsChemicalId;
}
