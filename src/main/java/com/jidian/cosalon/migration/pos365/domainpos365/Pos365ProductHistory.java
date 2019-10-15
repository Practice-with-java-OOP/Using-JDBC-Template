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

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "p365_products_history")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Pos365ProductHistory {
    @JsonProperty("Id")
    @Id
    private Long id;
    @JsonProperty("ProductId")
    private Long productId;
    @JsonProperty("Quantity")
    private Long quantity;
    @JsonProperty("EndingStocks")
    private Long endingStocks;
    @JsonProperty("DocumentCode")
    private String documentCode;
    @JsonProperty("DocumentType")
    private Long documentType;
    @JsonProperty("BranchId")
    private Long branchId;
    @JsonProperty("RetailerId")
    private Long retailerId;
    @JsonProperty("Price")
    private BigDecimal price;
    @JsonProperty("Cost")
    private BigDecimal cost;
    @JsonProperty("TransDate")
    private String transDate;
    @JsonProperty("DocumentId")
    private Long documentId;
}
