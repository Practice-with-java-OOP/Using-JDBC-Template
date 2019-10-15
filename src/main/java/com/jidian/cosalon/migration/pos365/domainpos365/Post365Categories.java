package com.jidian.cosalon.migration.pos365.domainpos365;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "p365_categories")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Post365Categories {
    @JsonProperty("Id")
    @Id
    private Long id;
    @JsonProperty("Name")
    private String name;
    @JsonProperty("RetailerId")
    private Long retailerId;
    @JsonProperty("CreatedDate")
    private Timestamp createdDate;
    @JsonProperty("CreatedBy")
    private Long createdBy;
}
