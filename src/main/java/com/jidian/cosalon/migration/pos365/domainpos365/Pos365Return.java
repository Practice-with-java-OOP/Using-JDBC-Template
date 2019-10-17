package com.jidian.cosalon.migration.pos365.domainpos365;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "p365_return")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Pos365Return {
    @JsonProperty("Id")
    @Id
    private Long id;
    @JsonProperty("Code")
    private String code;
    @JsonProperty("BranchId")
    private Long branchId;
    @JsonProperty("CreatedDate")
    private String createdDate;
    @JsonProperty("CreatedBy")
    private Long createdBy;
    @JsonProperty("Status")
    private Long status;
    @JsonProperty("Description")
    private String description;
    @JsonProperty("RetailerId")
    private Long retailerId;
    @JsonProperty("OrderId")


    private Long orderId;
    @JsonProperty("ReturnDate")
    private String returnDate;


    @JsonProperty("PartnerId")
    private Long partnerId;
    @JsonProperty("ModifiedDate")
    private String modifiedDate;
    @JsonProperty("ModifiedBy")
    private Long modifiedBy;
    @JsonProperty("Total")
    private BigDecimal total;
    @JsonProperty("Discount")
    private BigDecimal discount;
    @JsonProperty("TotalPayment")
    private BigDecimal totalPayment;
    @JsonProperty("AccountingTransactions")
    @Transient
    private List<Object> accountingTransactions;
    @JsonProperty("ReturnDetails")
    @Transient
    private List<Object> returnDetails;
}
