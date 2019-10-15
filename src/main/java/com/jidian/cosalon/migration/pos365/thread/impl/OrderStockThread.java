package com.jidian.cosalon.migration.pos365.thread.impl;

import com.jidian.cosalon.migration.pos365.domainpos365.Post365OrderStock;
import com.jidian.cosalon.migration.pos365.dto.BaseResponse;
import com.jidian.cosalon.migration.pos365.thread.MyThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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
                BaseResponse<Post365OrderStock> response = pos365RetrofitService
                    .listOrderStock(getMapHeaders2(), top, skip).execute().body();
                if (response != null) {
                    skip += top;
                    assumptionTotal = response.getCount();
                }
                count = 0;
                if (response != null && response.getResults() != null) {
                    count = response.getResults().size();
                    response.getResults().forEach(item -> {
                        jdbcTemplate.update("INSERT INTO p365_order_stock " +
                                " (id, code, document_date, branch_id, status, modified_date, retailer_id, discount, created_date, created_by,"
                                +
                                " modified_by, total, total_payment, account_id, exchange_rate, delivery_date, vat)  "
                                +
                                "  VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                            item.getId(), item.getCode(), item.getDocumentDate(),
                            item.getBranchId(), item.getStatus(), item.getModifiedDate(),
                            item.getRetailerId(), item.getDiscount(), item.getCreatedDate(),
                            item.getCreatedBy(), item.getModifiedBy(), item.getTotal(),
                            item.getTotalPayment(), item.getAccountId(), item.getExchangeRate(),
                            item.getDeliveryDate(), item.getVat());
                    });
                    jdbcTemplate.execute("COMMIT");
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
