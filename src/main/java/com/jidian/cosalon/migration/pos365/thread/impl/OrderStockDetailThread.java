package com.jidian.cosalon.migration.pos365.thread.impl;

import com.jidian.cosalon.migration.pos365.domainpos365.Pos365OrderDetail;
import com.jidian.cosalon.migration.pos365.domainpos365.Pos365OrderStockDetail;
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
import java.util.List;

@Component("orderStockDetailThread")
public class OrderStockDetailThread extends MyThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderStockDetailThread.class);

    @Override
    public String getName() {
        return "orderStockDetailThread";
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
            jdbcTemplate.execute("TRUNCATE TABLE p365_order_stock_detail");

            final List<Long> orderStockIds = jdbcTemplate.queryForList("select distinct id from p365_order_stock", Long.class);
            if (orderStockIds != null && !orderStockIds.isEmpty()) {
                orderStockIds.forEach(orderStockId -> {
                    try {
                        skip = 0;
                        count = 0;
                        do {
                            BaseResponse<Pos365OrderStockDetail> response = pos365RetrofitService.listOrderStockDetail(getMapHeaders2(), top, skip, orderStockId).execute().body();
                            if (response != null) {
                                skip += top;
                                assumptionTotal = response.getCount();
                            }
                            count = 0;
                            if (response != null && response.getResults() != null) {
                                count = response.getResults().size();

                                List<Pos365OrderStockDetail> orderStockDetails =  response.getResults();
                                jdbcTemplate.batchUpdate("INSERT INTO p365_order_stock_detail " +
                                        "    (id, purchase_order_id, product_id, quantity, price, description, is_large_unit, selling_price, conversion_value, order_quantity) " +
                                        "    VALUES (?,?,?,?,?,?,?,?,?,?)", new BatchPreparedStatementSetter() {
                                    @Override
                                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                                        Pos365OrderStockDetail categories = orderStockDetails.get(i);
                                        ps.setLong(1, categories.getId());
                                        ps.setLong(2, categories.getPurchaseOrderId() == null ? 0 : categories.getPurchaseOrderId());
                                        ps.setLong(3, categories.getProductId() == null ? 0 : categories.getProductId());
                                        ps.setInt(4, categories.getQuantity());
                                        ps.setBigDecimal(5, categories.getPrice() == null ? BigDecimal.ZERO : categories.getPrice());
                                        ps.setString(6, categories.getDescription());
                                        ps.setBoolean(7, categories.getIsLargeUnit());
                                        ps.setBigDecimal(8, categories.getSellingPrice() == null ? BigDecimal.ZERO : categories.getSellingPrice());
                                        ps.setInt(9, categories.getConversionValue());
                                        ps.setInt(10, categories.getOrderQuantity());
                                    }

                                    @Override
                                    public int getBatchSize() {
                                        return orderStockDetails.size();
                                    }
                                });

//                                response.getResults().forEach(item -> {
//                                    jdbcTemplate.update(
//                                            "INSERT INTO p365_order_stock_detail " +
//                                                    "    (id, purchase_order_id, product_id, quantity, price, description, is_large_unit, selling_price, conversion_value, order_quantity) " +
//                                                    "    VALUES (?,?,?,?,?,?,?,?,?,?)",
//                                            item.getId(), item.getPurchaseOrderId(), item.getProductId(), item.getQuantity(), item.getPrice(), item.getDescription(),
//                                            item.getIsLargeUnit(), item.getSellingPrice(), item.getConversionValue(), item.getOrderQuantity());
//                                });
//                                jdbcTemplate.execute("COMMIT");
//                                insertedTotal += response.getResults().size();
                            }
                        } while (count > 0);
                    } catch (IOException e) {
                        LOGGER.error(e.getMessage(), e);
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
