package com.jidian.cosalon.migration.pos365.thread.impl;

import com.jidian.cosalon.migration.pos365.domainpos365.Pos365ProductHistory;
import com.jidian.cosalon.migration.pos365.dto.BaseResponse;
import com.jidian.cosalon.migration.pos365.thread.MyThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component("productHistoryThread")
public class ProductHistoryThread extends MyThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductHistoryThread.class);

    @Override
    public String getName() {
        return "ProductHistoryThread";
    }

    private int skip = 0;
    private int top = 100;
    private int count = 0;
    private int insertedTotal = 0;
    private int assumptionTotal = 0;

    /**
     * chay thu:
     * SUMMARY: insertedTotal: 35632, assumptionTotal: 0
     * ProductHistoryThread run time elapsed: 1789s ( ~30 phut )
     */
    @Override
    public void doRun() {

        try {
            jdbcTemplate.execute("TRUNCATE TABLE p365_products_history");

            final List<Long> productIds = jdbcTemplate.queryForList("select distinct id from p365_products", Long.class);
            final List<Long> branchIds = jdbcTemplate.queryForList("select distinct id from p365_branchs", Long.class);
            if (productIds != null) {
                productIds.forEach(productId -> {
                    branchIds.forEach(branchId -> {
                        try {
                            skip = 0;
                            count = 0;
                            do {
                                BaseResponse<Pos365ProductHistory> response = pos365RetrofitService.listProductsHistory(getMapHeaders2(), top, skip, productId, branchId).execute().body();
                                if (response != null) {
                                    skip += top;
                                    assumptionTotal = response.getCount();
                                }
                                count = 0;
                                if (response != null && response.getResults() != null) {
                                    count = response.getResults().size();
                                    response.getResults().forEach(item -> {
                                        jdbcTemplate.update(
                                                "INSERT INTO p365_products_history " +
                                                "    (id, branch_id, cost, document_code, document_id, document_type, ending_stocks, price, " +
                                                "    product_id, quantity, retailer_id, trans_date) " +
                                                "    VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
                                                item.getId(), item.getBranchId(), item.getCost(), item.getDocumentCode(), item.getDocumentId(), item.getDocumentType(), item.getEndingStocks(), item.getPrice(),
                                                item.getProductId(), item.getQuantity(), item.getRetailerId(), item.getTransDate());
                                    });
                                    jdbcTemplate.execute("COMMIT");
                                    insertedTotal += response.getResults().size();
                                }
                            } while (count > 0);
                        } catch (IOException e) {
                            LOGGER.error(e.getMessage(), e);
                        }
                    });

                });
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            LOGGER.info("SUMMARY: insertedTotal: {}, assumptionTotal: {}", insertedTotal, assumptionTotal);
        }
    }
}
