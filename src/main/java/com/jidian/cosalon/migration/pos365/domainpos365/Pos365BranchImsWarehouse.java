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
@Table(name = "p365_branchs_ims_warehouse")
public class Pos365BranchImsWarehouse {
    @Id
    @Column(name = "p365_branchs_id")
    private Long p365BranchId;
    @Column(name = "ims_warehouse_id")
    private Long imsWarehouseId;
}
