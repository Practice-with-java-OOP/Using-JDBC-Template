package com.jidian.cosalon.migration.pos365.thread.impl;

import com.jidian.cosalon.migration.pos365.domainpos365.Pos365Product;
import com.jidian.cosalon.migration.pos365.dto.BaseResponse;
import com.jidian.cosalon.migration.pos365.thread.MyThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import retrofit2.Response;

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
                    response.getResults().forEach(item -> {
                        jdbcTemplate.update(
                                "INSERT INTO p365_products " +
                                        "    (id, on_hand, total_on_hand, position, attributes_name, block_of_time_to_use_service, bonus_point, bonus_point_for_assistant, " +
                                        "    bonus_point_for_assistant2, bonus_point_for_assistant3, category_id, code, compare_max_quantity, compare_min_quantity, conversion_value, " +
                                        "    cost, created_by, created_date, is_percentage_of_total_order, is_price_for_block, is_serial_number_tracking, large_unit, large_unit_code, " +
                                        "    max_quantity, min_quantity, modified_date, name, on_order, order_quick_notes, price, price_by_branch, price_by_branch_large_unit, " +
                                        "    price_large_unit, printer, product_type, recent_purchase_price, recent_purchase_price_large_unit, retailer_id, split_for_sales_order, unit) " +
                                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                                item.getId(), item.getOnHand(), item.getTotalOnHand(), item.getPosition(), item.getAttributesName(), item.getBlockOfTimeToUseService(), item.getBonusPoint(), item.getBonusPointForAssistant(),
                                item.getBonusPointForAssistant2(), item.getBonusPointForAssistant3(), item.getCategoryId(), item.getCode(), item.getCompareMaxQuantity(), item.getCompareMinQuantity(), item.getConversionValue(),
                                item.getCost(), item.getCreatedBy(), item.getCreatedDate(), item.getIsPercentageOfTotalOrder(), item.getIsPriceForBlock(), item.getIsSerialNumberTracking(), item.getLargeUnit(), item.getLargeUnitCode(),
                                item.getMaxQuantity(), item.getMinQuantity(), item.getModifiedDate(), item.getName(), item.getOnOrder(), item.getOrderQuickNotes(), item.getPrice(), item.getPriceByBranch(), item.getPriceByBranchLargeUnit(),
                                item.getPriceLargeUnit(), item.getPrinter(), item.getProductType(), item.getRecentPurchasePrice(), item.getRecentPurchasePriceLargeUnit(), item.getRetailerId(), item.getSplitForSalesOrder(), item.getUnit());
                    });
                    jdbcTemplate.execute("COMMIT");
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
