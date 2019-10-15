package com.jidian.cosalon.migration.pos365.domainpos365;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "p365_items")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Post365Items {
    @JsonProperty("Id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JsonProperty("ProductId")
    private Long productId;
    @JsonProperty("Name")
    private String name;
    @JsonProperty("AttributesName")
    private String attributesName;
    @JsonProperty("Code")
    private String code;
    @JsonProperty("Cost")
    private BigDecimal cost;
    @JsonProperty("MultiUnit")
    private Boolean multiUnit;
    @JsonProperty("Price")
    private BigDecimal price;
    @JsonProperty("OriginalPrice")
    private BigDecimal originalPrice;
    @JsonProperty("OriginalPriceLargeUnit")
    private BigDecimal originalPriceLargeUnit;
    @JsonProperty("PriceLargeUnit")
    private BigDecimal priceLargeUnit;
}
