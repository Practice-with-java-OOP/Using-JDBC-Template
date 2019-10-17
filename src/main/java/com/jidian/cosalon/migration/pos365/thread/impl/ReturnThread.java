package com.jidian.cosalon.migration.pos365.thread.impl;

import com.jidian.cosalon.migration.pos365.domainpos365.Pos365Product;
import com.jidian.cosalon.migration.pos365.domainpos365.Pos365Return;
import com.jidian.cosalon.migration.pos365.dto.BaseResponse;
import com.jidian.cosalon.migration.pos365.thread.MyThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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
                    response.getResults().forEach(item -> {
                        jdbcTemplate.update(
                                "INSERT INTO p365_return " +
                                        "    (id, branch_id, code, created_by, created_date, description, discount, modified_by, modified_date, " +
                                        "    retailer_id, order_id, return_date, partner_id, status, total, total_payment) " +
                                        "    VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                                item.getId(), item.getBranchId(), item.getCode(), item.getCreatedBy(), item.getCreatedDate(), item.getDescription(), item.getDiscount(), item.getModifiedBy(), item.getModifiedDate(),
                                item.getRetailerId(), item.getOrderId(), item.getReturnDate(), item.getPartnerId(), item.getStatus(), item.getTotal(), item.getTotalPayment());
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
