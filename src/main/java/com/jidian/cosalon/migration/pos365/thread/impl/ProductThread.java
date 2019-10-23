package com.jidian.cosalon.migration.pos365.thread.impl;

import com.jidian.cosalon.migration.pos365.domainpos365.Pos365Partner;
import com.jidian.cosalon.migration.pos365.domainpos365.Pos365Product;
import com.jidian.cosalon.migration.pos365.dto.BaseResponse;
import com.jidian.cosalon.migration.pos365.thread.MyThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.stereotype.Component;
import retrofit2.Response;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Component("productThread")
public class ProductThread extends MyThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductThread.class);

    @Override
    public String getName() {
        return "ProductThread";
    }

    @Override
    public void doRun() {
        int skip = 0;
        int top = 100;
        int count = 0;
        int insertedTotal = 0;
        int assumptionTotal = 0;

        try {
            jdbcTemplate.execute("TRUNCATE TABLE p365_products");
            do {
                BaseResponse<Pos365Product> response = pos365RetrofitService.listProducts(getMapHeaders2(), top, skip).execute().body();
                if (response != null) {
                    skip += top;
                    assumptionTotal = response.getCount();
                }
                count = 0;
                if (response != null && response.getResults() != null) {
                    count = response.getResults().size();

                    List<Pos365Product> products = response.getResults();
                    jdbcTemplate.batchUpdate("INSERT INTO p365_products " +
                            "    (id, on_hand, total_on_hand, position, attributes_name, block_of_time_to_use_service, bonus_point, bonus_point_for_assistant, " +
                            "    bonus_point_for_assistant2, bonus_point_for_assistant3, category_id, code, compare_max_quantity, compare_min_quantity, conversion_value, " +
                            "    cost, created_by, created_date, is_percentage_of_total_order, is_price_for_block, is_serial_number_tracking, large_unit, large_unit_code, " +
                            "    max_quantity, min_quantity, modified_date, name, on_order, order_quick_notes, price, price_by_branch, price_by_branch_large_unit, " +
                            "    price_large_unit, printer, product_type, recent_purchase_price, recent_purchase_price_large_unit, retailer_id, split_for_sales_order, unit) " +
                            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            Pos365Product categories = products.get(i);
                            ps.setLong(1, categories.getId());
                            ps.setLong(2, categories.getOnHand() == null ? 0 : categories.getOnHand());
                            ps.setLong(3, categories.getTotalOnHand() == null ? 0 : categories.getTotalOnHand());
                            ps.setLong(4, categories.getPosition() == null ? 0 : categories.getPosition());
                            ps.setString(5, categories.getAttributesName());
                            ps.setLong(6, categories.getBlockOfTimeToUseService() == null ? 0 : categories.getBlockOfTimeToUseService());
                            ps.setLong(7, categories.getBonusPoint() == null ? 0 : categories.getBonusPoint());
                            ps.setLong(8, categories.getBonusPointForAssistant() == null ? 0 : categories.getBonusPointForAssistant());
                            ps.setLong(9, categories.getBonusPointForAssistant2() == null ? 0 : categories.getBonusPointForAssistant2());
                            ps.setLong(10, categories.getBonusPointForAssistant3() == null ? 0 : categories.getBonusPointForAssistant3());
                            ps.setLong(11, categories.getCategoryId() == null ? 0 : categories.getCategoryId());
                            ps.setString(12, categories.getCode());
                            ps.setLong(13, categories.getCompareMaxQuantity() == null ? 0 : categories.getCompareMaxQuantity());
                            ps.setLong(14, categories.getCompareMinQuantity() == null ? 0 : categories.getCompareMinQuantity());
                            ps.setLong(15, categories.getConversionValue() == null ? 0 : categories.getConversionValue());
                            ps.setBigDecimal(16, categories.getCost() == null ? BigDecimal.ZERO : categories.getCost());
                            ps.setLong(17, categories.getCreatedBy() == null ? 0 : categories.getCreatedBy());
                            ps.setString(18, categories.getCreatedDate());
                            ps.setBoolean(19, categories.getIsPercentageOfTotalOrder());
                            ps.setBoolean(20, categories.getIsPriceForBlock());
                            ps.setBoolean(21, categories.getIsSerialNumberTracking());
                            ps.setString(22, categories.getLargeUnit());
                            ps.setString(23, categories.getLargeUnitCode());
                            ps.setLong(24, categories.getMaxQuantity() == null ? 0 : categories.getMaxQuantity());
                            ps.setLong(25, categories.getMinQuantity() == null ? 0 : categories.getMinQuantity());
                            ps.setString(26, categories.getModifiedDate());
                            ps.setString(27, categories.getName());
                            ps.setLong(28, categories.getOnOrder() == null ? 0 : categories.getOnOrder());
                            ps.setString(29, categories.getOrderQuickNotes());
                            ps.setBigDecimal(30, categories.getPrice() == null ? BigDecimal.ZERO : categories.getPrice());
                            ps.setBigDecimal(31, categories.getPriceByBranch() == null ? BigDecimal.ZERO : categories.getPriceByBranch());
                            ps.setBigDecimal(32, categories.getPriceByBranchLargeUnit() == null ? BigDecimal.ZERO : categories.getPriceByBranchLargeUnit());
                            ps.setBigDecimal(33, categories.getPriceLargeUnit() == null ? BigDecimal.ZERO : categories.getPriceLargeUnit());
                            ps.setString(34, categories.getPrinter());
                            ps.setLong(35, categories.getProductType() == null ? 0 : categories.getProductType());
                            ps.setLong(36, categories.getRecentPurchasePrice() == null ? 0 : categories.getRecentPurchasePrice());
                            ps.setLong(37, categories.getRecentPurchasePriceLargeUnit() == null ? 0 : categories.getRecentPurchasePriceLargeUnit());
                            ps.setLong(38, categories.getRetailerId() == null ? 0 : categories.getRetailerId());
                            ps.setBoolean(39, categories.getSplitForSalesOrder());
                            ps.setString(40, categories.getUnit());
                        }

                        @Override
                        public int getBatchSize() {
                            return products.size();
                        }
                    });


//                    response.getResults().forEach(item -> {
//                        jdbcTemplate.update(
//                                "INSERT INTO p365_products " +
//                                        "    (id, on_hand, total_on_hand, position, attributes_name, block_of_time_to_use_service, bonus_point, bonus_point_for_assistant, " +
//                                        "    bonus_point_for_assistant2, bonus_point_for_assistant3, category_id, code, compare_max_quantity, compare_min_quantity, conversion_value, " +
//                                        "    cost, created_by, created_date, is_percentage_of_total_order, is_price_for_block, is_serial_number_tracking, large_unit, large_unit_code, " +
//                                        "    max_quantity, min_quantity, modified_date, name, on_order, order_quick_notes, price, price_by_branch, price_by_branch_large_unit, " +
//                                        "    price_large_unit, printer, product_type, recent_purchase_price, recent_purchase_price_large_unit, retailer_id, split_for_sales_order, unit) " +
//                                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
//                                item.getId(), item.getOnHand(), item.getTotalOnHand(), item.getPosition(), item.getAttributesName(), item.getBlockOfTimeToUseService(), item.getBonusPoint(), item.getBonusPointForAssistant(),
//                                item.getBonusPointForAssistant2(), item.getBonusPointForAssistant3(), item.getCategoryId(), item.getCode(), item.getCompareMaxQuantity(), item.getCompareMinQuantity(), item.getConversionValue(),
//                                item.getCost(), item.getCreatedBy(), item.getCreatedDate(), item.getIsPercentageOfTotalOrder(), item.getIsPriceForBlock(), item.getIsSerialNumberTracking(), item.getLargeUnit(), item.getLargeUnitCode(),
//                                item.getMaxQuantity(), item.getMinQuantity(), item.getModifiedDate(), item.getName(), item.getOnOrder(), item.getOrderQuickNotes(), item.getPrice(), item.getPriceByBranch(), item.getPriceByBranchLargeUnit(),
//                                item.getPriceLargeUnit(), item.getPrinter(), item.getProductType(), item.getRecentPurchasePrice(), item.getRecentPurchasePriceLargeUnit(), item.getRetailerId(), item.getSplitForSalesOrder(), item.getUnit());
//                    });
//                    jdbcTemplate.execute("COMMIT");
                    insertedTotal += response.getResults().size();
                }
            } while (count > 0);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            LOGGER.info("SUMMARY: insertedTotal: {}, assumptionTotal: {}", insertedTotal, assumptionTotal);
        }
    }
}
