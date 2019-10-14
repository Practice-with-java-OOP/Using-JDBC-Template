package com.jidian.cosalon.migration.pos365.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Warehouse {

    private Long id;
    private Timestamp gmtCreate;
    private Timestamp gmtModified;
    private Integer version;
    private String name;
    private String address;
    private Long parentId;
    private Integer status;
}
