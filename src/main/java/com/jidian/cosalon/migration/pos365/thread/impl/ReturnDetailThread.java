package com.jidian.cosalon.migration.pos365.thread.impl;

import com.jidian.cosalon.migration.pos365.domainpos365.Pos365Partner;
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
import java.util.List;

@Component("returnDetailThread")
public class ReturnDetailThread extends MyThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReturnDetailThread.class);

    @Override
    public String getName() {
        return "returnDetailThread";
    }

    private int skip = 0;
    private int top = 100;
    private int count = 0;
    private int insertedTotal = 0;
    private int assumptionTotal = 0;

    @Override
    public void doRun() {
        try {
            jdbcTemplate.execute("TRUNCATE TABLE p365_return_detail");

            final List<Long> returnIds = jdbcTemplate.queryForList("select distinct id from p365_return", Long.class);
            if (returnIds != null && !returnIds.isEmpty()) {
                returnIds.forEach(returnId -> {
                    try {
                        skip = 0;
                        count = 0;
                        do {
                            BaseResponse<Pos365ReturnDetail> response = pos365RetrofitService.listReturnDetail(getMapHeaders2(), top, skip, returnId).execute().body();
                            if (response != null) {
                                skip += top;
                                assumptionTotal = response.getCount();
                            }
                            count = 0;
                            if (response != null && response.getResults() != null) {
                                count = response.getResults().size();

                                List<Pos365ReturnDetail> returnDetails = response.getResults();
                                jdbcTemplate.batchUpdate("INSERT INTO p365_return_detail " +
                                        "    (id, return_id, product_id, quantity, price, is_large_unit, conversion_value) " +
                                        "    VALUES (?,?,?,?,?,?,?)", new BatchPreparedStatementSetter() {
                                    @Override
                                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                                        Pos365ReturnDetail categories = returnDetails.get(i);
                                        ps.setLong(1, categories.getId());
                                        ps.setLong(2, categories.getReturnId() == null ? 0 : categories.getReturnId());
                                        ps.setLong(3, categories.getProductId() == null ? 0 : categories.getProductId());
                                        ps.setLong(4, categories.getQuantity() == null ? 0 : categories.getQuantity());
                                        ps.setBigDecimal(5, categories.getPrice() == null ? BigDecimal.ZERO : categories.getPrice());
                                        ps.setBoolean(6, categories.getIsLargeUnit());
                                        ps.setInt(7, categories.getConversionValue());
                                    }

                                    @Override
                                    public int getBatchSize() {
                                        return returnDetails.size();
                                    }
                                });

//                                response.getResults().forEach(item -> {
//                                    jdbcTemplate.update(
//                                            "INSERT INTO p365_return_detail " +
//                                                    "    (id, return_id, product_id, quantity, price, is_large_unit, conversion_value) " +
//                                                    "    VALUES (?,?,?,?,?,?,?)",
//                                            item.getId(), item.getReturnId(), item.getProductId(), item.getQuantity(), item.getPrice(), item.getIsLargeUnit(), item.getConversionValue());
//                                });
//                                jdbcTemplate.execute("COMMIT");
                                insertedTotal += response.getResults().size();
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
