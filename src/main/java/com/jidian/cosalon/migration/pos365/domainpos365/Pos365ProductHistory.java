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
    protected Long id;
    @JsonProperty("ProductId")
    protected Long productId;
    @JsonProperty("Quantity")
    protected Long quantity;
    @JsonProperty("EndingStocks")
    protected Long endingStocks;
    @JsonProperty("DocumentCode")
    protected String documentCode;
    @JsonProperty("DocumentType")
    protected Long documentType;
    @JsonProperty("BranchId")
    protected Long branchId;
    @JsonProperty("RetailerId")
    protected Long retailerId;
    @JsonProperty("Price")
    protected BigDecimal price;
    @JsonProperty("Cost")
    protected BigDecimal cost;
    @JsonProperty("TransDate")
    protected String transDate;
    @JsonProperty("DocumentId")
    protected Long documentId;
}
