package com.jidian.cosalon.migration.pos365.domainpos365;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "p365_partners")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Pos365Partner {

    @JsonProperty("Debt")
    private BigDecimal debt;
    @JsonProperty("Transactionvalue")
    private Long transactionValue;
    @JsonProperty("TotalTransactionvalue")
    private Long totalTransactionValue;
    @JsonProperty("BranchId")
    private Long branchId;
    @JsonProperty("Id")
    @Id
    private Long id;
    @JsonProperty("Code")
    private String code;
    @JsonProperty("Name")
    private String name;
    @JsonProperty("Address")
    private String address;
    @JsonProperty("Phone")
    private String phone;
    @JsonProperty("Type")
    private Integer type;
    @JsonProperty("Gender")
    private Integer gender;
    @JsonProperty("TotalDebt")
    private BigDecimal totalDebt;
    @JsonProperty("TaxCode")
    private String taxCode;
    @JsonProperty("Loyalty")
    private Integer loyalty;
    @JsonProperty("CreatedBy")
    private Long createdBy;
    @JsonProperty("CreatedDate")
    private String createdDate;
    @JsonProperty("ModifiedDate")
    private String modifiedDate;
    @JsonProperty("RetailerId")
    private Long retailerId;
    @JsonProperty("Company")
    private String company;
    @JsonProperty("Point")
    private Integer point;
    @JsonProperty("AccountingTransactions")
    @Transient
    private List<Object> accountingTransactions;
    @JsonProperty("PartnerBranches")
    @Transient
    private List<Object> partnerBranches;
    @JsonProperty("PartnerGroupMembers")
    @Transient
    private List<Object> partnerGroupMembers;
    @JsonProperty("PurchaseOrders")
    @Transient
    private List<Object> purchaseOrders;
    @JsonProperty("Returns")
    @Transient
    private List<Object> returns;
    @JsonProperty("PurchaseOrderReturns")
    @Transient
    private List<Object> purchaseOrderReturns;
    @JsonProperty("PointUses")
    @Transient
    private List<Object> pointUses;
    @JsonProperty("ProductPartners")
    @Transient
    private List<Object> productPartners;
    @JsonProperty("DeliveryOrders")
    @Transient
    private List<Object> deliveryOrders;
    @JsonProperty("DeliveryOrders1")
    @Transient
    private List<Object> deliveryOrders1;
    @JsonProperty("Orders")
    @Transient
    private List<Object> orders;
}
