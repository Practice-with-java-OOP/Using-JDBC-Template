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
@Table(name = "p365_transfers")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Pos365Transfer {

    @JsonProperty("Id")
    @Id
    private Long id;
    @JsonProperty("Code")
    private String code;
    @JsonProperty("FromBranchId")
    private Long fromBranchId;
    @JsonProperty("ToBranchId")
    private Long toBranchId;
    @JsonProperty("Status")
    private Integer status;
    @JsonProperty("DocumentDate")
    private String documentDate;
    @JsonProperty("CreatedDate")
    private String createdDate;
    @JsonProperty("CreatedBy")
    private Long createdBy;
    @JsonProperty("RetailerId")
    private Long retailerId;
    @JsonProperty("ModifiedBy")
    private String modifiedBy;
    @JsonProperty("ModifiedDate")
    private String modifiedDate;
    @JsonProperty("TransferDetails")
    @Transient
    private List<Object> transferDetails;
}
