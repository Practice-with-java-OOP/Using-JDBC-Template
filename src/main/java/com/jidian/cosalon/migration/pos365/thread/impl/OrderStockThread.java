package com.jidian.cosalon.migration.pos365.thread.impl;

import com.jidian.cosalon.migration.pos365.domainpos365.Post365Categories;
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
        try {
            jdbcTemplate.execute("TRUNCATE TABLE p365_order_stock");

            BaseResponse<Post365OrderStock> response = pos365RetrofitService.listOrderStock(getMapHeaders2()).execute().body();
            LOGGER.info("Response: {}", response);
            response.getResults().forEach(item -> {
                jdbcTemplate.update("INSERT INTO p365_order_stock " +
                                " (id, code, document_date, branch_id, status, modified_date, retailer_id, discount, created_date, created_by," +
                                " modified_by, total, total_payment, account_id, exchange_rate, delivery_date, vat)  " +
                                "  VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                        item.getId(), item.getCode(), item.getDocumentDate(), item.getBranchId(), item.getStatus(), item.getModifiedDate(),
                        item.getRetailerId(), item.getDiscount(), item.getCreatedDate(), item.getCreatedBy(), item.getModifiedBy(), item.getTotal(),
                        item.getTotalPayment(), item.getAccountId(), item.getExchangeRate(), item.getDeliveryDate(), item.getVat());
            });
            jdbcTemplate.execute("COMMIT");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
