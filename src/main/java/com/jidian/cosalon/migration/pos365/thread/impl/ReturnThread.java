package com.jidian.cosalon.migration.pos365.thread.impl;

import com.jidian.cosalon.migration.pos365.domainpos365.Pos365Return;
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

@Component("returnThread")
public class ReturnThread extends MyThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReturnThread.class);

    @Override
    public String getName() {
        return "ReturnThread";
    }

    @Override
    public void doRun() {
        int skip = 0;
        int top = 100;
        int count = 0;
        int insertedTotal = 0;
        int assumptionTotal = 0;

        try {
            jdbcTemplate.execute("TRUNCATE TABLE p365_return");
            do {
                BaseResponse<Pos365Return> response = pos365RetrofitService.listReturn(getMapHeaders2(), top, skip).execute().body();
                if (response != null) {
                    skip += top;
                    assumptionTotal = response.getCount();
                }
                count = 0;
                if (response != null && response.getResults() != null) {
                    count = response.getResults().size();

                    List<Pos365Return> returns = response.getResults();
                    jdbcTemplate.batchUpdate("INSERT INTO p365_return " +
                            "    (id, branch_id, code, created_by, created_date, description, discount, modified_by, modified_date, " +
                            "    retailer_id, order_id, return_date, partner_id, status, total, total_payment) " +
                            "    VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            Pos365Return categories = returns.get(i);
                            ps.setLong(1, categories.getId());
                            ps.setLong(2, categories.getBranchId() == null ? 0 : categories.getBranchId());
                            ps.setString(3, categories.getCode());
                            ps.setLong(4, categories.getCreatedBy() == null ? 0 : categories.getCreatedBy());
                            ps.setString(5, categories.getCreatedDate());
                            ps.setString(6, categories.getDescription());
                            ps.setBigDecimal(7, categories.getDiscount() == null ? BigDecimal.ZERO : categories.getDiscount());
                            ps.setLong(8, categories.getModifiedBy() == null ? 0 : categories.getBranchId());
                            ps.setString(9, categories.getModifiedDate());
                            ps.setLong(10, categories.getRetailerId() == null ? 0 : categories.getBranchId());
                            ps.setLong(11, categories.getOrderId() == null ? 0 : categories.getBranchId());
                            ps.setString(12, categories.getReturnDate());
                            ps.setLong(13, categories.getPartnerId() == null ? 0 : categories.getBranchId());
                            ps.setLong(14, categories.getStatus() == null ? 0 : categories.getBranchId());
                            ps.setBigDecimal(15, categories.getTotal() == null ? BigDecimal.ZERO : categories.getTotal());
                            ps.setBigDecimal(16, categories.getTotalPayment() == null ? BigDecimal.ZERO : categories.getTotalPayment());
                        }

                        @Override
                        public int getBatchSize() {
                            return returns.size();
                        }
                    });

//                    response.getResults().forEach(item -> {
//                        jdbcTemplate.update(
//                                "INSERT INTO p365_return " +
//                                        "    (id, branch_id, code, created_by, created_date, description, discount, modified_by, modified_date, " +
//                                        "    retailer_id, order_id, return_date, partner_id, status, total, total_payment) " +
//                                        "    VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
//                                item.getId(), item.getBranchId(), item.getCode(), item.getCreatedBy(), item.getCreatedDate(), item.getDescription(), item.getDiscount(), item.getModifiedBy(), item.getModifiedDate(),
//                                item.getRetailerId(), item.getOrderId(), item.getReturnDate(), item.getPartnerId(), item.getStatus(), item.getTotal(), item.getTotalPayment());
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
