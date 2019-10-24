package com.jidian.cosalon.migration.pos365.thread.impl;

import com.jidian.cosalon.migration.pos365.domainpos365.Pos365Partner;
import com.jidian.cosalon.migration.pos365.dto.BaseResponse;
import com.jidian.cosalon.migration.pos365.thread.MyThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component("partnerThread")
public class PartnerThread extends MyThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartnerThread.class);

    @Override
    public String getName() {
        return "PartnerThread";
    }

    @Override
    public void doRun() {
        List<Integer> partnerTypes = new ArrayList<>(Arrays.asList(1, 2));
        partnerTypes.forEach(type -> {
                    int count;
                    int skip = 0;
                    int top = 100;
                    int insertedTotal = 0;
                    int assumptionTotal = 0;
                    try {
                        jdbcTemplate.execute("TRUNCATE TABLE p365_orders");
                        do {
                            BaseResponse<Pos365Partner> response = pos365RetrofitService
                                    .listPartner(getMapHeaders2(), top, skip, type).execute().body();
                            if (response != null) {
                                skip += top;
                                assumptionTotal = response.getCount();
                            }
                            count = 0;
                            if (response != null && response.getResults() != null) {
                                count = response.getResults().size();

                                List<Pos365Partner> partners = response.getResults();
                                jdbcTemplate.batchUpdate("INSERT INTO "
                                        + " p365_partners("
                                        + " id, address, branch_id, code, company, created_by, "
                                        + " created_date, debt, gender, loyalty, modified_date, "
                                        + " name, phone, point, retailer_id, tax_code, total_debt, "
                                        + " total_transaction_value, transaction_value, type) "
                                        + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ", new BatchPreparedStatementSetter() {
                                    @Override
                                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                                        Pos365Partner categories = partners.get(i);
                                        ps.setLong(1, categories.getId());
                                        ps.setString(2, categories.getAddress());
                                        ps.setLong(3, categories.getBranchId() == null ? 0 : categories.getBranchId());
                                        ps.setString(4, categories.getCode());
                                        ps.setString(5, categories.getCompany());
                                        ps.setLong(6, categories.getCreatedBy() == null ? 0 : categories.getCreatedBy());
                                        ps.setString(7, categories.getCreatedDate());
                                        ps.setBigDecimal(8, categories.getDebt() == null ? BigDecimal.ZERO : categories.getDebt());
                                        ps.setInt(9, categories.getGender() == null ? 0 : categories.getGender());
                                        ps.setInt(10, categories.getLoyalty() == null ? 0 : categories.getLoyalty());
                                        ps.setString(11, categories.getModifiedDate());
                                        ps.setString(12, categories.getName());
                                        ps.setString(13, categories.getPhone());
                                        ps.setInt(14, categories.getPoint() == null ? 0 : categories.getPoint());
                                        ps.setLong(15, categories.getRetailerId() == null ? 0 : categories.getRetailerId());
                                        ps.setString(16, categories.getTaxCode());
                                        ps.setBigDecimal(17, categories.getTotalDebt() == null ? BigDecimal.ZERO : categories.getTotalDebt());
                                        ps.setLong(18, categories.getTotalTransactionValue() == null ? 0 : categories.getTotalTransactionValue());
                                        ps.setLong(19, categories.getTransactionValue() == null ? 0 : categories.getTransactionValue());
                                        ps.setInt(20, categories.getType() == null ? 0 : categories.getType());
                                    }

                                    @Override
                                    public int getBatchSize() {
                                        return partners.size();
                                    }
                                });

//                            response.getResults().forEach(item -> jdbcTemplate.update(
//                                "INSERT INTO "
//                                    + " p365_partners("
//                                    + " id, address, branch_id, code, company, created_by, "
//                                    + " created_date, debt, gender, loyalty, modified_date, "
//                                    + " name, phone, point, retailer_id, tax_code, total_debt, "
//                                    + " total_transaction_value, transaction_value, type) "
//                                    + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ",
//                                item.getId(), item.getAddress(), item.getBranchId(), item.getCode(),
//                                item.getCompany(), item.getCreatedBy(), item.getCreatedDate(),
//                                item.getDebt(), item.getGender(), item.getLoyalty(),
//                                item.getModifiedDate(), item.getName(), item.getPhone(),
//                                item.getPoint(), item.getRetailerId(), item.getTaxCode(),
//                                item.getTotalDebt(), item.getTotalTransactionValue(),
//                                item.getTransactionValue(), item.getType()));
//                            jdbcTemplate.execute("COMMIT");
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
        );
    }
}
