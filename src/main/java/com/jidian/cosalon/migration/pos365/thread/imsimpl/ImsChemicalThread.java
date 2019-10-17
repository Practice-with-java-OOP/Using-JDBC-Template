package com.jidian.cosalon.migration.pos365.thread.imsimpl;

import com.jidian.cosalon.migration.pos365.domainpos365.Pos365Branch;
import com.jidian.cosalon.migration.pos365.domainpos365.Pos365Product;
import com.jidian.cosalon.migration.pos365.thread.MyThread;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.util.List;

@Component("imsChemicalThread")
public class ImsChemicalThread extends MyThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImsChemicalThread.class);

    @Override
    public String getName() {
        return "ImsChemicalThread";
    }

    private int insertedTotal = 0;
    private int assumptionTotal = 0;

    @Override
    public void doRun() {
        insertedTotal = 0;
        assumptionTotal = 0;
        try {
            // select p365_branchs voi mapping voi bang ims_warehouse
            final List<ProductChemicalQueryDto> items = jdbcTemplate.query(
                    "select * from p365_products a " +
                            "left outer join p365_products_ims_chemical b on a.id = b.p365_products_id " +
                            "where a.product_type = 1", (rs, rowNum) -> {
                    final ProductChemicalQueryDto result = new ProductChemicalQueryDto();
                    result.setId(rs.getLong("id"));
                    result.setCode(rs.getString("id"));
                    result.setName(rs.getString("name"));
                    result.setConversionValue(rs.getLong("conversion_value"));
                    result.setCategoryId(rs.getLong("category_id"));
                    result.setCost(rs.getBigDecimal("cost"));
                    result.setCreatedDate(rs.getString("created_date"));
                    result.setCreatedBy(rs.getLong("created_by"));
                    result.setModifiedDate(rs.getString("modified_date"));
                    result.setLargeUnit(rs.getString("large_unit"));
                    result.setLargeUnitCode(rs.getString("large_unit_code"));
                    result.setPrice(rs.getBigDecimal("price"));
                    result.setPriceByBranch(rs.getBigDecimal("price_by_branch"));
                    result.setPriceByBranchLargeUnit(rs.getBigDecimal("price_by_branch_large_unit"));
                    result.setPriceLargeUnit(rs.getBigDecimal("price_large_unit"));
                    result.setProductType(rs.getLong("product_type"));
                    result.setRetailerId(rs.getLong("retailer_id"));
                    result.setUnit(rs.getString("unit"));
                    result.setP365ProductId(rs.getLong("p365_products_id"));
                    result.setImsChemicalId(rs.getLong("ims_chemical_id"));
                    return result;
                }
            );
            assumptionTotal = items.size();
            items.forEach(dto -> {
                Long chemicalId = null;
                if (dto.getImsChemicalId() == null || dto.getImsChemicalId() == 0L) {
                    KeyHolder keyHolder = new GeneratedKeyHolder();
                    insertedTotal += jdbcTemplate.update(
                            connection -> {
                                PreparedStatement ps = connection.prepareStatement("INSERT INTO ims_chemical (gmt_create, gmt_modified, version, name, code, item_sku_id, quantity, standard_unit, price_standard_unit) " +
                                                "VALUES (CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP(),0,?,?,0,0,?,?)",
                                        new String[] {"id"});
                                ps.setString(1, dto.getName());
                                ps.setString(2, dto.getCode());
                                ps.setString(3, dto.getLargeUnit() == null ? "" : dto.getLargeUnit());
                                ps.setBigDecimal(4, dto.getPriceLargeUnit());
                                return ps;
                            },
                            keyHolder);
                    if (keyHolder.getKey() != null) {
                        chemicalId = keyHolder.getKey().longValue();
                        try {
                            jdbcTemplate.update("INSERT INTO p365_products_ims_chemical (p365_products_id, ims_chemical_id) " +
                                    "VALUES (?,?) ON DUPLICATE KEY UPDATE ims_chemical_id = ?",
                                    dto.getId(),
                                    keyHolder.getKey().longValue(),
                                    keyHolder.getKey().longValue());
                        } catch (DataAccessException e) {
                        }
                    }
                } else {
                    chemicalId = dto.getImsChemicalId();
                    insertedTotal += jdbcTemplate.update("UPDATE ims_chemical SET gmt_modified = CURRENT_TIMESTAMP(), version = version + 1, " +
                                    "   name = ?, code = ?, standard_unit = ?, price_standard_unit = ? WHERE id = ?",
                            dto.getName(),
                            dto.getCode(),
                            dto.getLargeUnit() == null ? "" : dto.getLargeUnit(),
                            dto.getPriceLargeUnit(),
                            dto.getImsChemicalId());
                }

                if (chemicalId != null ) {
                    if (dto.getUnit() != null && !dto.getUnit().isEmpty()) {
                        final List<ChemicalSubUnitQueryDto> subUnits = jdbcTemplate.query(
                                "select * from ims_chemical_sub_unit a where a.type = 3 and a.chemical_id = ? order by a.id asc",  (rs, rowNum) -> {
                                    final ChemicalSubUnitQueryDto result = new ChemicalSubUnitQueryDto();
                                    result.setId(rs.getLong("id"));
                                    result.setVersion(rs.getLong("version"));
                                    result.setType(rs.getInt("type"));
                                    result.setName(rs.getString("name"));
                                    result.setPrice(rs.getBigDecimal("price"));
                                    result.setChemicalId(rs.getLong("chemical_id"));
                                    result.setStandardUnitExchange(rs.getString("standard_unit_exchange"));
                                    return result;
                                },
                                chemicalId
                        );
                        if (subUnits.size() <= 0) {
                            try {
                                jdbcTemplate.update("INSERT INTO ims_chemical_sub_unit (gmt_create, gmt_modified, version, type, name, price, chemical_id, standard_unit_exchange) " +
                                                "VALUES (CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP(),0,3,?,?,?,?)",
                                        dto.getUnit(),
                                        null,
                                        chemicalId,
                                        new BigDecimal(dto.getConversionValue()));
                            } catch (DataAccessException e) {
                            }
                        } else {
                            for (int i = 0; i < subUnits.size(); i++) {
                                jdbcTemplate.update("UPDATE ims_chemical_sub_unit SET  gmt_modified = CURRENT_TIMESTAMP(), version = version+1, " +
                                                "name = ?, price = ?, chemical_id = ?, standard_unit_exchange = ? WHERE id = ?",
                                        dto.getUnit(),
                                        null,
                                        chemicalId,
                                        new BigDecimal(dto.getConversionValue()),
                                        subUnits.get(i).getId());
                            }
                        }
                    }
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
    private static class ProductChemicalQueryDto extends Pos365Product {
        private Long p365ProductId;
        private Long imsChemicalId;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class ChemicalSubUnitQueryDto {
        private Long id;
        private Long version;
        private Integer type;
        private String name;
        private BigDecimal price;
        private Long chemicalId;
        private String standardUnitExchange;
    }
}
