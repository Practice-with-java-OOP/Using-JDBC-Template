package com.jidian.cosalon.migration.pos365.thread.impl;

import com.jidian.cosalon.migration.pos365.domainpos365.Pos365User;
import com.jidian.cosalon.migration.pos365.dto.BaseResponse;
import com.jidian.cosalon.migration.pos365.thread.MyThread;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
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

                    List<Pos365User> users = response.getResults();
                    jdbcTemplate.batchUpdate("INSERT  " +
                        "INTO p365_users "
                        + " (id, username, name, created_date, is_active, is_admin, retailer_id, "
                        + " created_by, admin_group, phone) "
                        + " values  (?,?,?,?,?,?,?,?,?,?)", new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            int index = 1;
                            Pos365User pos365User = users.get(i);
                            ps.setLong(index++, pos365User.getId());
                            ps.setString(index++, pos365User.getUsername());
                            ps.setString(index++, pos365User.getName());
                            ps.setString(index++, pos365User.getCreatedDate());
                            ps.setBoolean(index++, pos365User.getIsActive());
                            ps.setBoolean(index++, pos365User.getIsAdmin());
                            ps.setLong(index++, pos365User.getRetailerId() == null ? 0
                                : pos365User.getRetailerId());
                            ps.setLong(index++,
                                pos365User.getCreatedBy() == null ? 0 : pos365User.getCreatedBy());
                            ps.setLong(index++, pos365User.getAdminGroup() == null ? 0
                                : pos365User.getAdminGroup());
                            ps.setString(index, pos365User.getPhone());
                        }

                        @Override
                        public int getBatchSize() {
                            return users.size();
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
    }
}
