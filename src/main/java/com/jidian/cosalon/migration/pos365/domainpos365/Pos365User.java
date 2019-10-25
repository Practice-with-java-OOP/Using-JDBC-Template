package com.jidian.cosalon.migration.pos365.domainpos365;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "p365_users")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Pos365User {

    @JsonProperty("Id")
    @Id
    private Long id;
    @JsonProperty("UserName")
    private String username;
    @JsonProperty("Name")
    private String name;
    @JsonProperty("CreatedDate")
    private String createdDate;
    @JsonProperty("IsActive")
    private Boolean isActive;
    @JsonProperty("IsAdmin")
    private Boolean isAdmin;
    @JsonProperty("RetailerId")
    private Long retailerId;
    @JsonProperty("Phone")
    private String phone;
    @JsonProperty("CreatedBy")
    private Long createdBy;
    @JsonProperty("AdminGroup")
    private Long adminGroup;
    @JsonProperty("AccountingTransactions")
    @Transient
    private List<Object> accountingTransactions;
    @JsonProperty("User1")
    @Transient
    private List<Object> user1;
    @JsonProperty("Partners")
    @Transient
    private List<Object> partners;
    @JsonProperty("CdKeys")
    @Transient
    private List<Object> cdKeys;
}
