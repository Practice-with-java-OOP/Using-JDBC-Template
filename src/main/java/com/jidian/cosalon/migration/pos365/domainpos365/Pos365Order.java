package com.jidian.cosalon.migration.pos365.domainpos365;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "p365_orders")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Pos365Order {

    @JsonProperty("Id")
    @Id
    private Long id;
    @JsonProperty("Code")
    private String code;
    @JsonProperty("PurchaseDate")
    private String purchaseDate;
    @JsonProperty("BranchId")
    private Long branchId;
    @JsonProperty("Status")
    private Integer status;
    @JsonProperty("RetailerId")
    private Long retailerId;
    @JsonProperty("Discount")
    private BigDecimal discount;
    @JsonProperty("SoldById")
    private Long soldById;
    @JsonProperty("CreatedDate")
    private String createdDate;
    @JsonProperty("CreatedBy")
    private Long createdBy;
    @JsonProperty("DiscountToView")
    private String discountToView;
    @JsonProperty("Total")
    private BigDecimal total;
    @JsonProperty("TotalPayment")
    private BigDecimal totalPayment;
    @JsonProperty("PartnerId")
    private Long partnerId;
    @JsonProperty("ExcessCash")
    private BigDecimal excessCash;
    @JsonProperty("AmountReceived")
    private BigDecimal amountReceived;
    @JsonProperty("ShippingCost")
    private BigDecimal shippingCost;
    @JsonProperty("PriceBookId")
    private Long priceBookId;
    @JsonProperty("NumberOfGuests")
    private Integer numberOfGuests;
    @JsonProperty("VAT")
    private BigDecimal vat;
    @JsonProperty("Voucher")
    private BigDecimal voucher;
    @JsonProperty("MoreAttributes")
    @Lob
    private String moreAttributes;
    @JsonProperty("AccountingTransactions")
    @Transient
    private List<Object> accountingTransactions;
    @JsonProperty("DeliveryOrders")
    @Transient
    private List<Object> deliveryOrders;
    @JsonProperty("OrderDetails")
    @Transient
    private List<Object> orderDetails;
    @JsonProperty("PointUsers")
    @Transient
    private List<Object> pointUsers;
    @JsonProperty("Returns")
    @Transient
    private List<Object> returns;
    @JsonProperty("Vouchers")
    @Transient
    private List<Object> vouchers;
    @JsonProperty("ShippingCostForPartner")
    private Long shippingCostForPartner;
    @JsonProperty("LadingCode")
    private String ladingCode;
    @JsonProperty("IsOnline")
    private Boolean isOnline;
}
