package com.jidian.cosalon.migration.pos365.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoodsReceipt {
    private Long id;
    private Integer type;
    private Integer importExport;
    private String receiptCode;
}
