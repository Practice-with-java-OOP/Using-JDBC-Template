package com.jidian.cosalon.migration.pos365.thread.impl;

import com.jidian.cosalon.migration.pos365.domainpos365.Pos365OrderDetail;
import com.jidian.cosalon.migration.pos365.domainpos365.Pos365OrderStock;
import com.jidian.cosalon.migration.pos365.dto.BaseResponse;
import com.jidian.cosalon.migration.pos365.thread.MyThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Component("orderStockThread")
public class OrderStockThread extends MyThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderStockThread.class);

    @Override
    public String getName() {
        return "orderStockThread";
    }

    @Override
    public void doRun() {
        int skip = 0;
        int top = 100;
        int count = 0;
        int insertedTotal = 0;
        int assumptionTotal = 0;

        try {
            jdbcTemplate.execute("TRUNCATE TABLE p365_order_stock");
            do {
                BaseResponse<Pos365OrderStock> response = pos365RetrofitService
                        .listOrderStock(getMapHeaders2(), top, skip).execute().body();
                if (response != null) {
                    skip += top;
                    assumptionTotal = response.getCount();
                }
                count = 0;
                if (response != null && response.getResults() != null) {
                    count = response.getResults().size();

                    List<Pos365OrderStock> orderStocks = response.getResults();
                    jdbcTemplate.batchUpdate("INSERT INTO p365_order_stock " +
                            " (id, code, document_date, branch_id, status, modified_date, retailer_id, discount, created_date, created_by," +
                            " modified_by, total, total_payment, account_id,partner_id, exchange_rate, delivery_date, vat)  " +
                            "  VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            Pos365OrderStock categories = orderStocks.get(i);
                            ps.setLong(1, categories.getId());
                            ps.setString(2, categories.getCode());
                            ps.setTimestamp(3, categories.getDocumentDate());
                            ps.setLong(4, categories.getBranchId() == null ? 0 : categories.getBranchId());
                            ps.setInt(5, categories.getStatus());
                            ps.setTimestamp(6, categories.getModifiedDate());
                            ps.setLong(7, categories.getRetailerId() == null ? 0 : categories.getRetailerId());
                            ps.setBigDecimal(8, categories.getDiscount() == null ? BigDecimal.ZERO : categories.getDiscount());
                            ps.setTimestamp(9, categories.getCreatedDate());
                            ps.setLong(10, categories.getCreatedBy() == null ? 0 : categories.getCreatedBy());
                            ps.setLong(11, categories.getModifiedBy() == null ? 0 : categories.getModifiedBy());
                            ps.setBigDecimal(12, categories.getTotal() == null ? BigDecimal.ZERO : categories.getTotal());
                            ps.setBigDecimal(13, categories.getTotalPayment() == null ? BigDecimal.ZERO : categories.getTotalPayment());
                            ps.setLong(14, categories.getAccountId() == null ? 0 : categories.getAccountId());
                            ps.setLong(15, categories.getPartnerId() == null ? 0 : categories.getPartnerId());
                            ps.setBigDecimal(16, categories.getExchangeRate() == null ? BigDecimal.ZERO : categories.getExchangeRate());
                            ps.setTimestamp(17, categories.getDeliveryDate());
                            ps.setBigDecimal(18, categories.getVat() == null ? BigDecimal.ZERO : categories.getVat());
                        }

                        @Override
                        public int getBatchSize() {
                            return orderStocks.size();
                        }
                    });

//                    response.getResults().forEach(item -> {
//                        jdbcTemplate.update("INSERT INTO p365_order_stock " +
//                                        " (id, code, document_date, branch_id, status, modified_date, retailer_id, discount, created_date, created_by,"
//                                        +
//                                        " modified_by, total, total_payment, account_id,partner_id, exchange_rate, delivery_date, vat)  "
//                                        +
//                                        "  VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
//                                item.getId(), item.getCode(), item.getDocumentDate(),
//                                item.getBranchId(), item.getStatus(), item.getModifiedDate(),
//                                item.getRetailerId(), item.getDiscount(), item.getCreatedDate(),
//                                item.getCreatedBy(), item.getModifiedBy(), item.getTotal(),
//                                item.getTotalPayment(), item.getAccountId(), item.getPartnerId(), item.getExchangeRate(),
//                                item.getDeliveryDate(), item.getVat());
//                    });
//                    jdbcTemplate.execute("COMMIT");
                    insertedTotal += response.getResults().size();
                }
            } while (count > 0);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            LOGGER.info("SUMMARY: insertedTotal: {}, assumptionTotal: {}", insertedTotal,
                    assumptionTotal);
        }
    }
}
