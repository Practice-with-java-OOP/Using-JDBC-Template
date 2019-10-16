package com.jidian.cosalon.migration.pos365.domainpos365;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name = "p365_transfers_detail")
public class Pos365TransfersDetail {
    @Id
    @JsonProperty("Id")
    private Long id;
    @JsonProperty("TransferId")
    private Long transferId;
    @JsonProperty("ProductId")
    private Long productId;
    @JsonProperty("Quantity")
    private Integer quantity;
    @JsonProperty("Price")
    private BigDecimal price;
    @JsonProperty("PriceLargeUnit")
    private BigDecimal priceLargeUnit;
    @JsonProperty("ProductType")
    private Integer productType;
    @JsonProperty("ConversionValue")
    private Integer conversionValue;
}
