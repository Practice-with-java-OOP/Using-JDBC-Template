package com.jidian.cosalon.migration.pos365.thread.imsimpl;

import com.jidian.cosalon.migration.pos365.Utils;
import com.jidian.cosalon.migration.pos365.domain.UserRole;
import com.jidian.cosalon.migration.pos365.domainpos365.Pos365Partner;
import com.jidian.cosalon.migration.pos365.domainpos365.Pos365User;
import com.jidian.cosalon.migration.pos365.thread.MyThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component("upmsUserThread")
public class UpmsUserThread extends MyThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpmsUserThread.class);
    private int insertedTotal = 0;
    private int assumptionTotal = 0;
    private int insertedFromPartnerTotal = 0;
    private int assumptionInsertFromPartner = 0;
    private int insertedToAms = 0;

    @Override
    public String getName() {
        return "UpmsUserThread";
    }


    int counter = 0;
    List<Long> longs = new ArrayList<>();

    @Override
    public void doRun() {
        try {
            long startUserId = upmsJdbcTemplate.queryForObject("SELECT MAX(id) from cosalon_upms.upms_user", Long.class) + 1;
            // migrate from p365_users
            counter = 0;
//            final List<Pos365User> users = jdbcTemplate
//                .query("select * from p365_users u where u.is_admin = false and is_active = true",
//                    (rs, rowNum) -> {
//                        final Pos365User result = new Pos365User();
//                        result.setId(rs.getLong("id"));
//                        result.setUsername(rs.getString("username"));
//                        result.setName(rs.getString("name"));
//                        return result;
//                    });
//            assumptionTotal = users.size();
//            users.forEach(user -> {
//                KeyHolder keyHolder = new GeneratedKeyHolder();
//                insertedTotal += upmsJdbcTemplate.update(
//                    connection -> {
//                        PreparedStatement ps = connection.prepareStatement("insert into upms_user"
//                                + "(id, gmt_create, gmt_modified, version, avatar, email, ext1, ext2, ext3, "
//                                + " is_locked, nickname, password, phone_num, is_sys_built_in, username)"
//                                + " values (?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, null, null, null, "
//                                + " null, null, 0, ?, null, ?, 0, ?)"
//                                + " on duplicate key update gmt_modified = current_timestamp,"
//                                + " version = version + 1, nickname = ?,phone_num = ?, username = ?",
//                            new String[]{"id"});
//                        int index = 1;
//                        ps.setLong(index++, startUserId + counter);
//                        counter++;
//                        ps.setString(index++, user.getName());
//                        ps.setString(index++, user.getPhone() == null ? Utils
//                            .genP365PhoneNumber(user.getId().toString())
//                            : Utils.genP365PhoneNumber(user.getPhone()));
//                        ps.setString(index++, user.getUsername());
//                        ps.setString(index++, user.getName());
//                        ps.setString(index++, user.getPhone() == null ? Utils
//                            .genP365PhoneNumber(user.getId().toString())
//                            : Utils.genP365PhoneNumber(user.getPhone()));
//                        ps.setString(index, user.getUsername());
//                        return ps;
//                    },
//                    keyHolder
//                );
//            });

            // migrate from p365_partners
            final List<Pos365Partner> partners = jdbcTemplate
                    .query(
                            "select * from p365_partners "
                                    + " where type = 1 and "
                                    + " ((lower(code) not like '%h0%') and (lower(code) not like '%f0%') and (lower(code) not like '%CO000%'))",
                            (rs, rowNum) -> {
                                final Pos365Partner result = new Pos365Partner();
                                result.setId(rs.getLong("id"));
                                result.setCode(rs.getString("code"));
                                result.setName(rs.getString("name"));
                                result.setPhone(rs.getString("phone"));
                                result.setDebt(rs.getBigDecimal("debt"));
                                result.setCreatedDate(rs.getString("created_date"));
                                result.setModifiedDate(rs.getString("modified_date"));
                                result.setTotalDebt(rs.getBigDecimal("total_debt"));
                                result.setTransactionValue(rs.getLong("transaction_value"));
                                result.setTotalTransactionValue(rs.getLong("total_transaction_value"));
                                return result;
                            });
            assumptionInsertFromPartner = partners.size();

            partners.forEach(p365User -> {
                try {
                    KeyHolder keyHolder = new GeneratedKeyHolder();
                    insertedFromPartnerTotal += upmsJdbcTemplate.update(
                            connection -> {
                                PreparedStatement ps = connection
                                        .prepareStatement(" insert into upms_user  "
                                                        + " (id, gmt_create, gmt_modified, version, avatar, email, ext1, ext2, "
                                                        + " ext3, is_locked, nickname, password, phone_num, is_sys_built_in, "
                                                        + " username) values (?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 100, null, "
                                                        + " null, null, null, null, 0, ?, ?, ?, 0, ?)"
                                                        + " on duplicate key update gmt_modified = current_timestamp,"
                                                        + " version = version + 1, nickname = ?,phone_num = ?, username = ?",
                                                new String[]{"id"});
                                int index = 1;
                                ps.setLong(index++, counter + startUserId);
                                ps.setString(index++, p365User.getName());
                                ps.setString(index++, UUID.randomUUID().toString());
                                ps.setString(index++, p365User.getPhone() == null ? Utils
                                        .genP365PhoneNumber(p365User.getId().toString())
                                        : p365User.getPhone());
                                ps.setString(index++, p365User.getCode());
                                ps.setString(index++, p365User.getName());
                                ps.setString(index++, p365User.getPhone() == null ? Utils
                                        .genP365PhoneNumber(p365User.getId().toString())
                                        : p365User.getPhone());
                                ps.setString(index, p365User.getCode());
                                return ps;
                            },
                            keyHolder
                    );

                    insertedToAms += amsJdbcTemplate.update(
                            connection -> {
                                PreparedStatement ps = connection
                                        .prepareStatement("insert into cosalon_ams.ams_account "
                                                        + "(gmt_create, gmt_modified, version, account_name, account_status, "
                                                        + " account_type, balance, today_expend, today_income, total_expend, "
                                                        + " total_income, unbalance, user_id, username) "
                                                        + " values (current_timestamp, current_timestamp, 0, ?, 1,"
                                                        + " 1, ?, 0, 0, 0, 0, 0, ?, ?) "
                                                        + " on duplicate key update gmt_modified = current_timestamp, "
                                                        + " version = version + 1, balance = ?",
                                                new String[]{"id"});
                                int index = 1;
                                ps.setString(index++, p365User.getName());
                                ps.setBigDecimal(index++, p365User.getTotalDebt().compareTo(
                                        BigDecimal.valueOf(0)) < 1 ? p365User.getTotalDebt().negate()
                                        : BigDecimal.valueOf(0));
                                ps.setLong(index++, counter + startUserId);
                                ps.setString(index++, p365User.getCode());
                                ps.setBigDecimal(index, p365User.getTotalDebt().compareTo(
                                        BigDecimal.valueOf(0)) < 1 ? p365User.getTotalDebt().negate()
                                        : BigDecimal.valueOf(0));
                                return ps;
                            });
                    counter++;
                } catch (DataAccessException e) {
                    e.printStackTrace();
                }
            });

            final List<UserRole> userRoles = upmsJdbcTemplate.query(
                            "select * from upms_user where version = 100",
                            (rs, rowNum) -> {
                                final UserRole result = new UserRole();
                                result.setUserId(rs.getLong("id"));
                                result.setRoleId(1L);
                                return result;
                            });

            upmsJdbcTemplate.batchUpdate("INSERT  " +
                    "INTO " +
                    "    upms_user_role " +
                    "    (user_id, role_id)  " +
                    "  VALUES " +
                    "    (?,?)", new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setLong(1, userRoles.get(i).getUserId());
                    ps.setLong(2, userRoles.get(i).getRoleId());
                }

                @Override
                public int getBatchSize() {
                    return userRoles.size();
                }
            });
            userRoles.clear();

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            LOGGER.info("SUMMARY: insert/update total: {}, pos365 user total: {}", insertedTotal,
                    assumptionTotal);
            LOGGER.info(
                    "SUMMARY: insert/update total from p365 partner: {}, pos365 partners total: {}",
                    insertedFromPartnerTotal, assumptionInsertFromPartner);
            LOGGER.info("SUMMARY: insert/update to ams_account: {}, pos365 partners total: {}",
                    insertedToAms, assumptionInsertFromPartner);
        }
    }
}
