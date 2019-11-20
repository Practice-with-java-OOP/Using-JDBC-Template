package com.jidian.cosalon.migration.pos365.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AmsAccount {
    private Long id;

    private int version;

    private String accountName;

    private int accountStatus;

    private int accountType;

    private BigDecimal balance;

    private Long userId;

    private String username;
}
