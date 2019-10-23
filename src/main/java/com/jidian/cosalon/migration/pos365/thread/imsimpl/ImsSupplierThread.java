package com.jidian.cosalon.migration.pos365.thread.imsimpl;

import com.jidian.cosalon.migration.pos365.Utils;
import com.jidian.cosalon.migration.pos365.domain.ImsSupplier;
import com.jidian.cosalon.migration.pos365.domainpos365.Pos365Partner;
import com.jidian.cosalon.migration.pos365.thread.MyThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component("imsSupplierThread")
public class ImsSupplierThread extends MyThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImsSupplierThread.class);

    @Override
    public String getName() {
        return "imsSupplierThread";
    }

    @Override
    public void doRun() {
        try {
            final List<Pos365Partner> partners = jdbcTemplate.query("select * from p365_partners p where p.type = 2", (rs, rowNum) -> {
                final Pos365Partner result = new Pos365Partner();
                result.setId(rs.getLong("id"));
                result.setCode(rs.getString("code"));
                result.setName(rs.getString("name"));
                result.setType(rs.getInt("type"));
                result.setAddress(rs.getString("address"));
                result.setBranchId(rs.getLong("branch_id"));
                result.setCompany(rs.getString("company"));
                result.setCreatedDate(rs.getString("created_date"));
                result.setModifiedDate(rs.getString("modified_date"));
                result.setPhone(rs.getString("phone"));
                result.setDebt(rs.getBigDecimal("debt"));
                result.setTotalDebt(rs.getBigDecimal("total_debt"));
                result.setTotalTransactionValue(rs.getLong("total_transaction_value"));
                result.setTransactionValue(rs.getLong("transaction_value"));
                return result;
            });


            final List<ImsSupplier> suppliers = jdbcTemplate.query("select * from ims_supplier", (rs, rowNum) -> {
                final ImsSupplier result = new ImsSupplier();
                result.setId(rs.getLong("id"));
                result.setGmtCreate(rs.getTimestamp("gmt_create"));
                result.setGmtModified(rs.getTimestamp("gmt_modified"));
                result.setVersion(rs.getInt("version"));
                result.setCode(rs.getString("code"));
                result.setName(rs.getString("name"));
                result.setPhoneNum(rs.getString("phone_number_1"));
                result.setPhoneNum2(rs.getString("phone_number_2"));
                result.setAddress(rs.getString("address_1"));
                result.setAddress2(rs.getString("address_2"));
                result.setTransactionQuantity(rs.getInt("transaction_quantity"));
                result.setTransactionAmount(rs.getBigDecimal("transaction_amount"));
                result.setStatus(rs.getInt("status"));
                result.setRemarks(rs.getString("remarks"));
                return result;
            });

            List<Pos365Partner> partnerInserts = new ArrayList<>();
            List<Pos365Partner> partnerUpdates = new ArrayList<>();

            Map<String, ImsSupplier> supplierByCode = suppliers.stream().collect(Collectors.toMap(ImsSupplier::getCode, supplier -> supplier));
            partners.forEach(partner -> {
                ImsSupplier supplier = supplierByCode.get(partner.getCode());
                if (supplier != null && supplier.getName().equals(partner.getName())) {
                    partnerUpdates.add(partner);
                } else {
                    partnerInserts.add(partner);
                }
            });

            partnerInserts.forEach(partner -> {
                jdbcTemplate.update(
                        "INSERT INTO ims_supplier" +
                                "(code, name, phone_number_1, address_1, transaction_quantity, transaction_amount, status, gmt_create, gmt_modified, version)" +
                                "VALUES (?,?,?,?,?,?,?,?,?,?)",
                        partner.getCode(), partner.getName(), partner.getPhone() != null ? partner.getPhone() : Utils.PHONE_NUM,
                        partner.getAddress() != null ? partner.getAddress() : partner.getName(), Math.toIntExact(partner.getTotalTransactionValue()), partner.getDebt(),
                        1, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), 0
                );
            });

            partnerUpdates.forEach(partner -> {
                ImsSupplier supplier = supplierByCode.get(partner.getCode());
                jdbcTemplate.update(
                        "UPDATE ims_supplier SET phone_number_1 = ?, address_1 = ?, transaction_quantity = ?, transaction_amount = ?, " +
                                "gmt_create = ?, gmt_modified = ?, version = version + 1 where id = ?",
                        partner.getPhone() != null ? partner.getPhone() : supplier.getPhoneNum(),
                        partner.getAddress() != null ? partner.getAddress() : supplier.getAddress(),
                        Math.toIntExact(partner.getTotalTransactionValue()), partner.getDebt(),
                        supplier.getGmtCreate(), new Timestamp(System.currentTimeMillis()), supplier.getId()
                );
            });

            // haimt add default supplier
            final List<ImsSupplier> defaultSupps = jdbcTemplate.query("select * from ims_supplier a where a.code = 'DEFAULT'", (rs, rowNum) -> {
                final ImsSupplier result = new ImsSupplier();
                result.setId(rs.getLong("id"));
                result.setGmtCreate(rs.getTimestamp("gmt_create"));
                result.setGmtModified(rs.getTimestamp("gmt_modified"));
                result.setVersion(rs.getInt("version"));
                result.setCode(rs.getString("code"));
                result.setName(rs.getString("name"));
                result.setPhoneNum(rs.getString("phone_number_1"));
                result.setPhoneNum2(rs.getString("phone_number_2"));
                result.setAddress(rs.getString("address_1"));
                result.setAddress2(rs.getString("address_2"));
                result.setTransactionQuantity(rs.getInt("transaction_quantity"));
                result.setTransactionAmount(rs.getBigDecimal("transaction_amount"));
                result.setStatus(rs.getInt("status"));
                result.setRemarks(rs.getString("remarks"));
                return result;
            });
            if (defaultSupps.isEmpty()) {
                jdbcTemplate.update(
                        "INSERT INTO ims_supplier" +
                                "(code, name, phone_number_1, address_1, transaction_quantity, transaction_amount, status, gmt_create, gmt_modified, version)" +
                                "VALUES ('DEFAULT','DEFAULT','00000000000','',0,0,1,CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP(),0)");
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
