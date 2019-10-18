package com.jidian.cosalon.migration.pos365.thread.imsimpl;

import com.jidian.cosalon.migration.pos365.Utils;
import com.jidian.cosalon.migration.pos365.domain.Warehouse;
import com.jidian.cosalon.migration.pos365.domainpos365.Pos365Branch;
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
import java.util.Map;
import java.util.stream.Collectors;

@Component("imsWarehouseChemicalThread")
public class ImsWarehouseChemicalThread extends MyThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImsWarehouseChemicalThread.class);

    @Override
    public String getName() {
        return "ImsWarehouseChemicalThread";
    }

    private int insertedTotal = 0;
    private int assumptionTotal = 0;

    @Override
    public void doRun() {
        insertedTotal = 0;
        assumptionTotal = 0;
        try {
            // mirgate data
            final List<ProductOnHandChemicalWarehouseQueryDto> items = jdbcTemplate.query("select a.branch_id, a.product_id, a.cost, a.created_by, a.created_date, a.modified_by, a.modified_date, " +
                            "    a.on_hand, a.price_by_branch, a.price_by_branch_large_unit, b.p365_branchs_id, b.ims_warehouse_id, " +
                            "    c.p365_products_id, c.ims_chemical_id, d.code as chemical_code, e.id as unit_id, e.type as unit_type, " +
                            "    e.name as unit_name, e.price as unit_price, e.standard_unit_exchange as unit_exchange " +
                            "from p365_products_onhandbybranchs a " +
                            "left outer join p365_branchs_ims_warehouse b on a.branch_id = b.p365_branchs_id " +
                            "left outer join p365_products_ims_chemical c on a.product_id = c.p365_products_id " +
                            "left outer join ims_chemical d on c.ims_chemical_id = d.id " +
                            "left outer join ( " +
                            "    select * from ims_chemical_sub_unit a where a.type = 3) e on d.id = e.chemical_id ",
                    (rs, rowNum) -> {
                    final ProductOnHandChemicalWarehouseQueryDto result = new ProductOnHandChemicalWarehouseQueryDto();
                    result.setBranchId(rs.getLong("branch_id"));
                    result.setProductId(rs.getLong("product_id"));
                    result.setOnHand(rs.getLong("on_hand"));
                    result.setCost(rs.getBigDecimal("cost"));
                    result.setPriceByBranch(rs.getBigDecimal("price_by_branch"));
                    result.setCreatedDate(rs.getString("created_date"));
                    result.setCreatedBy(rs.getLong("created_by"));
                    result.setModifiedBy(rs.getLong("modified_by"));
                    result.setModifiedDate(rs.getString("modified_date"));
                    result.setPriceByBranchLargeUnit(rs.getBigDecimal("price_by_branch_large_unit"));
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
            items.forEach(item -> {
                KeyHolder keyHolder = new GeneratedKeyHolder();
                insertedTotal += jdbcTemplate.update(
                        connection -> {
                            PreparedStatement ps = connection.prepareStatement(
                                    "INSERT INTO ims_warehouse_chemical (gmt_create, gmt_modified, version, warehouse_id, chemical_id, chemical_code, chemical_item_sku_id, " +
                                    "    unit_id, unit_type, unit_name, unit_price, standard_unit_exchange, package_weight, package_unit, " +
                                    "    quantity, delta, total_price, ref_goods_receipt_id, ref_goods_receipt_type, import_export) " +
                                    "VALUES (CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP(),0,?,?,?,0, " +
                                    "    ?,?,?,?,?,null,null, " +
                                    "    ?,0.0,?,null,null,0) " +
                                    "ON DUPLICATE KEY UPDATE gmt_modified = CURRENT_TIMESTAMP(), version = version+1, chemical_code = ?, chemical_item_sku_id = 0, " +
                                    "    unit_id = ?, unit_type = ?, unit_name = ?, unit_price = ?, standard_unit_exchange = ?, package_weight = null, package_unit = null, " +
                                    "    quantity = ?, delta = 0.0, total_price = ?, ref_goods_receipt_id = null, ref_goods_receipt_type = null, import_export = 0 ",
                                    new String[] {"id"});
                            ps.setLong(1, item.getImsWarehouseId());
                            ps.setLong(2, item.getImsChemicalId());
                            ps.setString(3, Utils.nvl(item.getChemicalCode()));
                            ps.setLong(4, Utils.nvl(item.getUnitId()));
                            ps.setLong(5, Utils.nvl(item.getUnitType()));
                            ps.setString(6, Utils.nvl(item.getUnitName()));
                            ps.setBigDecimal(7, Utils.nvl(item.getUnitPrice()));
                            ps.setBigDecimal(8, Utils.nvl(item.getUnitExchange()));
                            ps.setLong(9, Utils.nvl(item.getOnHand()));
                            ps.setBigDecimal(10, Utils.nvl(item.getUnitPrice()).multiply(new BigDecimal(Utils.nvl(item.getOnHand()))));
                            ps.setString(11, Utils.nvl(item.getChemicalCode()));
                            ps.setLong(12, Utils.nvl(item.getUnitId()));
                            ps.setLong(13, Utils.nvl(item.getUnitType()));
                            ps.setString(14, Utils.nvl(item.getUnitName()));
                            ps.setBigDecimal(15, Utils.nvl(item.getUnitPrice()));
                            ps.setBigDecimal(16, Utils.nvl(item.getUnitExchange()));
                            ps.setLong(17, Utils.nvl(item.getOnHand()));
                            ps.setBigDecimal(18, Utils.nvl(item.getUnitPrice()).multiply(new BigDecimal(Utils.nvl(item.getOnHand()))));
                            return ps;
                        },
                        keyHolder);
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
    private static class ProductOnHandChemicalWarehouseQueryDto extends Pos365ProductOnHandByBranch {
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
