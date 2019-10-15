package com.jidian.cosalon.migration.pos365.domainpos365;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "p365_order_stock")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Post365OrderStock {
    @JsonProperty("Id")
    @Id
    private Long id;
    @JsonProperty("Code")
    private String code;
    @JsonProperty("DocumentDate")
    private Timestamp documentDate;
    @JsonProperty("BranchId")
    private Long branchId;
    @JsonProperty("Status")
    private int status;
    @JsonProperty("ModifiedDate")
    private Timestamp modifiedDate;
    @JsonProperty("RetailerId")
    private Long retailerId;
    @JsonProperty("Discount")
    private BigDecimal discount;
    @JsonProperty("CreatedDate")
    private Timestamp createdDate;
    @JsonProperty("CreatedBy")
    private Long createdBy;
    @JsonProperty("ModifiedBy")
    private Long modifiedBy;
    @JsonProperty("Total")
    private BigDecimal total;
    @JsonProperty("TotalPayment")
    private BigDecimal totalPayment;
    @JsonProperty("AccountId")
    private Long accountId;
    @JsonProperty("ExchangeRate")
    private BigDecimal exchangeRate;
    @JsonProperty("DeliveryDate")
    private Timestamp deliveryDate;
    @JsonProperty("VAT")
    private BigDecimal vat;
}
