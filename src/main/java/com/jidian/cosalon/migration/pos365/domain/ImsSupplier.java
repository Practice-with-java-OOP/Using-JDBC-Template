package com.jidian.cosalon.migration.pos365.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImsSupplier {
    private Long id;
    private Timestamp gmtCreate;
    private Timestamp gmtModified;
    private Integer version;
    private String code;
    private String name;
    private String phoneNum;
    private String phoneNum2;
    private String address;
    private String address2;
    private Integer transactionQuantity;
    private BigDecimal transactionAmount;
    private Integer status;
    private String remarks;
}
