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
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "p365_order_detail")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Pos365OrderDetail {
    @JsonProperty("Id")
    @Id
    private Long id;
    @JsonProperty("OrderId")
    private Long orderId;
    @JsonProperty("ProductId")
    private Long productId;
    @JsonProperty("Quantity")
    private int quantity;
    @JsonProperty("Price")
    private BigDecimal price;
    @JsonProperty("BasePrice")
    private BigDecimal basePrice;
    @JsonProperty("IsLargeUnit")
    private Boolean isLargeUnit;
    @JsonProperty("ConversionValue")
    private int conversionValue;
    @JsonProperty("Coefficient")
    private int coefficient;
    @JsonProperty("Processed")
    private int processed;
    @JsonProperty("SoldById")
    private Long soldById;
    @JsonProperty("AssistantById")
    private Long assistantById;
}
