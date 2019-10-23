package com.jidian.cosalon.migration.pos365.thread.impl;

import com.jidian.cosalon.migration.pos365.domainpos365.Pos365Items;
import com.jidian.cosalon.migration.pos365.domainpos365.Pos365OrderDetail;
import com.jidian.cosalon.migration.pos365.domainpos365.Pos365ReturnDetail;
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

@Component("orderDetailThread")
public class OrderDetailThread extends MyThread {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderDetailThread.class);

    @Override
    public String getName() {
        return "orderDetailThread";
    }

    private int skip = 0;
    private int top = 100;
    private int count = 0;
    private int insertedTotal = 0;
    private int assumptionTotal = 0;

    @Override
    public void doRun() {
        try {
            jdbcTemplate.execute("TRUNCATE TABLE p365_order_detail");

            final List<Long> orderIds = jdbcTemplate.queryForList("select distinct id from p365_orders", Long.class);
            if (orderIds != null && !orderIds.isEmpty()) {
                List<Pos365OrderDetail> orderDetails = new ArrayList<>();
                orderIds.forEach(orderId -> {
                    try {
                        skip = 0;
                        count = 0;
                        do {
                            BaseResponse<Pos365OrderDetail> response = pos365RetrofitService.listOrderDetail(getMapHeaders2(), top, skip, orderId).execute().body();
                            if (response != null) {
                                skip += top;
                                assumptionTotal = response.getCount();
                            }
                            count = 0;
                            if (response != null && response.getResults() != null) {
                                count = response.getResults().size();

                                orderDetails.addAll(response.getResults());
//                                jdbcTemplate.batchUpdate("INSERT INTO p365_order_detail " +
//                                        "    (id, order_id, product_id, quantity, price, base_price, is_large_unit, conversion_value, coefficient, processed, sold_by_id, assistant_by_id) " +
//                                        "    VALUES (?,?,?,?,?,?,?,?,?,?,?,?)", new BatchPreparedStatementSetter() {
//                                    @Override
//                                    public void setValues(PreparedStatement ps, int i) throws SQLException {
//                                        Pos365OrderDetail categories = orderDetails.get(i);
//                                        ps.setLong(1, categories.getId());
//                                        ps.setLong(2, categories.getOrderId() == null ? 0 : categories.getOrderId());
//                                        ps.setLong(3, categories.getProductId() == null ? 0 : categories.getProductId());
//                                        ps.setInt(4, categories.getQuantity());
//                                        ps.setBigDecimal(5, categories.getPrice() == null ? BigDecimal.ZERO : categories.getPrice());
//                                        ps.setBigDecimal(6, categories.getBasePrice() == null ? BigDecimal.ZERO : categories.getBasePrice());
//                                        ps.setBoolean(7, categories.getIsLargeUnit());
//                                        ps.setInt(8, categories.getConversionValue());
//                                        ps.setInt(9, categories.getCoefficient());
//                                        ps.setInt(10, categories.getProcessed());
//                                        ps.setLong(11, categories.getSoldById() == null ? 0 : categories.getSoldById());
//                                        ps.setLong(12, categories.getAssistantById() == null ? 0 : categories.getAssistantById());
//                                    }
//
//                                    @Override
//                                    public int getBatchSize() {
//                                        return orderDetails.size();
//                                    }
//                                });


//                                response.getResults().forEach(item -> {
//                                    jdbcTemplate.update(
//                                            "INSERT INTO p365_order_detail " +
//                                                    "    (id, order_id, product_id, quantity, price, base_price, is_large_unit, conversion_value, coefficient, processed, sold_by_id, assistant_by_id) " +
//                                                    "    VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
//                                            item.getId(), item.getOrderId(), item.getProductId(), item.getQuantity(), item.getPrice(),item.getBasePrice(),
//                                            item.getIsLargeUnit(), item.getConversionValue(), item.getCoefficient(), item.getProcessed(), item.getSoldById(), item.getAssistantById());
//                                });
//                                jdbcTemplate.execute("COMMIT");
//                                insertedTotal += response.getResults().size();
                            }
                        } while (count > 0);
                    } catch (IOException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                });
                jdbcTemplate.batchUpdate("INSERT INTO p365_order_detail " +
                        "    (id, order_id, product_id, quantity, price, base_price, is_large_unit, conversion_value, coefficient, processed, sold_by_id, assistant_by_id) " +
                        "    VALUES (?,?,?,?,?,?,?,?,?,?,?,?)", new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Pos365OrderDetail categories = orderDetails.get(i);
                        ps.setLong(1, categories.getId());
                        ps.setLong(2, categories.getOrderId() == null ? 0 : categories.getOrderId());
                        ps.setLong(3, categories.getProductId() == null ? 0 : categories.getProductId());
                        ps.setInt(4, categories.getQuantity());
                        ps.setBigDecimal(5, categories.getPrice() == null ? BigDecimal.ZERO : categories.getPrice());
                        ps.setBigDecimal(6, categories.getBasePrice() == null ? BigDecimal.ZERO : categories.getBasePrice());
                        ps.setBoolean(7, categories.getIsLargeUnit());
                        ps.setInt(8, categories.getConversionValue());
                        ps.setInt(9, categories.getCoefficient());
                        ps.setInt(10, categories.getProcessed());
                        ps.setLong(11, categories.getSoldById() == null ? 0 : categories.getSoldById());
                        ps.setLong(12, categories.getAssistantById() == null ? 0 : categories.getAssistantById());
                    }

                    @Override
                    public int getBatchSize() {
                        return orderDetails.size();
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
