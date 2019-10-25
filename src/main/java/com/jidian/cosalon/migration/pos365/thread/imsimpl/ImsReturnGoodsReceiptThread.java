package com.jidian.cosalon.migration.pos365.thread.imsimpl;

import com.jidian.cosalon.migration.pos365.Utils;
import com.jidian.cosalon.migration.pos365.domain.GoodsReceipt;
import com.jidian.cosalon.migration.pos365.domainpos365.Pos365OrderStockDetail;
import com.jidian.cosalon.migration.pos365.domainpos365.Pos365Return;
import com.jidian.cosalon.migration.pos365.domainpos365.Pos365ReturnDetail;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component("imsReturnGoodsReceiptThread")
public class ImsReturnGoodsReceiptThread extends MyThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImsReturnGoodsReceiptThread.class);

    @Override
    public String getName() {
        return "ImsReturnGoodsReceiptThread";
    }

    private int insertedTotal = 0;
    private int assumptionTotal = 0;

    @Override
    public void doRun() {
        insertedTotal = 0;
        assumptionTotal = 0;
        try {
            final List<ReturnGoodsReceiptQueryDto> items = jdbcTemplate.query(
                    "select " +
                            "    a.id, a.branch_id, a.code, a.created_by, a.created_date, a.modified_by, a.modified_date, a.return_date, " +
                            "    a.description, a.discount, a.status, a.total, a.total_payment, a.order_id, a.partner_id, e.code as order_code, " +
                            "    d.ims_warehouse_id " +
                            "from p365_return a " +
                            "    left outer join p365_partners b on a.partner_id = b.id " +
                            "    left join p365_branchs_ims_warehouse d on a.branch_id = d.p365_branchs_id " +
                            "    left outer join p365_orders e on e.id = a.order_id " +
                            "order by a.id asc ",
                    (rs, rowNum) -> {
                    final ReturnGoodsReceiptQueryDto result = new ReturnGoodsReceiptQueryDto();
                    result.setId(rs.getLong("id"));
                    result.setBranchId(rs.getLong("branch_id"));
                    result.setCode(rs.getString("code"));
                    result.setCreatedDate(rs.getString("created_date"));
                    result.setCreatedBy(rs.getLong("created_by"));
                    result.setModifiedBy(rs.getLong("modified_by"));
                    result.setModifiedDate(rs.getString("modified_date"));
                    result.setReturnDate(rs.getString("modified_date"));
                    result.setDescription(rs.getString("description"));
                    result.setDiscount(rs.getBigDecimal("discount"));
                    result.setStatus(rs.getLong("status"));
                    result.setTotal(rs.getBigDecimal("total"));
                    result.setTotalPayment(rs.getBigDecimal("total_payment"));
                    result.setOrderId(rs.getLong("order_id"));
                    result.setPartnerId(rs.getLong("partner_id"));
                    result.setOrderCode(rs.getString("order_code"));
                    result.setImsWarehouseId(rs.getLong("ims_warehouse_id"));
                    return result;
                }
            );
            assumptionTotal = items.size();

            final List<ReturnGoodsReceiptChemicalQueryDto> details = jdbcTemplate.query(
                    "select " +
                            "    a.id, a.conversion_value, a.is_large_unit, a.price, a.product_id, a.quantity, a.return_id, " +
                            "    b.p365_products_id, b.ims_chemical_id, case when a.is_large_unit = 1 then d.id else e.id end as unit_id, " +
                            "    case when a.is_large_unit = 1 then d.name else e.name end as unit_name, " +
                            "    case when a.is_large_unit = 1 then d.type else e.type end as unit_type, " +
                            "    case when a.is_large_unit = 1 then d.standard_unit_exchange else e.standard_unit_exchange end as unit_exchange " +
                            "from p365_return_detail a " +
                            "    left outer join p365_products_ims_chemical b on a.product_id = b.p365_products_id " +
                            "    left outer join ims_chemical c on b.ims_chemical_id = c.id " +
                            "    left outer join (select * from ims_chemical_sub_unit a where a.type = 1) d on d.chemical_id = c.id " +
                            "    left outer join (select * from ims_chemical_sub_unit a where a.type = 2) e on e.chemical_id = c.id ",
                    (rs, rowNum) -> {
                        final ReturnGoodsReceiptChemicalQueryDto result = new ReturnGoodsReceiptChemicalQueryDto();
                        result.setId(rs.getLong("id"));
                        result.setConversionValue(rs.getInt("conversion_value"));
                        result.setIsLargeUnit(rs.getBoolean("is_large_unit"));
                        result.setPrice(rs.getBigDecimal("price"));
                        result.setProductId(rs.getLong("product_id"));
                        result.setQuantity(rs.getLong("quantity"));
                        result.setReturnId(rs.getLong("return_id"));
                        result.setImsChemicalId(rs.getLong("ims_chemical_id"));
                        result.setUnitId(rs.getLong("unit_id"));
                        result.setUnitType(rs.getInt("unit_type"));
                        result.setUnitName(rs.getString("unit_name"));
                        result.setUnitExchange(rs.getBigDecimal("unit_exchange"));
                        return result;
                    }
            );
            Map<Long, List<ReturnGoodsReceiptChemicalQueryDto>> detailMap = details.stream().collect(Collectors.groupingBy(Pos365ReturnDetail::getReturnId));

            final List<GoodsReceipt> existing = jdbcTemplate.query(
                    "select * from ims_goods_receipt a where a.type = 4 ",
                    (rs, rowNum) -> {
                        final GoodsReceipt result = new GoodsReceipt();
                        result.setId(rs.getLong("id"));
                        result.setType(rs.getInt("type"));
                        result.setReceiptCode(rs.getString("receipt_code"));
                        return result;
                    }
            );
            Map<String, List<GoodsReceipt>> existingMap = existing.stream().collect(Collectors.groupingBy(GoodsReceipt::getReceiptCode));
            List<ReturnGoodsReceiptQueryDto> inserts = new ArrayList<>();
            List<ReturnGoodsReceiptQueryDto> updates = new ArrayList<>();
            items.forEach(item -> {
                if (existingMap.containsKey(Utils.nvl(item.getCode()).trim())) {
                    item.setImsGoodsReceiptId(existingMap.get(Utils.nvl(item.getCode()).trim()).get(0).getId());
                    updates.add(item);
                } else {
                    inserts.add(item);
                }
            });

            inserts.forEach(item -> {
                KeyHolder keyHolder = new GeneratedKeyHolder();
                insertedTotal += jdbcTemplate.update(
                        connection -> {
                            PreparedStatement ps = connection.prepareStatement(
                                    "INSERT INTO ims_goods_receipt (gmt_create, gmt_modified, version, type, import_export, receipt_code, " +
                                            "    supplier_id, gmt_delivery, gmt_import, gmt_export, source_warehouse_id, dest_warehouse_id, " +
                                            "    total_quantity, total_pre_amount, deduction, total_amount, creator_id, editor_id, finisher_id, " +
                                            "    requester_id, requester_type, requester_name, requester_phone_num, order_num, reference, remark, status) " +
                                            "VALUES (CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP(),0,4,1,?, " +
                                            "    null,null,?,null,null,?, " +
                                            "    0,?,?,?,null,null,null, " +
                                            "    null,null,null,null,?,null,null,?) ",
                                    new String[] {"id"});
                            int i = 1;
                            ps.setString(i++, item.getCode());
                            ps.setTimestamp(i++, Utils.convertTimestamp(item.getReturnDate()));
                            ps.setLong(i++, item.getImsWarehouseId());
                            ps.setBigDecimal(i++, Utils.nvl(item.getTotal()));
                            ps.setBigDecimal(i++, Utils.nvl(item.getTotal()).intValue() == 0 ? BigDecimal.ZERO : Utils.nvl(item.getDiscount()).divide(item.getTotal(), 2, RoundingMode.HALF_UP));
                            ps.setBigDecimal(i++, Utils.nvl(item.getTotal().subtract(Utils.nvl(item.getDiscount()))));
                            ps.setString(i++, Utils.nvl(item.getOrderCode()));
                            ps.setLong(i++, StatusEnum.resolve(Utils.nvl(item.getStatus())) == null ? 10: StatusEnum.resolve(Utils.nvl(item.getStatus())).getGoodsReceiptStatus());
                            return ps;
                        },
                        keyHolder);
                List<ReturnGoodsReceiptChemicalQueryDto> subDetails = detailMap.get(item.getId());
                if (keyHolder.getKey() != null && subDetails != null) {
                    if (!subDetails.isEmpty()) {
                        jdbcTemplate.update("delete from ims_goods_receipt_chemical where goods_receipt_id = ?", keyHolder.getKey().longValue());
                    }
                    subDetails.forEach(subDetail -> {
                        jdbcTemplate.update(
                                "INSERT INTO ims_goods_receipt_chemical (gmt_create, gmt_modified, version, goods_receipt_id, " +
                                        "    chemical_id, unit_id, unit_type, unit_name, unit_price, standard_unit_exchange, quantity, total_price) " +
                                        "VALUES (CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP(),0,?, " +
                                        "    ?,?,?,?,?,?,?,?) ",
                                keyHolder.getKey().longValue(),
                                subDetail.getImsChemicalId(),
                                subDetail.getUnitId(),
                                subDetail.getUnitType(),
                                Utils.nvl(subDetail.getUnitName()),
                                subDetail.getPrice(),
                                subDetail.getUnitExchange(),
                                subDetail.getQuantity(),
                                Utils.nvl(subDetail.getPrice()).multiply(new BigDecimal(Utils.nvl(subDetail.getQuantity()))));
                    });

                }
            });

            updates.forEach(item -> {
                insertedTotal += jdbcTemplate.update(
                        connection -> {
                            PreparedStatement ps = connection.prepareStatement(
                                    "UPDATE ims_goods_receipt SET gmt_modified = CURRENT_TIMESTAMP(), version = version+1, type = 4, " +
                                            "    import_export = 1, receipt_code = ?, supplier_id = null, gmt_delivery = null, gmt_import = ?, gmt_export = null, " +
                                            "    source_warehouse_id = null, dest_warehouse_id = ?, total_quantity = 0, total_pre_amount = ?, deduction = ?, total_amount = ?, " +
                                            "    creator_id = null, editor_id = null, finisher_id = null, requester_id = null, requester_type = null, requester_name = null, requester_phone_num = null, " +
                                            "    order_num = ?, reference = null, remark = null, status = ? WHERE id = ? ",
                                    new String[] {"id"});
                            int i = 1;
                            ps.setString(i++, item.getCode());
                            ps.setTimestamp(i++, Utils.convertTimestamp(item.getReturnDate()));
                            ps.setLong(i++, item.getImsWarehouseId());
                            ps.setBigDecimal(i++, Utils.nvl(item.getTotal()));
                            ps.setBigDecimal(i++, Utils.nvl(item.getTotal()).intValue() == 0 ? BigDecimal.ZERO : Utils.nvl(item.getDiscount()).divide(item.getTotal(), 2, RoundingMode.HALF_UP));
                            ps.setBigDecimal(i++, Utils.nvl(item.getTotal().subtract(Utils.nvl(item.getDiscount()))));
                            ps.setString(i++, Utils.nvl(item.getOrderCode()));
                            ps.setLong(i++, StatusEnum.resolve(Utils.nvl(item.getStatus())) == null ? 10: StatusEnum.resolve(Utils.nvl(item.getStatus())).getGoodsReceiptStatus());
                            ps.setLong(i++, item.getImsGoodsReceiptId());
                            return ps;
                        });
                List<ReturnGoodsReceiptChemicalQueryDto> subDetails = detailMap.get(item.getId());
                if (subDetails != null) {
                    if (!subDetails.isEmpty()) {
                        jdbcTemplate.update("delete from ims_goods_receipt_chemical where goods_receipt_id = ?", item.getImsGoodsReceiptId());
                    }
                    subDetails.forEach(subDetail -> {
                        jdbcTemplate.update(
                                "INSERT INTO ims_goods_receipt_chemical (gmt_create, gmt_modified, version, goods_receipt_id, " +
                                        "    chemical_id, unit_id, unit_type, unit_name, unit_price, standard_unit_exchange, quantity, total_price) " +
                                        "VALUES (CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP(),0,?, " +
                                        "    ?,?,?,?,?,?,?,?) ",
                                item.getImsGoodsReceiptId(),
                                subDetail.getImsChemicalId(),
                                subDetail.getUnitId(),
                                subDetail.getUnitType(),
                                Utils.nvl(subDetail.getUnitName()),
                                subDetail.getPrice(),
                                subDetail.getUnitExchange(),
                                subDetail.getQuantity(),
                                Utils.nvl(subDetail.getPrice()).multiply(new BigDecimal(Utils.nvl(subDetail.getQuantity()))));
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
    private static class ReturnGoodsReceiptQueryDto extends Pos365Return {
        private Long imsWarehouseId;
        private Long imsGoodsReceiptId;
        private String orderCode;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class ReturnGoodsReceiptChemicalQueryDto extends Pos365ReturnDetail {
        private Long imsChemicalId;
        private Long unitId;
        private Integer unitType;
        private String unitName;
        private BigDecimal unitExchange;
    }

    private enum StatusEnum {
        EXECUTING(1L, 10L),
        FINISHED(2L, 40L),
        CANCELED(3L, 30L)
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
