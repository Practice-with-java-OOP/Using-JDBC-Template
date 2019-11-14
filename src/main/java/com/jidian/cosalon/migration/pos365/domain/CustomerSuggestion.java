package com.jidian.cosalon.migration.pos365.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerSuggestion {
    private String customerName;
    private String phoneNumber;
    private int type;
    private Long referentialId;
}
