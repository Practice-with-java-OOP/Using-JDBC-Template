package com.jidian.cosalon.migration.pos365.thread.imsimpl;

import com.jidian.cosalon.migration.pos365.Utils;
import com.jidian.cosalon.migration.pos365.Utils.StatusEnum;
import com.jidian.cosalon.migration.pos365.domainpos365.Pos365Transfer;
import com.jidian.cosalon.migration.pos365.domainpos365.Pos365TransfersDetail;
import com.jidian.cosalon.migration.pos365.thread.MyThread;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component("imsTransferGoodsReceiptThread")
public class ImsTransferGoodsReceiptThread extends MyThread {

    private static final Logger LOGGER = LoggerFactory
        .getLogger(ImsTransferGoodsReceiptThread.class);

    private int insertedTotal = 0;
    private int assumptionTotal = 0;

    @Override
    public String getName() {
        return "ImsTransferGoodsReceiptThread";
    }

    @Override
    public void doRun() {
        try {
            final List<TransferReceiptQueryDto> transfers = jdbcTemplate.query("select "
                    + " a.id, a.code, a.from_branch_id, b.ims_warehouse_id as source_warehouse_id, "
                    + " a.to_branch_id, b1.ims_warehouse_id as dest_warehouse_id, "
                    + " a.created_by, a.created_date, a.modified_by, a.modified_date, "
                    + " a.document_date, a.status, a.retailer_id "
                    + " from p365_transfers a "
                    + " left outer join p365_branchs_ims_warehouse b on a.from_branch_id = b.p365_branchs_id "
                    + " left outer join p365_branchs_ims_warehouse b1 on a.to_branch_id = b1.p365_branchs_id",
                (rs, rowNum) -> {
                    final TransferReceiptQueryDto result = new TransferReceiptQueryDto();
                    result.setId(rs.getLong("id"));
                    result.setCode(rs.getString("code"));
                    result.setFromBranchId(rs.getLong("from_branch_id"));
                    result.setSourceWarehouseId(rs.getLong("source_warehouse_id"));
                    result.setToBranchId(rs.getLong("to_branch_id"));
                    result.setDestWarehouseId(rs.getLong("dest_warehouse_id"));
                    result.setCreatedBy(rs.getLong("created_by"));
                    result.setCreatedDate(rs.getString("created_date"));
                    result.setDocumentDate(rs.getString("document_date"));
                    result.setModifiedBy(rs.getString("modified_by"));
                    result.setModifiedDate(rs.getString("modified_date"));
                    result.setStatus(rs.getInt("status"));
                    return result;
                }
            );

            assumptionTotal = transfers.size();

            final List<TransferChemicalDetailQueryDto> details = jdbcTemplate.query("select "
                + " a.id, a.product_id,a.product_type, a.quantity, a.transfer_id, a.conversion_value,"
                + " a.price, a.price_large_unit, b.p365_products_id, b.ims_chemical_id,"
                + " c.standard_unit, c.name as chemical_name"
                + " from p365_transfers_detail a"
                + " left outer join p365_products_ims_chemical b on a.product_id = b.p365_products_id"
                + " left outer join ims_chemical c on b.ims_chemical_id = c.id", (rs, rowNum) -> {
                final TransferChemicalDetailQueryDto result = new TransferChemicalDetailQueryDto();
                result.setId(rs.getLong("id"));
                result.setProductId(rs.getLong("product_id"));
                result.setProductType(rs.getInt("product_type"));
                result.setQuantity(rs.getInt("quantity"));
                result.setTransferId(rs.getLong("transfer_id"));
                result.setConversionValue(rs.getInt("conversion_value"));
                result.setPrice(rs.getBigDecimal("price"));
                result.setPriceLargeUnit(rs.getBigDecimal("price_large_unit"));
                result.setProductId(rs.getLong("product_id"));
                result.setImsChemicalId(rs.getLong("ims_chemical_id"));
                result.setStandardUnit(rs.getString("standard_unit"));
                result.setChemicalName(rs.getString("chemical_name"));
                return result;
            });

            Map<Long, List<TransferChemicalDetailQueryDto>> idDetailMap = details.stream().collect(
                Collectors.groupingBy(Pos365TransfersDetail::getTransferId));

            transfers.forEach(transaction -> {
                KeyHolder keyHolder = new GeneratedKeyHolder();
                insertedTotal += jdbcTemplate.update(
                    connection -> {
                        PreparedStatement ps = connection.prepareStatement(
                            "insert into ims_goods_receipt"
                                + " (gmt_create, gmt_modified, version, type, import_export,"
                                + " receipt_code, supplier_id, gmt_delivery, gmt_import, "
                                + " gmt_export,source_warehouse_id, dest_warehouse_id, "
                                + " total_quantity,total_pre_amount, deduction, total_amount, "
                                + " creator_id, editor_id,finisher_id, requester_id, requester_type,"
                                + " requester_name,requester_phone_num, order_num, reference, remark, status)"
                                + " values (?, ?, 0, 2, 3, ?, null, null, null, ?, ?, ?, 0, 0, 0, 0,1, "
                                + " null, null, null, null, null, null, null, null, null, ?)"
                                + " on duplicate key update gmt_modified = CURRENT_TIMESTAMP(), "
                                + " version = version + 1,type = 2, import_export = 3, "
                                + " receipt_code = ?,supplier_id = null, gmt_delivery = null, "
                                + " gmt_import = null, gmt_export = ?,source_warehouse_id = ?,"
                                + " dest_warehouse_id = ?,total_quantity = 0,total_pre_amount = 0,"
                                + " deduction = 0,total_amount = 0,creator_id = null,"
                                + " editor_id = null,finisher_id = null,requester_id = null,"
                                + " requester_type = null,requester_name = null,"
                                + " requester_phone_num = null,order_num = null,reference = null,"
                                + " remark = null,status = ?",
                            new String[]{"id"});
                        int index = 1;
                        ps.setTimestamp(index++, convertTimestamp(transaction.getCreatedDate()));
                        ps.setTimestamp(index++, convertTimestamp(transaction.getModifiedDate()));
                        ps.setString(index++, transaction.getCode());
                        ps.setTimestamp(index++, convertTimestamp(transaction.getDocumentDate()));
                        ps.setLong(index++, transaction.getSourceWarehouseId());
                        ps.setLong(index++, transaction.getDestWarehouseId());
                        ps.setInt(index++,
                            Utils.StatusEnum.resolve(Utils.nvl(transaction.getStatus())) == null
                                ? 10 : Objects.requireNonNull(
                                StatusEnum.resolve(Utils.nvl(transaction.getStatus()))).getGoodsReceiptStatus());
                        ps.setString(index++, transaction.getCode());
                        ps.setTimestamp(index++, convertTimestamp(transaction.getDocumentDate()));
                        ps.setLong(index++, transaction.getSourceWarehouseId());
                        ps.setLong(index++, transaction.getDestWarehouseId());
                        ps.setInt(index++,
                            Utils.StatusEnum.resolve(Utils.nvl(transaction.getStatus())) == null
                                ? 10 : Objects.requireNonNull(
                                StatusEnum.resolve(Utils.nvl(transaction.getStatus()))).getGoodsReceiptStatus());
                        return ps;
                    },
                    keyHolder);
                List<TransferChemicalDetailQueryDto> subDetails = idDetailMap
                    .get(transaction.getId());
                if (keyHolder.getKey() != null && subDetails != null) {
                    subDetails.forEach(
                        detail -> jdbcTemplate.update("insert into ims_goods_receipt_chemical "
                                + " (gmt_create, gmt_modified, version, goods_receipt_id, "
                                + "  chemical_id, unit_id, unit_type, unit_name, unit_price, "
                                + "  standard_unit_exchange, quantity, total_price) "
                                + " values (CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 0, ?, ?, null, 9, "
                                + " ?, ?, null, ?, 0)  "
                                + " on DUPLICATE key update "
                                + " gmt_modified = CURRENT_TIMESTAMP(), version = version + 1, "
                                + " goods_receipt_id = ?, chemical_id = ?, unit_id = null, "
                                + " unit_type = 9, unit_name = ?, unit_price =  ?, "
                                + " standard_unit_exchange = null, quantity  = ?, total_price = 0",
                            keyHolder.getKey().longValue(),
                            detail.getImsChemicalId(),
                            detail.getStandardUnit(),
                            detail.getPrice(),
                            detail.getQuantity(),
                            keyHolder.getKey().longValue(),
                            detail.getImsChemicalId(),
                            detail.getStandardUnit(),
                            detail.getPrice(),
                            detail.getQuantity()));
                }
            });
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            LOGGER.info("SUMMARY: insert/update total: {}, Pos365Transfer total: {}", insertedTotal,
                assumptionTotal);
        }
    }

    private Timestamp convertTimestamp(String stringDate) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        if (stringDate != null) {
            try {
                ZonedDateTime zonedDateTime2 = ZonedDateTime
                    .parse(stringDate, DateTimeFormatter.ISO_DATE_TIME);
                timestamp = Timestamp.valueOf(zonedDateTime2.toLocalDateTime());
                return timestamp;
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        } else {
            timestamp = null;
        }
        return timestamp;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private class TransferReceiptQueryDto extends Pos365Transfer {

        private Long sourceWarehouseId;
        private Long destWarehouseId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private class TransferChemicalDetailQueryDto extends Pos365TransfersDetail {

        private Long imsChemicalId;
        private String standardUnit;
        private String chemicalName;
    }
}
