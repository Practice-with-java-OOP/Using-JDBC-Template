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
    private Long id;
    @JsonProperty("OnHand")
    private Long onHand;
    @JsonProperty("TotalOnHand")
    private Long totalOnHand;
    @JsonProperty("RecentPurchasePrice")
    private Long recentPurchasePrice;
    @JsonProperty("RecentPurchasePriceLargeUnit")
    private Long recentPurchasePriceLargeUnit;
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
    @JsonProperty("PriceByBranchLargeUnit")
    private BigDecimal priceByBranchLargeUnit;
    @JsonProperty("CompareMinQuantity")
    private Long compareMinQuantity;
    @JsonProperty("CompareMaxQuantity")
    private Long compareMaxQuantity;
    @JsonProperty("Code")
    private String code;
    @JsonProperty("Name")
    private String name;
    @JsonProperty("AttributesName")
    private String attributesName;
    @JsonProperty("CategoryId")
    private Long categoryId;
    @JsonProperty("Price")
    private BigDecimal price;
    @JsonProperty("PriceLargeUnit")
    private BigDecimal priceLargeUnit;
    @JsonProperty("ProductType")
    private Long productType;
    @JsonProperty("ConversionValue")
    private Long conversionValue;
    @JsonProperty("Unit")
    private String unit;
    @JsonProperty("LargeUnit")
    private String largeUnit;
    @JsonProperty("IsSerialNumberTracking")
    private Boolean isSerialNumberTracking;
    @JsonProperty("CreatedDate")
    private String createdDate;
    @JsonProperty("CreatedBy")
    private Long createdBy;
    @JsonProperty("ModifiedDate")
    private String modifiedDate;
    @JsonProperty("RetailerId")
    private Long retailerId;
    @JsonProperty("IsPercentageOfTotalOrder")
    private Boolean isPercentageOfTotalOrder;
    @JsonProperty("SplitForSalesOrder")
    private Boolean splitForSalesOrder;
    @JsonProperty("OrderQuickNotes")
    private String orderQuickNotes;
    @JsonProperty("Printer")
    private String printer;
    @JsonProperty("BonusPoint")
    private Long bonusPoint;
    @JsonProperty("BonusPointForAssistant")
    private Long bonusPointForAssistant;
    @JsonProperty("BonusPointForAssistant2")
    private Long bonusPointForAssistant2;
    @JsonProperty("BonusPointForAssistant3")
    private Long bonusPointForAssistant3;
    @JsonProperty("BlockOfTimeToUseService")
    private Long blockOfTimeToUseService;
    @JsonProperty("IsPriceForBlock")
    private Boolean isPriceForBlock;
    @JsonProperty("Position")
    private Long Position;
    @JsonProperty("LargeUnitCode")
    private String largeUnitCode;
    @JsonProperty("BarCodes")
    @Transient
    private List<Object> barCodes;
//    @JsonProperty("Category")
//    @Transient
//    private Category category;
    @JsonProperty("CompositeItems")
    @Transient
    private List<Object> compositeItems;
    @JsonProperty("CompositeItems1")
    @Transient
    private List<Object> compositeItems1;
    @JsonProperty("DeliveryOrderDetails")
    @Transient
    private List<Object> deliveryOrderDetails;
    @JsonProperty("InventoryCountDetails")
    @Transient
    private List<Object> inventoryCountDetails;
    @JsonProperty("ManufacturingDetails")
    @Transient
    private List<Object> manufacturingDetails;
    @JsonProperty("ManufacturingMaterials")
    @Transient
    private List<Object> manufacturingMaterials;
    @JsonProperty("NotebookDetails")
    @Transient
    private List<Object> notebookDetails;
    @JsonProperty("OrderDetails")
    @Transient
    private List<Object> orderDetails;
    @JsonProperty("OtherTransactionDetails")
    @Transient
    private List<Object> otherTransactionDetails;
    @JsonProperty("PriceBookDetails")
    @Transient
    private List<Object> priceBookDetails;
    @JsonProperty("ProductAttributes")
    @Transient
    private List<Object> productAttributes;
    @JsonProperty("ProductBranches")
    @Transient
    private List<Object> productBranches;
    @JsonProperty("ProductExtras")
    @Transient
    private List<Object> productExtras;
    @JsonProperty("ProductImages")
    @Transient
    private List<Object> productImages;
    @JsonProperty("ProductPartners")
    @Transient
    private List<Object> productPartners;
    @JsonProperty("ProductSerials")
    @Transient
    private List<Object> productSerials;
    @JsonProperty("PurchaseOrderReturnDetails")
    @Transient
    private List<Object> purchaseOrderReturnDetails;
    @JsonProperty("ReturnDetails")
    @Transient
    private List<Object> returnDetails;
    @JsonProperty("Rooms")
    @Transient
    private List<Object> rooms;
    @JsonProperty("RoomHistories")
    @Transient
    private List<Object> roomHistories;
    @JsonProperty("TransferDetails")
    @Transient
    private List<Object> transferDetails;

}
