package com.jidian.cosalon.migration.pos365.thread.imsimpl;

import com.jidian.cosalon.migration.pos365.Utils;
import com.jidian.cosalon.migration.pos365.domainpos365.Pos365User;
import com.jidian.cosalon.migration.pos365.thread.MyThread;
import java.sql.PreparedStatement;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

@Component("upmsUserThread")
public class UpmsUserThread extends MyThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpmsUserThread.class);
    private int insertedTotal = 0;
    private int assumptionTotal = 0;

    @Override
    public String getName() {
        return "UpmsUserThread";
    }

    @Override
    public void doRun() {
        try {
            final List<Pos365User> users = jdbcTemplate
                .query("select * from p365_users u where u.is_admin = false and is_active = true",
                    (rs, rowNum) -> {
                        final Pos365User result = new Pos365User();
                        result.setId(rs.getLong("id"));
                        result.setUsername(rs.getString("username"));
                        result.setName(rs.getString("name"));
                        return result;
                    });
            assumptionTotal = users.size();
            users.forEach(user -> {
                KeyHolder keyHolder = new GeneratedKeyHolder();
                insertedTotal += upmsJdbcTemplate.update(
                    connection -> {
                        PreparedStatement ps = connection.prepareStatement("insert into upms_user"
                            + "(gmt_create, gmt_modified, version, avatar, email, ext1, ext2, ext3, "
                            + " is_locked, nickname, password, phone_num, is_sys_built_in, username)"
                            + " values (CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, null, null, null, "
                            + " null, null, 0, ?, null, ?, 0, ?)", new String[]{"id"});
                        int index = 1;
                        ps.setString(index++, user.getName());
                        ps.setString(index++, Utils.genP365PhoneNumber(user.getId().toString()));
                        ps.setString(index++, user.getUsername());
                        return ps;
                    },
                    keyHolder
                );
            });
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            LOGGER.info("SUMMARY: insert/update total: {}, pos365 product total: {}", insertedTotal,
                assumptionTotal);
        }
    }
}
