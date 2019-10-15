package com.jidian.cosalon.migration.pos365.thread.impl;

import com.jidian.cosalon.migration.pos365.domainpos365.Pos365Order;
import com.jidian.cosalon.migration.pos365.dto.BaseResponse;
import com.jidian.cosalon.migration.pos365.thread.MyThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("orderThread")
public class OrderThread extends MyThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderThread.class);

    @Override
    public String getName() {
        return "OrderThread";
    }

    @Override
    public void doRun() {
        int count;
        int skip = 0;
        int top = 100;
        int insertedTotal = 0;
        int assumptionTotal = 0;
        try {
            jdbcTemplate.execute("TRUNCATE TABLE p365_orders");
            do {
                BaseResponse<Pos365Order> response = pos365RetrofitService
                    .listOrders(getMapHeaders2(), top, skip).execute().body();
                if (response != null) {
                    skip += top;
                    assumptionTotal = response.getCount();
                }
                count = 0;
                if (response != null && response.getResults() != null) {
                    count = response.getResults().size();
                    response.getResults().forEach(item -> jdbcTemplate.update("INSERT "
                            + " INTO "
                            + " p365_orders"
                            + " (id, amount_received, branch_id, code, created_by, created_date, discount,"
                            + "  discount_to_view, excess_cash, is_online, lading_code, more_attributes,"
                            + "  number_of_guests, partner_id, price_book_id, purchase_date, retailer_id,"
                            + "  shipping_cost, shipping_cost_for_partner, sold_by_id, status, total, total_payment,"
                            + "  vat, voucher) "
                            + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                        item.getId(), item.getAmountReceived(), item.getBranchId(), item.getCode(),
                        item.getCreatedBy(), item.getCreatedDate(), item.getDiscount(),
                        item.getDiscountToView(), item.getExcessCash(), item.getIsOnline(),
                        item.getLadingCode(), item.getMoreAttributes(),
                        item.getNumberOfGuests(), item.getPartnerId(), item.getPriceBookId(),
                        item.getPurchaseDate(), item.getRetailerId(), item.getShippingCost(),
                        item.getShippingCostForPartner(), item.getSoldById(), item.getStatus(),
                        item.getTotal(), item.getTotalPayment(), item.getVat(), item.getVoucher()));
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
