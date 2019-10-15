package com.jidian.cosalon.migration.pos365.domainpos365;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@IdClass(Pos365ProductOnHandByBranchId.class)
@Table(name = "p365_products_onhandbybranchs")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Pos365ProductOnHandByBranch {
    @JsonProperty("ProductId")
    @Id
    private Long productId;
    @JsonProperty("BranchId")
    @Id
    private Long branchId;
    @JsonProperty("RetailerId")
    private Long retailerId;
    @JsonProperty("OnHand")
    private Long onHand;
    @JsonProperty("OnOrder")
    private Long onOrder;
    @JsonProperty("MinQuantity")
    private Long minQuantity;
    @JsonProperty("MaxQuantity")
    private Long maxQuantity;
    @JsonProperty("Cost")
    private BigDecimal cost;
    @JsonProperty("PriceByBranch")
    private BigDecimal priceByBranch;
    @JsonProperty("CreatedDate")
    private String createdDate;
    @JsonProperty("CreatedBy")
    private Long createdBy;
    @JsonProperty("ModifiedDate")
    private String modifiedDate;
    @JsonProperty("ModifiedBy")
    private Long modifiedBy;
    @JsonProperty("PriceByBranchLargeUnit")
    private BigDecimal priceByBranchLargeUnit;
}
