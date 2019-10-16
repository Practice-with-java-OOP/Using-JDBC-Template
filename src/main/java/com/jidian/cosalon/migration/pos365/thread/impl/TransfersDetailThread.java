package com.jidian.cosalon.migration.pos365.thread.impl;

import com.jidian.cosalon.migration.pos365.domainpos365.Pos365TransfersDetail;
import com.jidian.cosalon.migration.pos365.dto.BaseResponse;
import com.jidian.cosalon.migration.pos365.thread.MyThread;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("transfersDetailThread")
public class TransfersDetailThread extends MyThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransfersDetailThread.class);
    private int insertedTotal = 0;
    private int assumptionTotal = 0;
    private int skip = 0;
    private int top = 100;
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
                                skip += top;
                                assumptionTotal += response.getCount();
                            }
                            count = 0;
                            if (response != null) {
                                response.getResults().forEach(item -> jdbcTemplate.update(
                                    "INSERT INTO p365_transfers_detail(id, conversion_value, "
                                        + "price, price_large_unit, product_id, product_type, "
                                        + "quantity, transfer_id) "
                                        + "VALUES (?,?,?,?,?,?,?,?)",
                                    item.getId(), item.getConversionValue(), item.getPrice(),
                                    item.getPriceLargeUnit(), item.getProductId(),
                                    item.getProductType(),
                                    item.getQuantity(), item.getTransferId()));
                                jdbcTemplate.execute("COMMIT");
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
