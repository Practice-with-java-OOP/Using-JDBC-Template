package com.jidian.cosalon.migration.pos365.thread.impl;

import com.jidian.cosalon.migration.pos365.domainpos365.Pos365Partner;
import com.jidian.cosalon.migration.pos365.dto.BaseResponse;
import com.jidian.cosalon.migration.pos365.thread.MyThread;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
                            response.getResults().forEach(item -> jdbcTemplate.update(
                                "INSERT INTO "
                                    + " p365_partners("
                                    + " id, address, branch_id, code, company, created_by, "
                                    + " created_date, debt, gender, loyalty, modified_date, "
                                    + " name, phone, point, retailer_id, tax_code, total_debt, "
                                    + " total_transaction_value, transaction_value, type) "
                                    + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ",
                                item.getId(), item.getAddress(), item.getBranchId(), item.getCode(),
                                item.getCompany(), item.getCreatedBy(), item.getCreatedDate(),
                                item.getDebt(), item.getGender(), item.getLoyalty(),
                                item.getModifiedDate(), item.getName(), item.getPhone(),
                                item.getPoint(), item.getRetailerId(), item.getTaxCode(),
                                item.getTotalDebt(), item.getTotalTransactionValue(),
                                item.getTransactionValue(), item.getType()));
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
        );
    }
}
