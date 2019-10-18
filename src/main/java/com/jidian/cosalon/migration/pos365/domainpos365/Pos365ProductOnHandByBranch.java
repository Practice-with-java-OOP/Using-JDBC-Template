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
    protected Long productId;
    @JsonProperty("BranchId")
    @Id
    protected Long branchId;
    @JsonProperty("RetailerId")
    protected Long retailerId;
    @JsonProperty("OnHand")
    protected Long onHand;
    @JsonProperty("OnOrder")
    protected Long onOrder;
    @JsonProperty("MinQuantity")
    protected Long minQuantity;
    @JsonProperty("MaxQuantity")
    protected Long maxQuantity;
    @JsonProperty("Cost")
    protected BigDecimal cost;
    @JsonProperty("PriceByBranch")
    protected BigDecimal priceByBranch;
    @JsonProperty("CreatedDate")
    protected String createdDate;
    @JsonProperty("CreatedBy")
    protected Long createdBy;
    @JsonProperty("ModifiedDate")
    protected String modifiedDate;
    @JsonProperty("ModifiedBy")
    protected Long modifiedBy;
    @JsonProperty("PriceByBranchLargeUnit")
    protected BigDecimal priceByBranchLargeUnit;
}
