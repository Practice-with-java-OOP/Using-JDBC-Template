package com.jidian.cosalon.migration.pos365.thread.imsimpl;

import com.jidian.cosalon.migration.pos365.Utils;
import com.jidian.cosalon.migration.pos365.domain.GoodsReceipt;
import com.jidian.cosalon.migration.pos365.domainpos365.Pos365BranchImsWarehouse;
import com.jidian.cosalon.migration.pos365.domainpos365.Pos365Order;
import com.jidian.cosalon.migration.pos365.domainpos365.Pos365OrderDetail;
import com.jidian.cosalon.migration.pos365.domainpos365.Pos365User;
import com.jidian.cosalon.migration.pos365.thread.MyThread;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component("imsRetailGoodsReceiptThread")
public class ImsRetailGoodsReceiptThread extends MyThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImsRetailGoodsReceiptThread.class);

    @Override
    public String getName() {
        return "ImsRetailGoodsReceiptThread";
    }

    private int insertedTotal = 0;
    private int assumptionTotal = 0;
    private List<String> servicePrefixes = Arrays.asList("HD", "OFFLINE", "VD");
    private List<String> retailPrefixes = Arrays.asList("HA", "KT", "VH");

    @Override
    public void doRun() {
        insertedTotal = 0;
        assumptionTotal = 0;
        try {
            final Map<Long, Long> branchWarehouseMap = jdbcTemplate.query(
                    "select a.id as warehouse_id, b.id as branch_id " +
                            "from ims_warehouse a left join p365_branchs b on a.name = b.name ",
                    (rs, rowNum) -> {
                        final Pos365BranchImsWarehouse result = new Pos365BranchImsWarehouse();
                        result.setImsWarehouseId(rs.getLong("warehouse_id"));
                        result.setP365BranchId(rs.getLong("branch_id"));
                        return result;
                    }
            ).stream().collect(Collectors.toMap(Pos365BranchImsWarehouse::getP365BranchId, Pos365BranchImsWarehouse::getImsWarehouseId));
            final Map<Long, Pos365User> userMap = jdbcTemplate.query(
                    "select * from p365_users a where a.id in ( " +
                            "    select a.sold_by_id from p365_order_detail a where a.product_id in (select id from p365_products a where a.product_type = 1)) ",
                    (rs, rowNum) -> {
                        final Pos365User result = new Pos365User();
                        result.setId(rs.getLong("id"));
                        result.setName(rs.getString("name"));
                        return result;
                    }
            ).stream().collect(Collectors.toMap(Pos365User::getId, pos365User -> pos365User));
            final Map<Long, Pos365User> technicianMap = jdbcTemplate.query(
                    "select * from p365_users a where a.id in ( " +
                            "    select a.assistant_by_id from p365_order_detail a where a.product_id in (select id from p365_products a where a.product_type = 1)) ",
                    (rs, rowNum) -> {
                        final Pos365User result = new Pos365User();
                        result.setId(rs.getLong("id"));
                        result.setName(rs.getString("name"));
                        return result;
                    }
            ).stream().collect(Collectors.toMap(Pos365User::getId, pos365User -> pos365User));

            final List<RetailGoodsReceiptQueryDto> items = jdbcTemplate.query(
                    "select distinct a.*, b.name as partner_name, b.phone as partner_phone from ( " +
                            "    select * from p365_orders a where a.id in (select a.order_id from p365_order_detail a " +
                            "    where a.product_id in (select id from p365_products a where a.product_type = 1))) a " +
                            "left outer join p365_partners b on a.partner_id = b.id " +
                            "order by a.id asc ",
                    (rs, rowNum) -> {
                        final RetailGoodsReceiptQueryDto result = new RetailGoodsReceiptQueryDto();
                        result.setId(rs.getLong("id"));
                        result.setAmountReceived(rs.getBigDecimal("amount_received"));
                        result.setBranchId(rs.getLong("branch_id"));
                        result.setCode(rs.getString("code"));
                        result.setCreatedDate(rs.getString("created_date"));
                        result.setCreatedBy(rs.getLong("created_by"));
                        result.setDiscount(rs.getBigDecimal("discount"));
                        result.setDiscountToView(rs.getString("discount_to_view"));
                        result.setExcessCash(rs.getBigDecimal("excess_cash"));
                        result.setIsOnline(rs.getBoolean("is_online"));
                        result.setLadingCode(rs.getString("lading_code"));
                        result.setMoreAttributes(rs.getString("more_attributes"));
                        result.setNumberOfGuests(rs.getInt("number_of_guests"));
                        result.setPartnerId(rs.getLong("partner_id"));
                        result.setPriceBookId(rs.getLong("price_book_id"));
                        result.setPurchaseDate(rs.getString("purchase_date"));
                        result.setRetailerId(rs.getLong("retailer_id"));
                        result.setShippingCost(rs.getBigDecimal("shipping_cost"));
                        result.setShippingCostForPartner(rs.getLong("shipping_cost_for_partner"));
                        result.setSoldById(rs.getLong("sold_by_id"));
                        result.setStatus(rs.getInt("status"));
                        result.setTotal(rs.getBigDecimal("total"));
                        result.setTotalPayment(rs.getBigDecimal("total_payment"));
                        result.setVat(rs.getBigDecimal("vat"));
                        result.setVoucher(rs.getBigDecimal("voucher"));
                        result.setPartnerName(rs.getString("partner_name"));
                        result.setPartnerPhone(rs.getString("partner_phone"));
                        return result;
                }
            );
            assumptionTotal = items.size();

            final List<RetailGoodsReceiptChemicalQueryDto> details = jdbcTemplate.query(
                    "select " +
                            "    a.*, c.id as ims_chemical_id, case when a.is_large_unit = 1 then d.id else e.id end as unit_id, " +
                            "    case when a.is_large_unit = 1 then d.name else e.name end as unit_name, " +
                            "    case when a.is_large_unit = 1 then d.type else e.type end as unit_type, " +
                            "    case when a.is_large_unit = 1 then d.standard_unit_exchange else e.standard_unit_exchange end as unit_exchange " +
                            "from (select * from p365_order_detail a where a.product_id in (select id from p365_products a where a.product_type = 1)) a " +
                            "    left outer join p365_products b on a.product_id = b.id " +
                            "    left outer join ims_chemical c on trim(b.code) = trim(c.code) " +
                            "    left outer join (select * from ims_chemical_sub_unit a where a.type = 1) d on d.chemical_id = c.id " +
                            "    left outer join (select * from ims_chemical_sub_unit a where a.type = 2) e on e.chemical_id = c.id ",
                    (rs, rowNum) -> {
                        final RetailGoodsReceiptChemicalQueryDto result = new RetailGoodsReceiptChemicalQueryDto();
                        result.setId(rs.getLong("id"));
                        result.setConversionValue(rs.getInt("conversion_value"));
                        result.setCoefficient(rs.getInt("coefficient"));
                        result.setIsLargeUnit(rs.getBoolean("is_large_unit"));
                        result.setPrice(rs.getBigDecimal("price"));
                        result.setBasePrice(rs.getBigDecimal("base_price"));
                        result.setProductId(rs.getLong("product_id"));
                        result.setQuantity(rs.getInt("quantity"));
                        result.setOrderId(rs.getLong("order_id"));
                        result.setProcessed(rs.getInt("processed"));
                        result.setSoldById(rs.getLong("sold_by_id"));
                        result.setAssistantById(rs.getLong("assistant_by_id"));
                        result.setImsChemicalId(rs.getLong("ims_chemical_id"));
                        result.setUnitId(rs.getLong("unit_id"));
                        result.setUnitType(rs.getInt("unit_type"));
                        result.setUnitName(rs.getString("unit_name"));
                        result.setUnitExchange(rs.getBigDecimal("unit_exchange"));
                        return result;
                    }
            );
            Map<Long, List<RetailGoodsReceiptChemicalQueryDto>> detailMap = details.stream().collect(Collectors.groupingBy(Pos365OrderDetail::getOrderId));

            final List<GoodsReceipt> existing = jdbcTemplate.query(
                    "select * from ims_goods_receipt a where a.type = 5 ",
                    (rs, rowNum) -> {
                        final GoodsReceipt result = new GoodsReceipt();
                        result.setId(rs.getLong("id"));
                        result.setType(rs.getInt("type"));
                        result.setReceiptCode(rs.getString("receipt_code"));
                        return result;
                    }
            );
            Map<String, List<GoodsReceipt>> existingMap = existing.stream().collect(Collectors.groupingBy(GoodsReceipt::getReceiptCode));
            List<RetailGoodsReceiptQueryDto> inserts = new ArrayList<>();
            List<RetailGoodsReceiptQueryDto> updates = new ArrayList<>();
            items.forEach(item -> {
                if (existingMap.containsKey(Utils.nvl(item.getCode()).trim())) {
                    item.setImsGoodsReceiptId(existingMap.get(Utils.nvl(item.getCode()).trim()).get(0).getId());
                    updates.add(item);
                } else {
                    inserts.add(item);
                }
            });

            PrintWriter fileWriter = new PrintWriter("/Users/haimt/Desktop/" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".txt", "UTF-8");
            inserts.forEach(item -> {
                int type = 0;
                for (String prefix : servicePrefixes) {
                    if (Utils.nvl(item.getCode()).trim().startsWith(prefix)) {
                        type = 3;
                        break;
                    }
                }
                for (String prefix : retailPrefixes) {
                    if (Utils.nvl(item.getCode()).trim().startsWith(prefix)) {
                        type = 5;
                        break;
                    }
                }
                if (type == 0) {
                    fileWriter.println(Utils.nvl(item.getCode()).trim());
                    return;
                }

                KeyHolder keyHolder = new GeneratedKeyHolder();
                int finalType = type;
                insertedTotal += jdbcTemplate.update(
                        connection -> {
                            PreparedStatement ps = connection.prepareStatement(
                                    "INSERT INTO ims_goods_receipt (gmt_create, gmt_modified, version, type, import_export, receipt_code, " +
                                            "    supplier_id, gmt_delivery, gmt_import, gmt_export, source_warehouse_id, dest_warehouse_id, " +
                                            "    total_quantity, total_pre_amount, deduction, total_amount, creator_id, editor_id, finisher_id, " +
                                            "    requester_id, requester_type, requester_name, requester_phone_num, order_num, reference, remark, status) " +
                                            "VALUES (CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP(),0,?,2,?, " +
                                            "    null,null,null,?,null,?, " +
                                            "    0,?,?,?,1,null,null, " +
                                            "    null,6,?,?,?,null,null,?) ",
                                    new String[] {"id"});
                            int i = 1;
                            ps.setInt(i++, finalType);
                            ps.setString(i++, Utils.nvl(item.getCode()).trim());
                            ps.setTimestamp(i++, Utils.convertTimestamp(item.getPurchaseDate()));
                            ps.setLong(i++, branchWarehouseMap.get(item.getBranchId()));
                            ps.setBigDecimal(i++, Utils.nvl(item.getTotal()));
                            ps.setBigDecimal(i++, Utils.nvl(item.getTotal()).intValue() == 0 ? BigDecimal.ZERO : Utils.nvl(item.getDiscount()).divide(item.getTotal(), 2, RoundingMode.HALF_UP));
                            ps.setBigDecimal(i++, Utils.nvl(item.getTotal().subtract(Utils.nvl(item.getDiscount()))));
                            ps.setString(i++, item.getPartnerName());
                            ps.setString(i++, item.getPartnerPhone());
                            ps.setString(i++, Utils.nvl(item.getCode()).trim());
                            ps.setLong(i++, StatusEnum.resolve(Utils.nvl(item.getStatus())) == null ? 10: StatusEnum.resolve(Utils.nvl(item.getStatus())).getGoodsReceiptStatus());
                            return ps;
                        },
                        keyHolder);
                List<RetailGoodsReceiptChemicalQueryDto> subDetails = detailMap.get(item.getId());
                if (keyHolder.getKey() != null && subDetails != null) {
                    if (!subDetails.isEmpty()) {
                        jdbcTemplate.update("delete from ims_goods_receipt_person where goods_receipt_chemical_id in (select a.id from ims_goods_receipt_chemical a where goods_receipt_id = ?)",
                                keyHolder.getKey().longValue());
                        jdbcTemplate.update("delete from ims_goods_receipt_chemical where goods_receipt_id = ?",
                                keyHolder.getKey().longValue());
                    }
                    subDetails.forEach(subDetail -> {
                        KeyHolder keyHolder2 = new GeneratedKeyHolder();
                        jdbcTemplate.update(
                                connection -> {
                                    PreparedStatement ps = connection.prepareStatement(
                                            "INSERT INTO ims_goods_receipt_chemical (gmt_create, gmt_modified, version, goods_receipt_id, " +
                                                    "    chemical_id, unit_id, unit_type, unit_name, unit_price, standard_unit_exchange, quantity, total_price) " +
                                                    "VALUES (CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP(),0,?, " +
                                                    "    ?,?,?,?,?,?,?,?) ",
                                            new String[] {"id"});
                                    int i = 1;
                                    ps.setLong(i++, keyHolder.getKey().longValue());
                                    ps.setLong(i++, subDetail.getImsChemicalId());
                                    ps.setLong(i++, subDetail.getUnitId());
                                    ps.setInt(i++, subDetail.getUnitType());
                                    ps.setString(i++, Utils.nvl(subDetail.getUnitName()));
                                    ps.setBigDecimal(i++, subDetail.getPrice());
                                    ps.setBigDecimal(i++, subDetail.getUnitExchange());
                                    ps.setInt(i++, subDetail.getQuantity());
                                    ps.setBigDecimal(i++, Utils.nvl(subDetail.getPrice()).multiply(new BigDecimal(Utils.nvl(subDetail.getQuantity()))));
                                    return ps;
                                },
                                keyHolder2);
                        if (keyHolder2.getKey() != null) {
                            if (subDetail.getSoldById() != null && subDetail.getSoldById() > 0L) {
                                jdbcTemplate.update(
                                        "INSERT INTO ims_goods_receipt_person (gmt_create, gmt_modified, version, goods_receipt_chemical_id, " +
                                                "    person_role, person_id, person_type, person_name, person_phone_num) " +
                                                "VALUES (CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP(),0,?, " +
                                                "    3,0,6,?,?) ",
                                        keyHolder2.getKey().longValue(),
                                        userMap.containsKey(Utils.nvl(subDetail.getSoldById())) ? userMap.get(Utils.nvl(subDetail.getSoldById())).getName() : null,
                                        userMap.containsKey(Utils.nvl(subDetail.getSoldById())) ? Utils.nvl(userMap.get(Utils.nvl(subDetail.getSoldById())).getPhone()) : ""
                                );
                            }
                            if (subDetail.getAssistantById() != null && subDetail.getAssistantById() > 0L) {
                                jdbcTemplate.update(
                                        "INSERT INTO ims_goods_receipt_person (gmt_create, gmt_modified, version, goods_receipt_chemical_id, " +
                                                "    person_role, person_id, person_type, person_name, person_phone_num) " +
                                                "VALUES (CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP(),0,?, " +
                                                "    3,0,6,?,?) ",
                                        keyHolder2.getKey().longValue(),
                                        technicianMap.containsKey(Utils.nvl(subDetail.getAssistantById())) ? technicianMap.get(Utils.nvl(subDetail.getAssistantById())).getName() : null,
                                        technicianMap.containsKey(Utils.nvl(subDetail.getAssistantById())) ? Utils.nvl(technicianMap.get(Utils.nvl(subDetail.getAssistantById())).getPhone()) : ""
                                );
                            }
                        }
                    });

                }
            });
            fileWriter.close();

            updates.forEach(item -> {
                insertedTotal += jdbcTemplate.update(
                        connection -> {
                            PreparedStatement ps = connection.prepareStatement(
                                    "UPDATE ims_goods_receipt SET gmt_modified = CURRENT_TIMESTAMP(), version = version+1, type = type, " +
                                            "    import_export = 1, receipt_code = ?, supplier_id = null, gmt_delivery = null, gmt_import = null, gmt_export = ?, " +
                                            "    source_warehouse_id = null, dest_warehouse_id = ?, total_quantity = 0, total_pre_amount = ?, deduction = ?, total_amount = ?, " +
                                            "    creator_id = 1, editor_id = null, finisher_id = null, requester_id = null, requester_type = 6, requester_name = ?, requester_phone_num = ?, " +
                                            "    order_num = ?, reference = null, remark = null, status = ? WHERE id = ? ",
                                    new String[] {"id"});
                            int i = 1;
                            ps.setString(i++, Utils.nvl(item.getCode()).trim());
                            ps.setTimestamp(i++, Utils.convertTimestamp(item.getPurchaseDate()));
                            ps.setLong(i++, branchWarehouseMap.get(item.getBranchId()));
                            ps.setBigDecimal(i++, Utils.nvl(item.getTotal()));
                            ps.setBigDecimal(i++, Utils.nvl(item.getTotal()).intValue() == 0 ? BigDecimal.ZERO : Utils.nvl(item.getDiscount()).divide(item.getTotal(), 2, RoundingMode.HALF_UP));
                            ps.setBigDecimal(i++, Utils.nvl(item.getTotal().subtract(Utils.nvl(item.getDiscount()))));
                            ps.setString(i++, item.getPartnerName());
                            ps.setString(i++, item.getPartnerPhone());
                            ps.setString(i++, Utils.nvl(item.getCode()).trim());
                            ps.setLong(i++, StatusEnum.resolve(Utils.nvl(item.getStatus())) == null ? 10: StatusEnum.resolve(Utils.nvl(item.getStatus())).getGoodsReceiptStatus());
                            ps.setLong(i++, item.getImsGoodsReceiptId());
                            return ps;
                        });
                List<RetailGoodsReceiptChemicalQueryDto> subDetails = detailMap.get(item.getId());
                if (subDetails != null) {
                    if (!subDetails.isEmpty()) {
                        jdbcTemplate.update("delete from ims_goods_receipt_person where goods_receipt_chemical_id in (select a.id from ims_goods_receipt_chemical a where goods_receipt_id = ?)",
                                item.getImsGoodsReceiptId());
                        jdbcTemplate.update("delete from ims_goods_receipt_chemical where goods_receipt_id = ?",
                                item.getImsGoodsReceiptId());
                    }
                    subDetails.forEach(subDetail -> {
                        KeyHolder keyHolder2 = new GeneratedKeyHolder();
                        jdbcTemplate.update(
                                connection -> {
                                    PreparedStatement ps = connection.prepareStatement(
                                            "INSERT INTO ims_goods_receipt_chemical (gmt_create, gmt_modified, version, goods_receipt_id, " +
                                                    "    chemical_id, unit_id, unit_type, unit_name, unit_price, standard_unit_exchange, quantity, total_price) " +
                                                    "VALUES (CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP(),0,?, " +
                                                    "    ?,?,?,?,?,?,?,?) ",
                                            new String[] {"id"});
                                    int i = 1;
                                    ps.setLong(i++, item.getImsGoodsReceiptId());
                                    ps.setLong(i++, subDetail.getImsChemicalId());
                                    ps.setLong(i++, subDetail.getUnitId());
                                    ps.setInt(i++, subDetail.getUnitType());
                                    ps.setString(i++, Utils.nvl(subDetail.getUnitName()));
                                    ps.setBigDecimal(i++, subDetail.getPrice());
                                    ps.setBigDecimal(i++, subDetail.getUnitExchange());
                                    ps.setInt(i++, subDetail.getQuantity());
                                    ps.setBigDecimal(i++, Utils.nvl(subDetail.getPrice()).multiply(new BigDecimal(Utils.nvl(subDetail.getQuantity()))));
                                    return ps;
                                },
                                keyHolder2);
                        if (keyHolder2.getKey() != null) {
                            if (subDetail.getSoldById() != null && subDetail.getSoldById() > 0L) {
                                jdbcTemplate.update(
                                        "INSERT INTO ims_goods_receipt_person (gmt_create, gmt_modified, version, goods_receipt_chemical_id, " +
                                                "    person_role, person_id, person_type, person_name, person_phone_num) " +
                                                "VALUES (CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP(),0,?, " +
                                                "    3,0,6,?,?) ",
                                        keyHolder2.getKey().longValue(),
                                        userMap.containsKey(Utils.nvl(subDetail.getSoldById())) ? userMap.get(Utils.nvl(subDetail.getSoldById())).getName() : null,
                                        userMap.containsKey(Utils.nvl(subDetail.getSoldById())) ? Utils.nvl(userMap.get(Utils.nvl(subDetail.getSoldById())).getPhone()) : ""
                                );
                            }
                            if (subDetail.getAssistantById() != null && subDetail.getAssistantById() > 0L) {
                                jdbcTemplate.update(
                                        "INSERT INTO ims_goods_receipt_person (gmt_create, gmt_modified, version, goods_receipt_chemical_id, " +
                                                "    person_role, person_id, person_type, person_name, person_phone_num) " +
                                                "VALUES (CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP(),0,?, " +
                                                "    3,0,6,?,?) ",
                                        keyHolder2.getKey().longValue(),
                                        technicianMap.containsKey(Utils.nvl(subDetail.getAssistantById())) ? technicianMap.get(Utils.nvl(subDetail.getAssistantById())).getName() : null,
                                        technicianMap.containsKey(Utils.nvl(subDetail.getAssistantById())) ? Utils.nvl(technicianMap.get(Utils.nvl(subDetail.getAssistantById())).getPhone()) : ""
                                );
                            }
                        }
                    });

                }
            });
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            LOGGER.info("SUMMARY: insert/update total: {}, pos365 product total: {}", insertedTotal, assumptionTotal);
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class RetailGoodsReceiptQueryDto extends Pos365Order {
        private Long imsGoodsReceiptId;
        private String partnerName;
        private String partnerPhone;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class RetailGoodsReceiptChemicalQueryDto extends Pos365OrderDetail {
        private Long imsChemicalId;
        private Long unitId;
        private Integer unitType;
        private String unitName;
        private BigDecimal unitExchange;
    }

    private enum StatusEnum {
        EXECUTING(1L, 10L),
        FINISHED(2L, 40L),
        CANCELED(3L, 30L),
        DELIVERING(4L, 20L)
        ;

        @Getter
        private final long value;
        @Getter
        private final long goodsReceiptStatus;

        StatusEnum(long value, long grStatus) {
            this.value = value;
            this.goodsReceiptStatus = grStatus;
        }

        public static StatusEnum resolve(long value) {
            for (StatusEnum mEnum : values()) {
                if (mEnum.value == value) {
                    return mEnum;
                }
            }
            return null;
        }
    }
}
