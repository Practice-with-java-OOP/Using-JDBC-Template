package com.jidian.cosalon.migration.pos365.thread.imsimpl;

import com.jidian.cosalon.migration.pos365.domain.AmsAccount;
import com.jidian.cosalon.migration.pos365.domain.User;
import com.jidian.cosalon.migration.pos365.domain.UserRole;
import com.jidian.cosalon.migration.pos365.domainpos365.Pos365Partner;
import com.jidian.cosalon.migration.pos365.thread.MyThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

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

    @Override
    public void doRun() {
        try {
            AtomicLong startUserId = new AtomicLong(upmsJdbcTemplate.queryForObject("SELECT MAX(id) from cosalon_upms.upms_user", Long.class) + 1);
            // migrate from p365_partners
            String queryGetOneAccount = "select * from p365_partners where type = 1 and  ((lower(code) not like '%h0%')" +
                    " and (lower(code) not like '%f0%') and (lower(code) not like '%CO000%'))" +
                    " and phone not in(SELECT phone from cosalon_ims.p365_partners WHERE `type` = 1" +
                    " and ((lower(code) not like '%h0%') and (lower(code) not like '%f0%') and (lower(code) not like '%CO000%')) GROUP by phone HAVING count(phone) > 1)";

            String queryGetThanOneAccount = "select * from p365_partners where type = 1 and  ((lower(code) not like '%h0%')" +
                    " and (lower(code) not like '%f0%') and (lower(code) not like '%CO000%'))" +
                    " and phone in(SELECT phone from cosalon_ims.p365_partners WHERE `type` = 1" +
                    " and ((lower(code) not like '%h0%') and (lower(code) not like '%f0%') and (lower(code) not like '%CO000%')) GROUP by phone HAVING count(phone) > 1)";

            final List<Pos365Partner> partnersWithOneAccounts = getListPos365Partner(queryGetOneAccount);
            final List<Pos365Partner> partnersThanOneAccounts = getListPos365Partner(queryGetThanOneAccount);

            Map<String, List<Pos365Partner>> map = partnersThanOneAccounts.stream()
                    .collect(Collectors.groupingBy(Pos365Partner::getPhone));

            map.keySet().forEach(phone -> {
                List<Pos365Partner> list = map.get(phone);
                if (list.size() > 1) {
                    list.sort(Comparator.comparing(Pos365Partner::getCreatedDate));
                    BigDecimal totalDebt = BigDecimal.ZERO;
                    for (Pos365Partner partner : list) {
                        totalDebt = totalDebt.add(partner.getTotalDebt());
                    }
                    list.get(0).setTotalDebt(totalDebt);

                }
                partnersWithOneAccounts.add(list.get(0));
            });


            assumptionInsertFromPartner = partnersWithOneAccounts.size();


            final Map<String, Pos365Partner> pos365PartnerMap = partnersWithOneAccounts.stream().collect(
                    Collectors.toMap(Pos365Partner::getPhone, pos365Partner -> pos365Partner)
            );

            final List<User> userExists = upmsJdbcTemplate.query("select * from upms_user WHERE phone_num is not NULL",
                    (rs, rowNum) -> {
                        final User result = new User();
                        result.setId(rs.getLong("id"));
                        result.setVersion(rs.getInt("version"));
                        result.setUsername(rs.getString("username"));
                        result.setPhoneNum(rs.getString("phone_num"));
                        result.setNickName(rs.getString("nickname"));
                        return result;
                    });

            final Map<String, User> userExistMap = userExists.stream().collect(Collectors.toMap(User::getPhoneNum, user -> user));

            final List<User> userUpdates = new ArrayList<>();
            final List<User> userCreates = new ArrayList<>();

            partnersWithOneAccounts.forEach(partner -> {
                if (userExistMap.containsKey(partner.getPhone())) {
                    User user = userExistMap.get(partner.getPhone());
                    userUpdates.add(new User(user.getId(), user.getVersion() + 1, user.getPhoneNum(), user.getPhoneNum(), String.format("%s_%s", partner.getName(), partner.getCode())));
                } else {
                    userCreates.add(new User(startUserId.get(), 100, partner.getPhone(), partner.getPhone(), String.format("%s_%s", partner.getName(), partner.getCode())));
                    startUserId.getAndIncrement();
                }
            });

            System.out.println();

            final List<AmsAccount> amsAccountUpdates = new ArrayList<>();
            final List<AmsAccount> amsAccountCreates = new ArrayList<>();

            final List<AmsAccount> amsAccountExists = amsJdbcTemplate.query("select * from ams_account where account_type = 1",
                    (rs, rowNum) -> {
                        final AmsAccount result = new AmsAccount();
                        result.setId(rs.getLong("id"));
                        result.setVersion(rs.getInt("version"));
                        result.setAccountStatus(1);
                        result.setAccountType(1);
                        result.setBalance(rs.getBigDecimal("balance"));
                        result.setUserId(rs.getLong("user_id"));
                        result.setUsername(rs.getString("username"));
                        return result;
                    });

            final Map<Long, AmsAccount> amsAccountMap = amsAccountExists.stream().collect(Collectors.toMap(AmsAccount::getUserId, amsAccount -> amsAccount));

            userUpdates.forEach(user -> {
                if (amsAccountMap.containsKey(user.getId())) {
                    AmsAccount amsAccount = amsAccountMap.get(user.getId());
                    amsAccount.setBalance(pos365PartnerMap.get(user.getPhoneNum()).getTotalDebt());
                    amsAccount.setUsername(user.getUsername());
                    amsAccount.setAccountName(String.format("%s_%s",
                            pos365PartnerMap.get(user.getPhoneNum()).getName(), pos365PartnerMap.get(user.getPhoneNum()).getCode()));
                    amsAccountUpdates.add(amsAccount);
                } else {
                    amsAccountCreates.add(new AmsAccount(1L, 0,
                            String.format("%s_%s", pos365PartnerMap.get(user.getPhoneNum()).getName(), pos365PartnerMap.get(user.getPhoneNum()).getCode()), 1, 1,
                            pos365PartnerMap.get(user.getPhoneNum()).getTotalDebt(), user.getId(), user.getUsername()));
                }
            });

            userCreates.forEach(user -> {
                amsAccountCreates.add(new AmsAccount(1L, 0, pos365PartnerMap.get(user.getPhoneNum()).getName(), 1, 1,
                        pos365PartnerMap.get(user.getPhoneNum()).getTotalDebt(), user.getId(), user.getUsername()));
            });

            upmsJdbcTemplate.batchUpdate("insert into upms_user " +
                    "(id, gmt_create, gmt_modified, version, avatar, email, ext1, ext2," +
                    "ext3, is_locked, nickname, password, phone_num, is_sys_built_in," +
                    "username) values (?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?, null, null," +
                    "null, null, null, 0, ?, ?, ?, 0, ?)", new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setLong(1, userCreates.get(i).getId());
                    ps.setInt(2, userCreates.get(i).getVersion());
                    ps.setString(3, userCreates.get(i).getNickName());
                    ps.setString(4, UUID.randomUUID().toString());
                    ps.setString(5, userCreates.get(i).getPhoneNum());
                    ps.setString(6, userCreates.get(i).getUsername());
                }

                @Override
                public int getBatchSize() {
                    return userCreates.size();
                }
            });

            upmsJdbcTemplate.batchUpdate("update upms_user set " +
                    "gmt_modified = CURRENT_TIMESTAMP, version = ?, nickname = ?, phone_num = ?," +
                    "username = ? where id = ?", new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setInt(1, userUpdates.get(i).getVersion());
                    ps.setString(2, userUpdates.get(i).getNickName());
                    ps.setString(3, userUpdates.get(i).getPhoneNum());
                    ps.setString(4, userUpdates.get(i).getUsername());
                    ps.setLong(5, userUpdates.get(i).getId());
                }

                @Override
                public int getBatchSize() {
                    return userUpdates.size();
                }
            });

            amsJdbcTemplate.batchUpdate("insert into cosalon_ams.ams_account " +
                    "(gmt_create, gmt_modified, version, account_name, account_status," +
                    "account_type, balance, today_expend, today_income, total_expend," +
                    "total_income, unbalance, user_id, username) values (current_timestamp, current_timestamp, 0, ?, 1," +
                    "1, ?, 0, 0, 0, 0, 0, ?, ?)", new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setString(1, amsAccountCreates.get(i).getAccountName());
                    ps.setBigDecimal(2, amsAccountCreates.get(i).getBalance());
                    ps.setLong(3, amsAccountCreates.get(i).getUserId());
                    ps.setString(4, amsAccountCreates.get(i).getUsername());
                }

                @Override
                public int getBatchSize() {
                    return amsAccountCreates.size();
                }
            });

            amsJdbcTemplate.batchUpdate("update ams_account set " +
                    "gmt_modified = CURRENT_TIMESTAMP, version = ?, account_name= ?, balance = ?," +
                    "user_id = ?, username = ? where id = ?", new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setInt(1, amsAccountUpdates.get(i).getVersion());
                    ps.setString(2, amsAccountUpdates.get(i).getAccountName());
                    ps.setBigDecimal(3, amsAccountUpdates.get(i).getBalance());
                    ps.setLong(4, amsAccountUpdates.get(i).getUserId());
                    ps.setString(5, amsAccountUpdates.get(i).getUsername());
                    ps.setLong(6, amsAccountUpdates.get(i).getId());
                }

                @Override
                public int getBatchSize() {
                    return amsAccountUpdates.size();
                }
            });

            final List<UserRole> userRoles = upmsJdbcTemplate.query(
                    "select * from upms_user where version = 100",
                    (rs, rowNum) -> {
                        final UserRole result = new UserRole();
                        result.setUserId(rs.getLong("id"));
                        result.setRoleId(2L);
                        return result;
                    });

            upmsJdbcTemplate.batchUpdate("INSERT  " +
                    "INTO " +
                    "    upms_user_role " +
                    "    (user_id, role_id)  " +
                    "  VALUES " +
                    "    (?,?) on duplicate key update user_id = ?, role_id = ?", new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setLong(1, userRoles.get(i).getUserId());
                    ps.setLong(2, userRoles.get(i).getRoleId());
                    ps.setLong(3, userRoles.get(i).getUserId());
                    ps.setLong(4, userRoles.get(i).getRoleId());
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

    private List<Pos365Partner> getListPos365Partner(String query) {
        return jdbcTemplate
                .query(query, (rs, rowNum) -> {
                    final Pos365Partner result = new Pos365Partner();
                    result.setId(rs.getLong("id"));
                    result.setCode(rs.getString("code"));
                    result.setName(rs.getString("name"));
                    result.setPhone(rs.getString("phone"));
                    result.setDebt(rs.getBigDecimal("debt"));
                    result.setCreatedDate(rs.getString("created_date"));
                    result.setModifiedDate(rs.getString("modified_date"));
                    result.setTotalDebt(rs.getBigDecimal("total_debt").compareTo(BigDecimal.ZERO) < 0
                            ? rs.getBigDecimal("total_debt").negate() : BigDecimal.ZERO);
                    result.setTransactionValue(rs.getLong("transaction_value"));
                    result.setTotalTransactionValue(rs.getLong("total_transaction_value"));
                    return result;
                });
    }
}
