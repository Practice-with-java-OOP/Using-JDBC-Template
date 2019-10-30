package com.jidian.cosalon.migration.pos365.thread.impl;

import com.jidian.cosalon.migration.pos365.domainpos365.Pos365Partner;
import com.jidian.cosalon.migration.pos365.domainpos365.Pos365ProductHistory;
import com.jidian.cosalon.migration.pos365.dto.BaseResponse;
import com.jidian.cosalon.migration.pos365.thread.MyThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
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
                List<Pos365ProductHistory> productHistories = new ArrayList<>();
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
                                    productHistories.addAll(response.getResults());
//                                    response.getResults().forEach(item -> {
//                                        jdbcTemplate.update(
//                                                "INSERT INTO p365_products_history " +
//                                                "    (id, branch_id, cost, document_code, document_id, document_type, ending_stocks, price, " +
//                                                "    product_id, quantity, retailer_id, trans_date) " +
//                                                "    VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
//                                                item.getId(), item.getBranchId(), item.getCost(), item.getDocumentCode(), item.getDocumentId(), item.getDocumentType(), item.getEndingStocks(), item.getPrice(),
//                                                item.getProductId(), item.getQuantity(), item.getRetailerId(), item.getTransDate());
//                                    });
//                                    jdbcTemplate.execute("COMMIT");
                                    insertedTotal += response.getResults().size();
                                }
                                LOGGER.info("fetching insertedTotal p365_products_history size :" + insertedTotal);
                            } while (count > 0);
                        } catch (IOException e) {
                            LOGGER.error(e.getMessage(), e);
                        }
                    });

                });
                jdbcTemplate.batchUpdate("INSERT INTO p365_products_history " +
                        "    (id, branch_id, cost, document_code, document_id, document_type, ending_stocks, price, " +
                        "    product_id, quantity, retailer_id, trans_date) " +
                        "    VALUES (?,?,?,?,?,?,?,?,?,?,?,?)", new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Pos365ProductHistory categories = productHistories.get(i);
                        ps.setLong(1, categories.getId());
                        ps.setLong(2, categories.getBranchId() == null ? 0 : categories.getBranchId());
                        ps.setBigDecimal(3, categories.getCost() == null ? BigDecimal.ZERO : categories.getCost());
                        ps.setString(4, categories.getDocumentCode());
                        ps.setLong(5, categories.getDocumentId() == null ? 0 : categories.getDocumentId());
                        ps.setLong(6, categories.getDocumentType() == null ? 0 : categories.getDocumentType());
                        ps.setLong(7, categories.getEndingStocks() == null ? 0 : categories.getEndingStocks());
                        ps.setBigDecimal(8, categories.getPrice() == null ? BigDecimal.ZERO : categories.getPrice());
                        ps.setLong(9, categories.getProductId() == null ? 0 : categories.getProductId());
                        ps.setLong(10, categories.getQuantity() == null ? 0 : categories.getQuantity());
                        ps.setLong(11, categories.getRetailerId() == null ? 0 : categories.getRetailerId());
                        ps.setString(12, categories.getTransDate());
                    }

                    @Override
                    public int getBatchSize() {
                        return productHistories.size();
                    }
                });
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            LOGGER.info("SUMMARY: insertedTotal: {}, assumptionTotal: {}", insertedTotal, assumptionTotal);
        }
    }
}
