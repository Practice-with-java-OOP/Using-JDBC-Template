package com.jidian.cosalon.migration.pos365.thread.impl;

import com.jidian.cosalon.migration.pos365.domainpos365.Pos365TransfersDetail;
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

@Component("transfersDetailThread")
public class TransfersDetailThread extends MyThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransfersDetailThread.class);
    private int insertedTotal = 0;
    private int assumptionTotal = 0;
    private int count = 0;

    @Override
    public String getName() {
        return "TransfersDetailThread";
    }

    @Override
    public void doRun() {
        try {
            jdbcTemplate.execute("TRUNCATE TABLE p365_transfers_detail");
            List<Long> transferIds = jdbcTemplate
                    .queryForList("SELECT DISTINCT id FROM p365_transfers", Long.class);
            if (!transferIds.isEmpty()) {
                transferIds.forEach(transferId -> {
                    try {
                        do {
                            BaseResponse<Pos365TransfersDetail> response = pos365RetrofitService
                                    .listTransferDetails(getMapHeaders2(), transferId).execute().body();
                            if (response != null) {
                                assumptionTotal += response.getCount();
                            }
                            count = 0;
                            if (response != null) {

                                List<Pos365TransfersDetail> transfersDetails = response.getResults();
                                jdbcTemplate.batchUpdate("INSERT INTO p365_transfers_detail(id, conversion_value, "
                                        + "price, price_large_unit, product_id, product_type, "
                                        + "quantity, transfer_id) "
                                        + "VALUES (?,?,?,?,?,?,?,?)", new BatchPreparedStatementSetter() {
                                    @Override
                                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                                        Pos365TransfersDetail categories = transfersDetails.get(i);
                                        ps.setLong(1, categories.getId());
                                        ps.setInt(2, categories.getConversionValue() == null ? 0 : categories.getConversionValue());
                                        ps.setBigDecimal(3, categories.getPrice() == null ? BigDecimal.ZERO : categories.getPrice());
                                        ps.setBigDecimal(4, categories.getPriceLargeUnit() == null ? BigDecimal.ZERO : categories.getPriceLargeUnit());
                                        ps.setLong(5, categories.getProductId() == null ? 0 : categories.getProductId());
                                        ps.setLong(6, categories.getProductType() == null ? 0 : categories.getProductType());
                                        ps.setInt(7, categories.getQuantity() == null ? 0 : categories.getQuantity());
                                        ps.setLong(8, categories.getTransferId() == null ? 0 : categories.getTransferId());
                                    }

                                    @Override
                                    public int getBatchSize() {
                                        return transfersDetails.size();
                                    }
                                });

//                                response.getResults().forEach(item -> jdbcTemplate.update(
//                                    "INSERT INTO p365_transfers_detail(id, conversion_value, "
//                                        + "price, price_large_unit, product_id, product_type, "
//                                        + "quantity, transfer_id) "
//                                        + "VALUES (?,?,?,?,?,?,?,?)",
//                                    item.getId(), item.getConversionValue(), item.getPrice(),
//                                    item.getPriceLargeUnit(), item.getProductId(),
//                                    item.getProductType(),
//                                    item.getQuantity(), item.getTransferId()));
//                                jdbcTemplate.execute("COMMIT");
                                insertedTotal += response.getResults().size();
                            }
                        } while (count > 0);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            LOGGER.info("SUMMARY: insertedTotal: {}, assumptionTotal: {}", insertedTotal,
                    assumptionTotal);
        }
    }
}
