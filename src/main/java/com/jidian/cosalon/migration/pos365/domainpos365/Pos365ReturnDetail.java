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
@Table(name = "p365_return_detail")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Pos365ReturnDetail {
    @JsonProperty("Id")
    @Id
    private Long id;
    @JsonProperty("ReturnId")
    private Long returnId;
    @JsonProperty("ProductId")
    private Long productId;
    @JsonProperty("Quantity")
    private Long quantity;
    @JsonProperty("Price")
    private BigDecimal price;
    @JsonProperty("IsLargeUnit")
    private Boolean isLargeUnit;
    @JsonProperty("ConversionValue")
    private int conversionValue;
}
