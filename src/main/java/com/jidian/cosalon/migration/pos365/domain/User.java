package com.jidian.cosalon.migration.pos365.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long id;

    private int version;

    private String username;

    private String phoneNum;

    private String nickName;
}
