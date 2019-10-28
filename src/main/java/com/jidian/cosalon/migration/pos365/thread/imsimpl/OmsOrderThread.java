package com.jidian.cosalon.migration.pos365.thread.imsimpl;

import com.jidian.cosalon.migration.pos365.Utils;
import com.jidian.cosalon.migration.pos365.domain.BhairStore;
import com.jidian.cosalon.migration.pos365.domain.OmsOrder;
import com.jidian.cosalon.migration.pos365.domainpos365.Pos365BranchImsWarehouse;
import com.jidian.cosalon.migration.pos365.domainpos365.Pos365Order;
import com.jidian.cosalon.migration.pos365.domainpos365.Pos365OrderDetail;
import com.jidian.cosalon.migration.pos365.thread.MyThread;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component("omsOrderThread")
public class OmsOrderThread extends MyThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(OmsOrderThread.class);

    @Override
    public String getName() {
        return "OmsOrderThread";
    }

    private int insertedTotal = 0;
    private int assumptionTotal = 0;
    private long startOrderId = 1146L;
    private long endOrderId = 0L;

    @Override
    public void doRun() {
        insertedTotal = 0;
        assumptionTotal = 0;
        try {
            jdbcTemplate.update("update p365_orders set code = concat(code, '2') where id = 19302307 and code = 'HÁ002106' ");
            jdbcTemplate.update("update p365_orders set code = concat(code, '2') where id = 16281024 and code = 'ha00764a' ");

            final Map<Long, BranchWarehouseStoreQueryDto> branchWarehouseMap = jdbcTemplate.query(
                    "select a.id as warehouse_id, b.id as branch_id, c.store_id " +
                            "from ims_warehouse a " +
                            "    left join p365_branchs b on a.name = b.name " +
                            "    left join ims_warehouse_store c on c.warehouse_id = a.id ",
                    (rs, rowNum) -> {
                        final BranchWarehouseStoreQueryDto result = new BranchWarehouseStoreQueryDto();
                        result.setImsWarehouseId(rs.getLong("warehouse_id"));
                        result.setP365BranchId(rs.getLong("branch_id"));
                        result.setBhairStoreId(rs.getLong("store_id"));
                        return result;
                    }
            ).stream().collect(Collectors.toMap(BranchWarehouseStoreQueryDto::getP365BranchId, branchWarehouseStoreQueryDto -> branchWarehouseStoreQueryDto));
            final Map<Long, BhairStore> storeMap = bhairJdbcTemplate.query(
                    "select * from cosalon_bhair.bhair_store a ",
                    (rs, rowNum) -> {
                        final BhairStore result = new BhairStore();
                        result.setId(rs.getLong("id"));
                        result.setAddress(rs.getString("address"));
                        result.setName(rs.getString("name"));
                        return result;
                    }
            ).stream().collect(Collectors.toMap(BhairStore::getId, bhairStore -> bhairStore));

            final List<RetailGoodsReceiptQueryDto> items = jdbcTemplate.query(
                    "select distinct a.*, b.name as partner_name, b.phone as partner_phone from ( " +
                            "    select * from p365_orders a where a.id in (select a.order_id from p365_order_detail a " +
                            "        where a.product_id in (select id from p365_products a where a.product_type = 2))) a " +
                            "    left outer join p365_partners b on a.partner_id = b.id " +
                            "order by a.id asc ",
                    (rs, rowNum) -> {
                        final RetailGoodsReceiptQueryDto result = new RetailGoodsReceiptQueryDto();
                        result.setId(rs.getLong("id"));
                        result.setAmountReceived(rs.getBigDecimal("amount_received"));
                        result.setBranchId(rs.getLong("branch_id"));
                        result.setCode(rs.getString("code"));
                        result.setCreatedDate(rs.getString("created_date"));
                        result.setCreatedBy(rs.getLong("created_by"));
                        result.setDiscount(rs.getBigDecimal("discount"));
                        result.setDiscountToView(rs.getString("discount_to_view"));
                        result.setExcessCash(rs.getBigDecimal("excess_cash"));
                        result.setIsOnline(rs.getBoolean("is_online"));
                        result.setLadingCode(rs.getString("lading_code"));
                        result.setMoreAttributes(rs.getString("more_attributes"));
                        result.setNumberOfGuests(rs.getInt("number_of_guests"));
                        result.setPartnerId(rs.getLong("partner_id"));
                        result.setPriceBookId(rs.getLong("price_book_id"));
                        result.setPurchaseDate(rs.getString("purchase_date"));
                        result.setRetailerId(rs.getLong("retailer_id"));
                        result.setShippingCost(rs.getBigDecimal("shipping_cost"));
                        result.setShippingCostForPartner(rs.getLong("shipping_cost_for_partner"));
                        result.setSoldById(rs.getLong("sold_by_id"));
                        result.setStatus(rs.getInt("status"));
                        result.setTotal(rs.getBigDecimal("total"));
                        result.setTotalPayment(rs.getBigDecimal("total_payment"));
                        result.setVat(rs.getBigDecimal("vat"));
                        result.setVoucher(rs.getBigDecimal("voucher"));
                        result.setPartnerName(rs.getString("partner_name"));
                        result.setPartnerPhone(rs.getString("partner_phone"));
                        return result;
                }
            );
            assumptionTotal = items.size();

            final List<RetailGoodsReceiptChemicalQueryDto> details = jdbcTemplate.query(
                    "select " +
                            "    a.*, b.name as product_name, " +
                            "    c.name as stylist_name, c.username as stylist_code, c.phone as stylist_phone, " +
                            "    d.name as ass_name, d.username as ass_code, d.phone as ass_phone " +
                            "from (select * from p365_order_detail a where a.product_id in (select id from p365_products a where a.product_type = 2)) a " +
                            "    left outer join p365_products b on a.product_id = b.id " +
                            "    left outer join p365_users c on a.sold_by_id = c.id " +
                            "    left outer join p365_users d on a.assistant_by_id = d.id ",
                    (rs, rowNum) -> {
                        final RetailGoodsReceiptChemicalQueryDto result = new RetailGoodsReceiptChemicalQueryDto();
                        result.setId(rs.getLong("id"));
                        result.setConversionValue(rs.getInt("conversion_value"));
                        result.setCoefficient(rs.getInt("coefficient"));
                        result.setIsLargeUnit(rs.getBoolean("is_large_unit"));
                        result.setPrice(rs.getBigDecimal("price"));
                        result.setBasePrice(rs.getBigDecimal("base_price"));
                        result.setProductId(rs.getLong("product_id"));
                        result.setQuantity(rs.getInt("quantity"));
                        result.setOrderId(rs.getLong("order_id"));
                        result.setProcessed(rs.getInt("processed"));
                        result.setSoldById(rs.getLong("sold_by_id"));
                        result.setAssistantById(rs.getLong("assistant_by_id"));
                        result.setStylistName(rs.getString("stylist_name"));
                        result.setStylistCode(rs.getString("stylist_code"));
                        result.setStylistPhone(rs.getString("stylist_phone"));
                        result.setProductName(rs.getString("product_name"));
                        result.setTechnicianName(rs.getString("ass_name"));
                        result.setTechnicianCode(rs.getString("ass_code"));
                        result.setTechnicianPhone(rs.getString("ass_phone"));
                        return result;
                    }
            );
            Map<Long, List<RetailGoodsReceiptChemicalQueryDto>> detailMap = details.stream().collect(Collectors.groupingBy(Pos365OrderDetail::getOrderId));

            if (startOrderId > 0L) {
                final List<OmsOrder> existing = omsJdbcTemplate.query(
                        "select * from cosalon_oms.oms_order a where a.id >= ? ",
                        (rs, rowNum) -> {
                            final OmsOrder result = new OmsOrder();
                            result.setId(rs.getLong("id"));
                            result.setOrderNum(rs.getString("order_num"));
                            return result;
                        },
                        startOrderId
                );
                existing.forEach(omsOrder -> {
                    omsJdbcTemplate.update("delete from cosalon_oms.oms_order_item where order_id = ?", omsOrder.getId());
                });
                omsJdbcTemplate.update("delete from cosalon_oms.oms_order where id >= ?", startOrderId);
            }

            for (int index = 0; index < items.size(); index++) {
                final RetailGoodsReceiptQueryDto item = items.get(index);
                KeyHolder keyHolder = new GeneratedKeyHolder();
                int finalIndex = index;
                insertedTotal += omsJdbcTemplate.update(
                        connection -> {
                            PreparedStatement ps = connection.prepareStatement(
                                    "INSERT INTO cosalon_oms.oms_order (id, gmt_create, gmt_modified, version, actual_amount_paid, " +
                                            "    booking_address, booking_item_names, booking_name, booking_phone_num, booking_time, " +
                                            "    cancel_reason, is_evaluated, order_num, payment_method, payment_time, reject_reason, " +
                                            "    remarks, status, store_id, store_name, stylist_avatar, stylist_id, stylist_name, stylist_phone_num, " +
                                            "    total_amount, transaction_num, user_avatar, user_id, channel_booking) " +
                                            "VALUES (?,CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP(),0,?, " +
                                            "    ?,?,?,?,?, " +
                                            "    null,'0',?,null,?,null, " +
                                            "    'POS365',?,?,?,?,?,?,?, " +
                                            "    ?,null,'',0,5) ",
                                    new String[] {"id"});
                            int i = 1;
                            final BhairStore store = storeMap.get(branchWarehouseMap.get(item.getBranchId()).bhairStoreId);
                            String productNames = "";
                            RetailGoodsReceiptChemicalQueryDto detail = null;
                            if (detailMap.containsKey(item.getId())) {
                                productNames = detailMap.get(item.getId()).stream()
                                        .map(RetailGoodsReceiptChemicalQueryDto::getProductName)
                                        .collect(Collectors.joining(","));
                                detail = detailMap.get(item.getId()).isEmpty() ? null: detailMap.get(item.getId()).get(0);
                            }
                            ps.setLong(i++, finalIndex + startOrderId);
                            ps.setBigDecimal(i++, item.getTotalPayment());
                            ps.setString(i++, Utils.nvl(store.getAddress()));
                            ps.setString(i++, Utils.nvl(productNames)); //booking_item_names
                            ps.setString(i++, Utils.nvl(item.getPartnerName()));
                            ps.setString(i++, Utils.nvl(item.getPartnerPhone()));
                            ps.setTimestamp(i++, Utils.convertTimestamp(item.getCreatedDate()));
                            ps.setString(i++, Utils.nvl(item.getCode()).trim());
                            ps.setTimestamp(i++, Utils.convertTimestamp(item.getPurchaseDate()));
                            ps.setInt(i++, StatusEnum.resolve(item.getStatus()) == null ? 1: StatusEnum.resolve(item.getStatus()).getGoodsReceiptStatus());
                            ps.setLong(i++, store.getId());
                            ps.setString(i++, store.getName());
                            ps.setString(i++, ""); //stylist_avatar
                            ps.setLong(i++, 0L); //stylist_id
                            ps.setString(i++, Utils.nvl(detail == null ? "": detail.getStylistName())); //stylist_name
                            ps.setString(i++, Utils.nvl(detail == null ? "": detail.getStylistPhone())); //stylist_phone_num
                            ps.setBigDecimal(i++, item.getTotal());
                            return ps;
                        },
                        keyHolder);
                List<RetailGoodsReceiptChemicalQueryDto> subDetails = detailMap.get(item.getId());
                if (keyHolder.getKey() != null && subDetails != null) {
                    subDetails.forEach(subDetail -> {
                        omsJdbcTemplate.update(
                                connection -> {
                                    PreparedStatement ps = connection.prepareStatement(
                                            "INSERT INTO cosalon_oms.oms_order_item (gmt_create, gmt_modified, version, actual_amount_paid, " +
                                                    "    image_url, item_id, item_name, item_properties, nursing_staff_fee, nursing_staff_id, quantity, " +
                                                    "    rented_seat_id, seat_owner_id, seat_rental_fee, total_price, unit_price, order_id, " +
                                                    "    nursing_staff_avatar, nursing_staff_display_name, rented_seat_name, seat_owner_avatar, " +
                                                    "    seat_owner_display_name, price_list_id, platform_commission_ratio) " +
                                                    "VALUES (CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP(),0,?, " +
                                                    "    null,?,?,null,null,?,?, " +
                                                    "    null,null,null,?,?,?, " +
                                                    "    null,?,null,null, " +
                                                    "    null,?,0.0) ",
                                            new String[] {"id"});
                                    int i = 1;
                                    ps.setBigDecimal(i++, subDetail.getPrice());
                                    ps.setLong(i++, 1000000L + subDetail.getId());
                                    ps.setString(i++, subDetail.getProductName());
                                    ps.setLong(i++, 0L);
                                    ps.setInt(i++, subDetail.getQuantity());
                                    ps.setBigDecimal(i++, subDetail.getPrice());
                                    ps.setBigDecimal(i++, subDetail.getPrice());
                                    ps.setLong(i++, keyHolder.getKey().longValue());
                                    ps.setString(i++, subDetail.getTechnicianName());
                                    ps.setLong(i++, 0L);
                                    return ps;
                                });
                    });
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            LOGGER.info("SUMMARY: insert/update total: {}, pos365 product total: {}", insertedTotal, assumptionTotal);
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class RetailGoodsReceiptQueryDto extends Pos365Order {
        private Long omsOrderId;
        private String partnerName;
        private String partnerPhone;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class RetailGoodsReceiptChemicalQueryDto extends Pos365OrderDetail {
        private String stylistName;
        private String stylistCode;
        private String stylistPhone;
        private String productName;
        private String technicianName;
        private String technicianCode;
        private String technicianPhone;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class BranchWarehouseStoreQueryDto extends Pos365BranchImsWarehouse {
        private Long bhairStoreId;
    }

    private enum StatusEnum {
        EXECUTING(1, 1),
        FINISHED(2, 5),
        CANCELED(3, 6),
        DELIVERING(4, 3)
        ;

        @Getter
        private final int value;
        @Getter
        private final int goodsReceiptStatus;

        StatusEnum(int value, int grStatus) {
            this.value = value;
            this.goodsReceiptStatus = grStatus;
        }

        public static StatusEnum resolve(long value) {
            for (StatusEnum mEnum : values()) {
                if (mEnum.value == value) {
                    return mEnum;
                }
            }
            return null;
        }
    }
}
