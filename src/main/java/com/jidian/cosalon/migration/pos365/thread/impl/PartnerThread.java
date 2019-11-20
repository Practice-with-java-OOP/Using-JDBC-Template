package com.jidian.cosalon.migration.pos365.thread.impl;

import com.jidian.cosalon.migration.pos365.Utils;
import com.jidian.cosalon.migration.pos365.domainpos365.Pos365Partner;
import com.jidian.cosalon.migration.pos365.dto.BaseResponse;
import com.jidian.cosalon.migration.pos365.thread.MyThread;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.stereotype.Component;

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
        jdbcTemplate.execute("TRUNCATE TABLE p365_partners");
        partnerTypes.forEach(type -> {
            int count;
            int skip = 0;
            int top = 100;
            int insertedTotal = 0;
            int assumptionTotal = 0;
            try {
                do {
                    BaseResponse<Pos365Partner> response = pos365RetrofitService
                        .listPartner(getMapHeaders2(), top, skip, type, Timestamp.valueOf("2018-11-24 06:56:45"), new Timestamp(System.currentTimeMillis())).execute().body();
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
                                + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ",
                            new BatchPreparedStatementSetter() {
                                @Override
                                public void setValues(PreparedStatement ps, int i)
                                    throws SQLException {
                                    int index = 1;
                                    Pos365Partner categories = partners.get(i);
                                    ps.setLong(index++, categories.getId());
                                    ps.setString(index++, categories.getAddress());
                                    ps.setLong(index++, categories.getBranchId() == null ? 0
                                        : categories.getBranchId());
                                    ps.setString(index++, categories.getCode());
                                    ps.setString(index++, categories.getCompany());
                                    ps.setLong(index++, categories.getCreatedBy() == null ? 0
                                        : categories.getCreatedBy());
                                    ps.setString(index++, categories.getCreatedDate());
                                    ps.setBigDecimal(index++,
                                        categories.getDebt() == null ? BigDecimal.ZERO
                                            : categories.getDebt());
                                    ps.setInt(index++, categories.getGender() == null ? 0
                                        : categories.getGender());
                                    ps.setInt(index++, categories.getLoyalty() == null ? 0
                                        : categories.getLoyalty());
                                    ps.setString(index++, categories.getModifiedDate());
                                    ps.setString(index++, categories.getName());
                                    ps.setString(index++, categories.getPhone() == null ? categories.getCode() : Utils.convertPhoneNumber(categories.getPhone()));
                                    ps.setInt(index++,
                                        categories.getPoint() == null ? 0 : categories.getPoint());
                                    ps.setLong(index++, categories.getRetailerId() == null ? 0
                                        : categories.getRetailerId());
                                    ps.setString(index++, categories.getTaxCode());
                                    ps.setBigDecimal(index++,
                                        categories.getTotalDebt() == null ? BigDecimal.ZERO
                                            : categories.getTotalDebt());
                                    ps.setLong(index++,
                                        categories.getTotalTransactionValue() == null ? 0
                                            : categories.getTotalTransactionValue());
                                    ps.setLong(index++, categories.getTransactionValue() == null ? 0
                                        : categories.getTransactionValue());
                                    ps.setInt(index,
                                        categories.getType() == null ? 0 : categories.getType());
                                }

                                @Override
                                public int getBatchSize() {
                                    return partners.size();
                                }
                            });

                        insertedTotal += response.getResults().size();
                    }
                } while (count > 0);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            } finally {
                LOGGER.info("SUMMARY: insertedTotal: {}, assumptionTotal: {}", insertedTotal,
                    assumptionTotal);
            }
        });
    }

}

