package com.jidian.cosalon.migration.pos365.thread.imsimpl;

import com.jidian.cosalon.migration.pos365.Utils;
import com.jidian.cosalon.migration.pos365.domain.GoodsReceipt;
import com.jidian.cosalon.migration.pos365.domainpos365.Pos365OrderStock;
import com.jidian.cosalon.migration.pos365.domainpos365.Pos365OrderStockDetail;
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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component("imsImportGoodsReceiptThread")
public class ImsImportGoodsReceiptThread extends MyThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImsImportGoodsReceiptThread.class);

    @Override
    public String getName() {
        return "ImsImportGoodsReceiptThread";
    }

    private int insertedTotal = 0;
    private int assumptionTotal = 0;
    private long defaultSupplierId = 26;

    @Override
    public void doRun() {
        insertedTotal = 0;
        assumptionTotal = 0;
        try {
            final List<ImportGoodsReceiptQueryDto> items = jdbcTemplate.query(
                    "select " +
                            "    a.id, a.branch_id, a.code, a.created_by, a.created_date, a.delivery_date, a.modified_by, a.modified_date, " +
                            "    a.discount, a.document_date, a.exchange_rate, a.status, a.total, a.total_payment, a.vat, a.partner_id, " +
                            "    a.discount, c.id as supplier_id, d.ims_warehouse_id " +
                            "from p365_order_stock a " +
                            "left outer join ( " +
                            "    select * from p365_partners p where p.type = 2) b on a.partner_id = b.id " +
                            "left outer join ims_supplier c on b.code = c.code " +
                            "left join p365_branchs_ims_warehouse d on a.branch_id = d.p365_branchs_id " +
                            "order by a.id asc ",
                    (rs, rowNum) -> {
                        final ImportGoodsReceiptQueryDto result = new ImportGoodsReceiptQueryDto();
                        result.setId(rs.getLong("id"));
                        result.setBranchId(rs.getLong("branch_id"));
                        result.setCode(rs.getString("code"));
                        result.setCreatedDate(rs.getTimestamp("created_date"));
                        result.setCreatedBy(rs.getLong("created_by"));
                        result.setModifiedBy(rs.getLong("modified_by"));
                        result.setModifiedDate(rs.getTimestamp("modified_date"));
                        result.setDeliveryDate(rs.getTimestamp("delivery_date"));
                        result.setDiscount(rs.getBigDecimal("discount"));
                        result.setDocumentDate(rs.getTimestamp("document_date"));
                        result.setExchangeRate(rs.getBigDecimal("exchange_rate"));
                        result.setStatus(rs.getInt("status"));
                        result.setTotal(rs.getBigDecimal("total"));
                        result.setTotalPayment(rs.getBigDecimal("total_payment"));
                        result.setVat(rs.getBigDecimal("vat"));
                        result.setPartnerId(rs.getLong("partner_id"));
                        result.setDiscount(rs.getBigDecimal("discount"));
                        result.setImsSupplierId(rs.getLong("supplier_id") == 0L ? defaultSupplierId: rs.getLong("supplier_id"));
                        result.setImsWarehouseId(rs.getLong("ims_warehouse_id"));
                        return result;
                    }
            );
            assumptionTotal = items.size();

            final List<ImportGoodsReceiptChemicalQueryDto> details = jdbcTemplate.query(
                    "select " +
                            "    a.id, a.conversion_value, a.description, a.is_large_unit, a.order_quantity, a.price, a.product_id, a.purchase_order_id, " +
                            "    a.quantity, a.selling_price, b.p365_products_id, b.ims_chemical_id, c.standard_unit " +
                            "from p365_order_stock_detail a " +
                            "left outer join p365_products_ims_chemical b on a.product_id = b.p365_products_id " +
                            "left outer join ims_chemical c on b.ims_chemical_id = c.id ",
                    (rs, rowNum) -> {
                        final ImportGoodsReceiptChemicalQueryDto result = new ImportGoodsReceiptChemicalQueryDto();
                        result.setId(rs.getLong("id"));
                        result.setConversionValue(rs.getInt("conversion_value"));
                        result.setDescription(rs.getString("description"));
                        result.setIsLargeUnit(rs.getBoolean("is_large_unit"));
                        result.setOrderQuantity(rs.getInt("order_quantity"));
                        result.setPrice(rs.getBigDecimal("price"));
                        result.setProductId(rs.getLong("product_id"));
                        result.setPurchaseOrderId(rs.getLong("purchase_order_id"));
                        result.setQuantity(rs.getInt("quantity"));
                        result.setSellingPrice(rs.getBigDecimal("selling_price"));
                        result.setSellingPrice(rs.getBigDecimal("selling_price"));
                        result.setImsChemicalId(rs.getLong("ims_chemical_id"));
                        result.setStandardUnit(rs.getString("standard_unit"));
                        return result;
                    }
            );
            Map<Long, List<ImportGoodsReceiptChemicalQueryDto>> detailMap = details.stream().collect(Collectors.groupingBy(Pos365OrderStockDetail::getPurchaseOrderId));

            final List<GoodsReceipt> existing = jdbcTemplate.query(
                    "select * from ims_goods_receipt a where a.type = 1 ",
                    (rs, rowNum) -> {
                        final GoodsReceipt result = new GoodsReceipt();
                        result.setId(rs.getLong("id"));
                        result.setType(rs.getInt("type"));
                        result.setReceiptCode(rs.getString("receipt_code"));
                        return result;
                    }
            );
            Map<String, List<GoodsReceipt>> existingMap = existing.stream().collect(Collectors.groupingBy(GoodsReceipt::getReceiptCode));
            List<ImportGoodsReceiptQueryDto> inserts = new ArrayList<>();
            List<ImportGoodsReceiptQueryDto> updates = new ArrayList<>();
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
                                        "VALUES (CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP(),0,1,1,?, " +
                                        "    ?,?,?,null,null,?, " +
                                        "    0,?,?,?,1,null,null, " +
                                        "    null,null,null,null,null,null,null,?) ",
                                    new String[] {"id"});
                            int i = 1;
                            ps.setString(i++, item.getCode());
                            ps.setLong(i++, item.getImsSupplierId());
                            ps.setTimestamp(i++, item.getDeliveryDate());
                            ps.setTimestamp(i++, item.getDeliveryDate());
                            ps.setLong(i++, item.getImsWarehouseId());
                            ps.setBigDecimal(i++, Utils.nvl(item.getTotal()));
                            ps.setBigDecimal(i++, Utils.nvl(item.getTotal()).intValue() == 0 ? BigDecimal.ZERO : Utils.nvl(item.getDiscount()).divide(item.getTotal(), 2, RoundingMode.HALF_UP));
                            ps.setBigDecimal(i++, Utils.nvl(item.getTotal().subtract(Utils.nvl(item.getDiscount()))));
                            ps.setInt(i++, StatusEnum.resolve(Utils.nvl(item.getStatus())) == null ? 10: StatusEnum.resolve(Utils.nvl(item.getStatus())).getGoodsReceiptStatus());
                            return ps;
                        },
                        keyHolder);
                List<ImportGoodsReceiptChemicalQueryDto> subDetails = detailMap.get(item.getId());
                if (keyHolder.getKey() != null && subDetails != null) {
                    if (!subDetails.isEmpty()) {
                        jdbcTemplate.update("delete from ims_goods_receipt_chemical where goods_receipt_id = ?", keyHolder.getKey().longValue());
                    }
                    subDetails.forEach(subDetail -> {
                        jdbcTemplate.update(
                                "INSERT INTO ims_goods_receipt_chemical (gmt_create, gmt_modified, version, goods_receipt_id, " +
                                        "    chemical_id, unit_id, unit_type, unit_name, unit_price, standard_unit_exchange, quantity, total_price) " +
                                        "VALUES (CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP(),0,?, " +
                                        "    ?,null,9,?,?,null,?,?) ",
                                keyHolder.getKey().longValue(),
                                subDetail.getImsChemicalId(),
                                subDetail.getStandardUnit(),
                                subDetail.getPrice(),
                                subDetail.getQuantity(),
                                Utils.nvl(subDetail.getPrice()).multiply(new BigDecimal(Utils.nvl(subDetail.getQuantity()))));
                    });

                }
            });
            updates.forEach(item -> {
                insertedTotal += jdbcTemplate.update(
                        connection -> {
                            PreparedStatement ps = connection.prepareStatement(
                                    "UPDATE ims_goods_receipt SET gmt_modified = CURRENT_TIMESTAMP(), version = version+1, type = 1, " +
                                            "    import_export = 1, receipt_code = ?, supplier_id = ?, gmt_delivery = ?, gmt_import = ?, gmt_export = null, " +
                                            "    source_warehouse_id = null, dest_warehouse_id = ?, total_quantity = 0, total_pre_amount = ?, deduction = ?, total_amount = ?, " +
                                            "    creator_id = 1, editor_id = null, finisher_id = null, requester_id = null, requester_type = null, requester_name = null, requester_phone_num = null, " +
                                            "    order_num = null, reference = null, remark = null, status = ? WHERE id = ? ",
                                    new String[] {"id"});
                            int i = 1;
                            ps.setString(i++, item.getCode());
                            ps.setLong(i++, item.getImsSupplierId());
                            ps.setTimestamp(i++, item.getDeliveryDate());
                            ps.setTimestamp(i++, item.getDeliveryDate());
                            ps.setLong(i++, item.getImsWarehouseId());
                            ps.setBigDecimal(i++, Utils.nvl(item.getTotal()));
                            ps.setBigDecimal(i++, Utils.nvl(item.getTotal()).intValue() == 0 ? BigDecimal.ZERO : Utils.nvl(item.getDiscount()).divide(item.getTotal(), 2, RoundingMode.HALF_UP));
                            ps.setBigDecimal(i++, Utils.nvl(item.getTotal().subtract(Utils.nvl(item.getDiscount()))));
                            ps.setString(i++, Utils.nvl(item.getOrderCode()));
                            ps.setLong(i++, StatusEnum.resolve(Utils.nvl(item.getStatus())) == null ? 10: StatusEnum.resolve(Utils.nvl(item.getStatus())).getGoodsReceiptStatus());
                            ps.setLong(i++, item.getImsGoodsReceiptId());
                            return ps;
                        });
                List<ImportGoodsReceiptChemicalQueryDto> subDetails = detailMap.get(item.getId());
                if (subDetails != null) {
                    if (!subDetails.isEmpty()) {
                        jdbcTemplate.update("delete from ims_goods_receipt_chemical where goods_receipt_id = ?", item.getImsGoodsReceiptId());
                    }
                    subDetails.forEach(subDetail -> {
                        jdbcTemplate.update(
                                "INSERT INTO ims_goods_receipt_chemical (gmt_create, gmt_modified, version, goods_receipt_id, " +
                                        "    chemical_id, unit_id, unit_type, unit_name, unit_price, standard_unit_exchange, quantity, total_price) " +
                                        "VALUES (CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP(),0,?, " +
                                        "    ?,null,9,?,?,null,?,?) ",
                                item.getImsGoodsReceiptId(),
                                subDetail.getImsChemicalId(),
                                subDetail.getStandardUnit(),
                                subDetail.getPrice(),
                                subDetail.getQuantity(),
                                Utils.nvl(subDetail.getPrice()).multiply(new BigDecimal(Utils.nvl(subDetail.getQuantity()))));
                    });

                }
            });

            // update gia tieu chuan cho chemical
            Map<Long, Optional<ImportGoodsReceiptChemicalQueryDto>> prices = details.stream()
                    .collect(Collectors.groupingBy(ImportGoodsReceiptChemicalQueryDto::getImsChemicalId, Collectors.maxBy(Comparator.comparing(Pos365OrderStockDetail::getId))));
            prices.forEach((chemicalId, optDto) -> {
                optDto.ifPresent(importGoodsReceiptChemicalQueryDto -> jdbcTemplate.update("UPDATE ims_chemical SET price_standard_unit = ? WHERE id = ?",
                        importGoodsReceiptChemicalQueryDto.getIsLargeUnit() != null && importGoodsReceiptChemicalQueryDto.getIsLargeUnit() ? Utils.nvl(importGoodsReceiptChemicalQueryDto.getPrice())
                                : (Utils.nvl(importGoodsReceiptChemicalQueryDto.getPrice()).multiply(new BigDecimal(importGoodsReceiptChemicalQueryDto.getConversionValue()))),
                        chemicalId));
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
    private static class ImportGoodsReceiptQueryDto extends Pos365OrderStock {
        private Long imsSupplierId;
        private Long imsWarehouseId;
        private Long imsGoodsReceiptId;
        private String orderCode;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class ImportGoodsReceiptChemicalQueryDto extends Pos365OrderStockDetail {
        private Long imsChemicalId;
        private String standardUnit;
    }

    private enum StatusEnum {
        EXECUTING(1, 10),
        FINISHED(2, 40),
        CANCELED(3, 30)
        ;

        @Getter
        private final int value;
        @Getter
        private final int goodsReceiptStatus;

        StatusEnum(int value, int grStatus) {
            this.value = value;
            this.goodsReceiptStatus = grStatus;
        }

        public static StatusEnum resolve(int value) {
            for (StatusEnum mEnum : values()) {
                if (mEnum.value == value) {
                    return mEnum;
                }
            }
            return null;
        }
    }
}
