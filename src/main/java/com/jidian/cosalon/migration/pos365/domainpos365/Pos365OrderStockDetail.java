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
@Table(name = "p365_order_stock_detail")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Pos365OrderStockDetail {
    @JsonProperty("Id")
    @Id
    private Long id;
    @JsonProperty("PurchaseOrderId")
    private Long purchaseOrderId;
    @JsonProperty("ProductId")
    private Long productId;
    @JsonProperty("Quantity")
    private int quantity;
    @JsonProperty("Price")
    private BigDecimal price;
    @JsonProperty("Description")
    private String description;
    @JsonProperty("IsLargeUnit")
    private Boolean isLargeUnit;
    @JsonProperty("SellingPrice")
    private BigDecimal sellingPrice;
    @JsonProperty("ConversionValue")
    private int conversionValue;
    @JsonProperty("OrderQuantity")
    private int orderQuantity;
}
