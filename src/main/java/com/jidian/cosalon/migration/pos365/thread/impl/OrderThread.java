package com.jidian.cosalon.migration.pos365.thread.impl;

import com.jidian.cosalon.migration.pos365.domainpos365.Pos365Order;
import com.jidian.cosalon.migration.pos365.dto.BaseResponse;
import com.jidian.cosalon.migration.pos365.thread.MyThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

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
                    List<Pos365Order> pos365OrderList = response.getResults();

                    jdbcTemplate.batchUpdate("INSERT "
                            + " INTO "
                            + " p365_orders"
                            + " (id, amount_received, branch_id, code, created_by, created_date, discount,"
                            + "  discount_to_view, excess_cash, is_online, lading_code, more_attributes,"
                            + "  number_of_guests, partner_id, price_book_id, purchase_date, retailer_id,"
                            + "  shipping_cost, shipping_cost_for_partner, sold_by_id, status, total, total_payment,"
                            + "  vat, voucher) "
                            + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            ps.setLong(1, pos365OrderList.get(i).getId());
                            ps.setBigDecimal(2, pos365OrderList.get(i).getAmountReceived());
                            ps.setLong(3, pos365OrderList.get(i).getBranchId());
                            ps.setString(4, pos365OrderList.get(i).getCode());
                            ps.setLong(5, pos365OrderList.get(i).getCreatedBy());
                            ps.setString(6, pos365OrderList.get(i).getCreatedDate());
                            ps.setBigDecimal(7, pos365OrderList.get(i).getDiscount());
                            ps.setString(8, pos365OrderList.get(i).getDiscountToView());
                            ps.setBigDecimal(9, pos365OrderList.get(i).getExcessCash());
                            ps.setBoolean(10, pos365OrderList.get(i).getIsOnline());
                            ps.setString(11, pos365OrderList.get(i).getLadingCode());
                            ps.setString(12, pos365OrderList.get(i).getMoreAttributes());
                            ps.setLong(13, pos365OrderList.get(i).getNumberOfGuests());
                            ps.setLong(14, pos365OrderList.get(i).getPartnerId() == null ? 0 : pos365OrderList.get(i).getPartnerId());
                            ps.setLong(15, pos365OrderList.get(i).getPriceBookId() == null ? 0 : pos365OrderList.get(i).getPriceBookId());
                            ps.setString(16, pos365OrderList.get(i).getPurchaseDate());
                            ps.setLong(17, pos365OrderList.get(i).getRetailerId());
                            ps.setBigDecimal(18, pos365OrderList.get(i).getShippingCost());
                            ps.setLong(19, pos365OrderList.get(i).getShippingCostForPartner());
                            ps.setLong(20, pos365OrderList.get(i).getSoldById());
                            ps.setLong(21, pos365OrderList.get(i).getStatus());
                            ps.setBigDecimal(22, pos365OrderList.get(i).getTotal());
                            ps.setBigDecimal(23, pos365OrderList.get(i).getTotalPayment());
                            ps.setBigDecimal(24, pos365OrderList.get(i).getVat());
                            ps.setBigDecimal(25, pos365OrderList.get(i).getVoucher());
                        }

                        @Override
                        public int getBatchSize() {
                            return pos365OrderList.size();
                        }
                    });
//                    response.getResults().forEach(item -> jdbcTemplate.update("INSERT "
//                            + " INTO "
//                            + " p365_orders"
//                            + " (id, amount_received, branch_id, code, created_by, created_date, discount,"
//                            + "  discount_to_view, excess_cash, is_online, lading_code, more_attributes,"
//                            + "  number_of_guests, partner_id, price_book_id, purchase_date, retailer_id,"
//                            + "  shipping_cost, shipping_cost_for_partner, sold_by_id, status, total, total_payment,"
//                            + "  vat, voucher) "
//                            + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
//                        item.getId(), item.getAmountReceived(), item.getBranchId(), item.getCode(),
//                        item.getCreatedBy(), item.getCreatedDate(), item.getDiscount(),
//                        item.getDiscountToView(), item.getExcessCash(), item.getIsOnline(),
//                        item.getLadingCode(), item.getMoreAttributes(),
//                        item.getNumberOfGuests(), item.getPartnerId(), item.getPriceBookId(),
//                        item.getPurchaseDate(), item.getRetailerId(), item.getShippingCost(),
//                        item.getShippingCostForPartner(), item.getSoldById(), item.getStatus(),
//                        item.getTotal(), item.getTotalPayment(), item.getVat(), item.getVoucher()));
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
