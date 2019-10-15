package com.jidian.cosalon.migration.pos365.thread.impl;

import com.jidian.cosalon.migration.pos365.domainpos365.Pos365User;
import com.jidian.cosalon.migration.pos365.dto.BaseResponse;
import com.jidian.cosalon.migration.pos365.thread.MyThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("userThread")
public class UserThread extends MyThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserThread.class);

    @Override
    public String getName() {
        return "UserThread";
    }

    @Override
    public void doRun() {
        int count;
        int skip = 0;
        int top = 100;
        int insertedTotal = 0;
        int assumptionTotal = 0;
        try {
            jdbcTemplate.execute("TRUNCATE TABLE p365_users");
            do {
                BaseResponse<Pos365User> response = pos365RetrofitService
                    .listUsers(getMapHeaders2(), top, skip).execute().body();
                if (response != null) {
                    skip += top;
                    assumptionTotal = response.getCount();
                }
                count = 0;
                if (response != null && response.getResults() != null) {
                    count = response.getResults().size();
                    response.getResults().forEach(item -> jdbcTemplate.update("INSERT  " +
                            "INTO " +
                            "    p365_users " +
                            "    (id, username, name, created_date, is_active, is_admin, retailer_id, created_by, admin_group)  "
                            +
                            "  VALUES " +
                            "    (?,?,?,?,?,?,?,?,?)",
                        item.getId(), item.getUsername(), item.getName(),
                        item.getCreatedDate(), item.getIsActive(), item.getIsAdmin(),
                        item.getRetailerId(), item.getCreatedBy(), item.getAdminGroup()));
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
