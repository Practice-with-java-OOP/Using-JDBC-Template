package com.jidian.cosalon.migration.pos365.thread.impl;

import com.jidian.cosalon.migration.pos365.domainpos365.Pos365User;
import com.jidian.cosalon.migration.pos365.dto.BaseResponse;
import com.jidian.cosalon.migration.pos365.thread.MyThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("userThread")
public class UserThread extends MyThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(BranchThread.class);

    @Override
    public String getName() {
        return "UserThread";
    }

    @Override
    public void doRun() {
        try {
            jdbcTemplate.execute("TRUNCATE TABLE p365_users");

            BaseResponse<Pos365User> response = pos365RetrofitService.listUsers(getMapHeaders2())
                .execute().body();
            response.getResults().forEach(item -> jdbcTemplate.update("INSERT  " +
                    "INTO " +
                    "    p365_users " +
                    "    (id, username, name, created_date, is_active, is_admin, retailer_id, created_by, admin_group)  "
                    +
                    "  VALUES " +
                    "    (?,?,?,?,?,?,?,?,?)",
                item.getId(), item.getUsername(), item.getName(), item.getCreatedDate(),
                item.getIsActive(), item.getIsAdmin(),
                item.getRetailerId(), item.getCreatedBy(), item.getAdminGroup()));
            jdbcTemplate.execute("COMMIT");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
