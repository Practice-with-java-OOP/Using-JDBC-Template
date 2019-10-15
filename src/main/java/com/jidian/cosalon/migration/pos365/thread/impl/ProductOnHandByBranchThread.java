package com.jidian.cosalon.migration.pos365.thread.impl;

import com.jidian.cosalon.migration.pos365.domainpos365.Pos365Product;
import com.jidian.cosalon.migration.pos365.domainpos365.Pos365ProductOnHandByBranch;
import com.jidian.cosalon.migration.pos365.dto.BaseResponse;
import com.jidian.cosalon.migration.pos365.thread.MyThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("productOnHandByBranchThread")
public class ProductOnHandByBranchThread extends MyThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductOnHandByBranchThread.class);

    @Override
    public String getName() {
        return "ProductOnHandByBranchThread";
    }

    @Override
    public void doRun() {
        int skip = 0;
        int top = 100;
        int count = 0;
        int insertedTotal = 0;
        int assumptionTotal = 0;

        try {
            jdbcTemplate.execute("TRUNCATE TABLE p365_products_onhandbybranchs");
            do {
                BaseResponse<Pos365ProductOnHandByBranch> response = pos365RetrofitService.listProductOnHandByBranch(getMapHeaders2(), top, skip).execute().body();
                if (response != null) {
                    skip += top;
                    assumptionTotal = response.getCount();
                }
                count = 0;
                if (response != null && response.getResults() != null) {
                    count = response.getResults().size();
                    response.getResults().forEach(item -> {
                        jdbcTemplate.update(
                                "INSERT INTO p365_products_onhandbybranchs " +
                                        "    (branch_id, product_id, cost, created_by, created_date, max_quantity, min_quantity, modified_by, modified_date, " +
                                        "    on_hand, on_order, price_by_branch, price_by_branch_large_unit, retailer_id) " +
                                        "    VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                                item.getBranchId(), item.getProductId(), item.getCost(), item.getCreatedBy(), item.getCreatedDate(), item.getMaxQuantity(), item.getMinQuantity(), item.getModifiedBy(), item.getModifiedDate(),
                                item.getOnHand(), item.getOnOrder(), item.getPriceByBranch(), item.getPriceByBranchLargeUnit(), item.getRetailerId());
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
