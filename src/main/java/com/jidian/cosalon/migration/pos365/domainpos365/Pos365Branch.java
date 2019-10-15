package com.jidian.cosalon.migration.pos365.domainpos365;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.List;

/**
 * script tao bang:
 create table p365_branchs (
     id bigint not null,
     address varchar(255),
     created_by bigint,
     created_date varchar(255),
     modified_by bigint,
     modified_date varchar(255),
     name varchar(255),
     online bit,
     retailer_id bigint,
     primary key (id)
 ) engine=InnoDB
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "p365_branchs")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Pos365Branch {
    @JsonProperty("Id")
    @Id
    private Long id;
    @JsonProperty("Name")
    private String name;
    @JsonProperty("Address")
    private String address;
    @JsonProperty("RetailerId")
    private Long retailerId;
    @JsonProperty("CreatedDate")
    private String createdDate;
    @JsonProperty("CreatedBy")
    private Long createdBy;
    @JsonProperty("ModifiedBy")
    private Long modifiedBy;
    @JsonProperty("ModifiedDate")
    private String modifiedDate;
    @JsonProperty("IsOnline")
    private Boolean online;
    @JsonProperty("PrintTemplates")
    @Transient
    private List<Object> printTemplates;
    @JsonProperty("Transfers")
    @Transient
    private List<Object> transfers;
    @JsonProperty("Transfers1")
    @Transient
    private List<Object> transfers1;
}
