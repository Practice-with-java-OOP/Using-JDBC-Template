package com.jidian.cosalon.migration.pos365.domainpos365;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "p365_products")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Pos365Product {
    @JsonProperty("Id")
    @Id
    protected Long id;
    @JsonProperty("OnHand")
    protected Long onHand;
    @JsonProperty("TotalOnHand")
    protected Long totalOnHand;
    @JsonProperty("RecentPurchasePrice")
    protected Long recentPurchasePrice;
    @JsonProperty("RecentPurchasePriceLargeUnit")
    protected Long recentPurchasePriceLargeUnit;
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
    @JsonProperty("PriceByBranchLargeUnit")
    protected BigDecimal priceByBranchLargeUnit;
    @JsonProperty("CompareMinQuantity")
    protected Long compareMinQuantity;
    @JsonProperty("CompareMaxQuantity")
    protected Long compareMaxQuantity;
    @JsonProperty("Code")
    protected String code;
    @JsonProperty("Name")
    protected String name;
    @JsonProperty("AttributesName")
    protected String attributesName;
    @JsonProperty("CategoryId")
    protected Long categoryId;
    @JsonProperty("Price")
    protected BigDecimal price;
    @JsonProperty("PriceLargeUnit")
    protected BigDecimal priceLargeUnit;
    @JsonProperty("ProductType")
    protected Long productType;
    @JsonProperty("ConversionValue")
    protected Long conversionValue;
    @JsonProperty("Unit")
    protected String unit;
    @JsonProperty("LargeUnit")
    protected String largeUnit;
    @JsonProperty("IsSerialNumberTracking")
    protected Boolean isSerialNumberTracking;
    @JsonProperty("CreatedDate")
    protected String createdDate;
    @JsonProperty("CreatedBy")
    protected Long createdBy;
    @JsonProperty("ModifiedDate")
    protected String modifiedDate;
    @JsonProperty("RetailerId")
    protected Long retailerId;
    @JsonProperty("IsPercentageOfTotalOrder")
    protected Boolean isPercentageOfTotalOrder;
    @JsonProperty("SplitForSalesOrder")
    protected Boolean splitForSalesOrder;
    @JsonProperty("OrderQuickNotes")
    protected String orderQuickNotes;
    @JsonProperty("Printer")
    protected String printer;
    @JsonProperty("BonusPoint")
    protected Long bonusPoint;
    @JsonProperty("BonusPointForAssistant")
    protected Long bonusPointForAssistant;
    @JsonProperty("BonusPointForAssistant2")
    protected Long bonusPointForAssistant2;
    @JsonProperty("BonusPointForAssistant3")
    protected Long bonusPointForAssistant3;
    @JsonProperty("BlockOfTimeToUseService")
    protected Long blockOfTimeToUseService;
    @JsonProperty("IsPriceForBlock")
    protected Boolean isPriceForBlock;
    @JsonProperty("Position")
    protected Long Position;
    @JsonProperty("LargeUnitCode")
    protected String largeUnitCode;
    @JsonProperty("BarCodes")
    @Transient
    protected List<Object> barCodes;
//    @JsonProperty("Category")
//    @Transient
//    protected Category category;
    @JsonProperty("CompositeItems")
    @Transient
    protected List<Object> compositeItems;
    @JsonProperty("CompositeItems1")
    @Transient
    protected List<Object> compositeItems1;
    @JsonProperty("DeliveryOrderDetails")
    @Transient
    protected List<Object> deliveryOrderDetails;
    @JsonProperty("InventoryCountDetails")
    @Transient
    protected List<Object> inventoryCountDetails;
    @JsonProperty("ManufacturingDetails")
    @Transient
    protected List<Object> manufacturingDetails;
    @JsonProperty("ManufacturingMaterials")
    @Transient
    protected List<Object> manufacturingMaterials;
    @JsonProperty("NotebookDetails")
    @Transient
    protected List<Object> notebookDetails;
    @JsonProperty("OrderDetails")
    @Transient
    protected List<Object> orderDetails;
    @JsonProperty("OtherTransactionDetails")
    @Transient
    protected List<Object> otherTransactionDetails;
    @JsonProperty("PriceBookDetails")
    @Transient
    protected List<Object> priceBookDetails;
    @JsonProperty("ProductAttributes")
    @Transient
    protected List<Object> productAttributes;
    @JsonProperty("ProductBranches")
    @Transient
    protected List<Object> productBranches;
    @JsonProperty("ProductExtras")
    @Transient
    protected List<Object> productExtras;
    @JsonProperty("ProductImages")
    @Transient
    protected List<Object> productImages;
    @JsonProperty("ProductPartners")
    @Transient
    protected List<Object> productPartners;
    @JsonProperty("ProductSerials")
    @Transient
    protected List<Object> productSerials;
    @JsonProperty("PurchaseOrderReturnDetails")
    @Transient
    protected List<Object> purchaseOrderReturnDetails;
    @JsonProperty("ReturnDetails")
    @Transient
    protected List<Object> returnDetails;
    @JsonProperty("Rooms")
    @Transient
    protected List<Object> rooms;
    @JsonProperty("RoomHistories")
    @Transient
    protected List<Object> roomHistories;
    @JsonProperty("TransferDetails")
    @Transient
    protected List<Object> transferDetails;

}
