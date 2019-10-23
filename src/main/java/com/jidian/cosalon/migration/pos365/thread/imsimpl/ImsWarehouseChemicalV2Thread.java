package com.jidian.cosalon.migration.pos365.thread.imsimpl;

import com.jidian.cosalon.migration.pos365.Utils;
import com.jidian.cosalon.migration.pos365.domainpos365.Pos365ProductHistory;
import com.jidian.cosalon.migration.pos365.domainpos365.Pos365ProductOnHandByBranch;
import com.jidian.cosalon.migration.pos365.thread.MyThread;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.util.List;

@Component("imsWarehouseChemicalV2Thread")
public class ImsWarehouseChemicalV2Thread extends MyThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImsWarehouseChemicalV2Thread.class);

    @Override
    public String getName() {
        return "ImsWarehouseChemicalV2Thread";
    }

    private int insertedTotal = 0;
    private int assumptionTotal = 0;
    private long currChemicalId = 0L;
    private long currWarehouseId = 0L;
    private long currWarehouseChemicalId = 0L;

    @Override
    public void doRun() {
        insertedTotal = 0;
        assumptionTotal = 0;
        try {
            final List<ProductOnHandChemicalWarehouseQueryDto> items = jdbcTemplate.query(
                    "select a.branch_id, a.product_id, a.cost, a.ending_stocks, a.price, a.document_code, a.document_id, a.document_type, " +
                            "    a.quantity, a.trans_date, c.p365_products_id, c.ims_chemical_id, b.p365_branchs_id, b.ims_warehouse_id, " +
                            "    d.code as chemical_code, e.id as unit_id, e.type as unit_type, " +
                            "    e.name as unit_name, e.price as unit_price, e.standard_unit_exchange as unit_exchange " +
                            "from p365_products_history a " +
                            "         inner join p365_products_ims_chemical c on a.product_id = c.p365_products_id " +
                            "         left outer join p365_branchs_ims_warehouse b on a.branch_id = b.p365_branchs_id " +
                            "         left outer join ims_chemical d on c.ims_chemical_id = d.id " +
                            "         left outer join ( " +
                            "    select * from ims_chemical_sub_unit a where a.type = 3) e on d.id = e.chemical_id " +
                            "order by a.product_id, a.branch_id asc, a.id desc ",
                    (rs, rowNum) -> {
                    final ProductOnHandChemicalWarehouseQueryDto result = new ProductOnHandChemicalWarehouseQueryDto();
                    result.setBranchId(rs.getLong("branch_id"));
                    result.setProductId(rs.getLong("product_id"));
                    result.setCost(rs.getBigDecimal("cost"));
                    result.setEndingStocks(rs.getLong("ending_stocks"));
                    result.setPrice(rs.getBigDecimal("price"));
                    result.setDocumentCode(rs.getString("document_code"));
                    result.setDocumentId(rs.getLong("document_id"));
                    result.setDocumentType(rs.getLong("document_type"));
                    result.setQuantity(rs.getLong("quantity"));
                    result.setTransDate(rs.getString("trans_date"));
                    result.setP365BranchId(rs.getLong("p365_branchs_id"));
                    result.setImsWarehouseId(rs.getLong("ims_warehouse_id"));
                    result.setP365ProductId(rs.getLong("p365_products_id"));
                    result.setImsChemicalId(rs.getLong("ims_chemical_id"));
                    result.setChemicalCode(rs.getString("chemical_code"));
                    result.setUnitId(rs.getLong("unit_id"));
                    result.setUnitType(rs.getLong("unit_type"));
                    result.setUnitName(rs.getString("unit_name"));
                    result.setUnitPrice(rs.getBigDecimal("unit_price"));
                    result.setUnitExchange(rs.getBigDecimal("unit_exchange"));
                    return result;
                }
            );
            assumptionTotal = items.size();

            currChemicalId = 0L;
            currWarehouseId = 0L;
            items.forEach(item -> {
                KeyHolder keyHolder = new GeneratedKeyHolder();
                if (currChemicalId != item.getImsChemicalId() || currWarehouseId != item.getImsWarehouseId()) {
                    insertedTotal += jdbcTemplate.update(
                            connection -> {
                                PreparedStatement ps = connection.prepareStatement(
                                        "INSERT INTO ims_warehouse_chemical (gmt_create, gmt_modified, version, warehouse_id, chemical_id, chemical_code, chemical_item_sku_id, " +
                                                "    unit_id, unit_type, unit_name, unit_price, standard_unit_exchange, package_weight, package_unit, " +
                                                "    quantity, delta, total_price, ref_goods_receipt_id, ref_goods_receipt_type, import_export) " +
                                                "VALUES (CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP(),0,?,?,?,0, " +
                                                "    ?,?,?,?,?,null,null, " +
                                                "    ?,?,?,null,null,?) " +
                                                "ON DUPLICATE KEY UPDATE gmt_modified = CURRENT_TIMESTAMP(), version = version+1, chemical_code = ?, chemical_item_sku_id = 0, " +
                                                "    unit_id = ?, unit_type = ?, unit_name = ?, unit_price = ?, standard_unit_exchange = ?, package_weight = null, package_unit = null, " +
                                                "    quantity = ?, delta = ?, total_price = ?, ref_goods_receipt_id = null, ref_goods_receipt_type = null, import_export = ?",
                                        new String[] {"id"});
                                int i = 1;
                                ps.setLong(i++, item.getImsWarehouseId());
                                ps.setLong(i++, item.getImsChemicalId());
                                ps.setString(i++, Utils.nvl(item.getChemicalCode()));
                                ps.setLong(i++, Utils.nvl(item.getUnitId()));
                                ps.setLong(i++, Utils.nvl(item.getUnitType()));
                                ps.setString(i++, Utils.nvl(item.getUnitName()));
                                ps.setBigDecimal(i++, Utils.nvl(item.getPrice()));
                                ps.setBigDecimal(i++, Utils.nvl(item.getUnitExchange()));
                                ps.setBigDecimal(i++, new BigDecimal(Utils.nvl(item.getEndingStocks())));
                                ps.setBigDecimal(i++, new BigDecimal(Utils.nvl(item.getQuantity())));
                                ps.setBigDecimal(i++, Utils.nvl(item.getPrice()).multiply(new BigDecimal(Utils.nvl(item.getEndingStocks()))));
                                ps.setLong(i++, item.getDocumentType() == null || item.getDocumentType() == 0L ? 0L :
                                        (Utils.POS365_IMPORT.contains(Utils.nvl(item.getDocumentType())) ? 1L : 2L));
                                ps.setString(i++, Utils.nvl(item.getChemicalCode()));
                                ps.setLong(i++, Utils.nvl(item.getUnitId()));
                                ps.setLong(i++, Utils.nvl(item.getUnitType()));
                                ps.setString(i++, Utils.nvl(item.getUnitName()));
                                ps.setBigDecimal(i++, Utils.nvl(item.getUnitPrice()));
                                ps.setBigDecimal(i++, Utils.nvl(item.getUnitExchange()));
                                ps.setBigDecimal(i++, new BigDecimal(Utils.nvl(item.getEndingStocks())));
                                ps.setBigDecimal(i++, new BigDecimal(Utils.nvl(item.getQuantity())));
                                ps.setBigDecimal(i++, Utils.nvl(item.getPrice()).multiply(new BigDecimal(Utils.nvl(item.getEndingStocks()))));
                                ps.setLong(i++, item.getDocumentType() == null || item.getDocumentType() == 0L ? 0L :
                                        (Utils.POS365_IMPORT.contains(Utils.nvl(item.getDocumentType())) ? 1L : 2L));
                                return ps;
                            },
                            keyHolder);
                    currChemicalId = item.getImsChemicalId();
                    currWarehouseId = item.getImsWarehouseId();
                    currWarehouseChemicalId = keyHolder.getKey() == null ? 0L: keyHolder.getKey().longValue();
                } else {
                    if (currWarehouseChemicalId > 0) {
                        jdbcTemplate.update("delete from ims_warehouse_chemical_history where warehouse_chemical_id = ?", currWarehouseChemicalId);
                    }
                    insertedTotal += jdbcTemplate.update(
                            connection -> {
                                PreparedStatement ps = connection.prepareStatement(
                                        "INSERT INTO ims_warehouse_chemical_history (gmt_create, gmt_modified, version, warehouse_chemical_id, warehouse_id, " +
                                                "    chemical_id, chemical_code, chemical_item_sku_id, unit_id, unit_type, unit_name, unit_price, package_weight, package_unit, " +
                                                "    standard_unit_exchange, quantity, delta, total_price, ref_goods_receipt_id, ref_goods_receipt_type, import_export) " +
                                                "VALUES (CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP(),0,?,?, " +
                                                "    ?,?,0,?,?,?,?,null,null, " +
                                                "    ?,?,?,?,null,null,?) ",
                                        new String[] {"id"});
                                int i = 1;
                                ps.setLong(i++, currWarehouseChemicalId);
                                ps.setLong(i++, item.getImsWarehouseId());
                                ps.setLong(i++, item.getImsChemicalId());
                                ps.setString(i++, Utils.nvl(item.getChemicalCode()));
                                ps.setLong(i++, Utils.nvl(item.getUnitId()));
                                ps.setLong(i++, Utils.nvl(item.getUnitType()));
                                ps.setString(i++, Utils.nvl(item.getUnitName()));
                                ps.setBigDecimal(i++, Utils.nvl(item.getPrice()));
                                ps.setBigDecimal(i++, Utils.nvl(item.getUnitExchange()));
                                ps.setBigDecimal(i++, new BigDecimal(Utils.nvl(item.getEndingStocks())));
                                ps.setBigDecimal(i++, new BigDecimal(Utils.nvl(item.getQuantity())));
                                ps.setBigDecimal(i++, Utils.nvl(item.getPrice()).multiply(new BigDecimal(Utils.nvl(item.getEndingStocks()))));
                                ps.setLong(i++, item.getDocumentType() == null || item.getDocumentType() == 0L ? 0L :
                                        (Utils.POS365_IMPORT.contains(Utils.nvl(item.getDocumentType())) ? 1L : 2L));
                                return ps;
                            },
                            keyHolder);
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
    private static class ProductOnHandChemicalWarehouseQueryDto extends Pos365ProductHistory {
        private Long p365BranchId;
        private Long imsWarehouseId;
        private Long p365ProductId;
        private Long imsChemicalId;
        private String chemicalCode;
        private Long unitId;
        private Long unitType;
        private String unitName;
        private BigDecimal unitPrice;
        private BigDecimal unitExchange;
    }
}
