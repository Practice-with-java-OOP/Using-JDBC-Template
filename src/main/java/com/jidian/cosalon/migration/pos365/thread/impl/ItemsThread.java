package com.jidian.cosalon.migration.pos365.thread.impl;

import com.jidian.cosalon.migration.pos365.domainpos365.Post365Items;
import com.jidian.cosalon.migration.pos365.dto.BaseResponse;
import com.jidian.cosalon.migration.pos365.thread.MyThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("itemsThread")
public class ItemsThread extends MyThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(ItemsThread.class);

    @Override
    public String getName() {
        return "itemsThread";
    }

    @Override
    public void doRun() {
        int skip = 0;
        int top = 100;
        int count = 0;
        int insertedTotal = 0;
        int assumptionTotal = 0;

        try {
            jdbcTemplate.execute("TRUNCATE TABLE p365_items");
            do {
                BaseResponse<Post365Items> response = pos365RetrofitService.listItems(getMapHeaders2(), top, skip).execute().body();
                if (response != null) {
                    skip += top;
                    assumptionTotal = response.getCount();
                }
                count = 0;
                if (response != null && response.getResults() != null) {
                    count = response.getResults().size();
                    //do id của dữ liệu hiện tại trả về đang = 0 hết nên đang để id tự động tăng.
                    response.getResults().forEach(item -> {
                        jdbcTemplate.update(
                                "INSERT INTO p365_items " +
                                        "    (product_id, name, attributes_name, code, cost, multi_unit, price, original_price, original_price_large_unit, price_large_unit) " +
                                        "VALUES (?,?,?,?,?,?,?,?,?,?)",
                                item.getProductId(), item.getName(), item.getAttributesName(), item.getCode(), item.getCost(), item.getMultiUnit(),
                                item.getPrice(), item.getOriginalPrice(), item.getOriginalPriceLargeUnit(), item.getPriceLargeUnit());
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
