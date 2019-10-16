package com.jidian.cosalon.migration.pos365.domainpos365;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Pos365ProductOnHandByBranchId implements Serializable {
    private Long productId;
    private Long branchId;
}
