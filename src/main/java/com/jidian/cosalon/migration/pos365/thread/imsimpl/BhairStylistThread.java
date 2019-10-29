package com.jidian.cosalon.migration.pos365.thread.imsimpl;

import com.jidian.cosalon.migration.pos365.Utils;
import com.jidian.cosalon.migration.pos365.domainpos365.Pos365Partner;
import com.jidian.cosalon.migration.pos365.thread.MyThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;

@Component("bhairStylistThread")
public class BhairStylistThread extends MyThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(BhairStylistThread.class);
    private int insertedTotal = 0;
    private int assumptionTotal = 0;
    private long startId = 102L;
    private long startAccountId = 250L;

    @Override
    public String getName() {
        return "BhairStylistThread";
    }

    @Override
    public void doRun() {
        try {
            final List<Pos365Partner> stylists = jdbcTemplate.query("select * from p365_partners "
                    + " where lower(name) like '%h01c%' or lower(name) like '%h02s%' or lower(name) like '%h03s0%'",
                (rs, rowNum) -> {
                    final Pos365Partner result = new Pos365Partner();
                    result.setId(rs.getLong("id"));
                    result.setBranchId(rs.getLong("branch_id"));
                    result.setCode(rs.getString("code"));
                    result.setCreatedBy(rs.getLong("created_by"));
                    result.setCreatedDate(rs.getString("created_date"));
                    result.setGender(rs.getInt("gender"));
                    result.setModifiedDate(rs.getString("modified_date"));
                    result.setName(rs.getString("name"));
                    result.setPhone(rs.getString("phone"));
                    result.setPoint(rs.getInt("point"));
                    result.setTotalDebt(rs.getBigDecimal("total_debt"));
                    return result;
                });
            assumptionTotal = stylists.size();
            for (int i = 0; i < stylists.size(); i++) {
                Pos365Partner stylist = stylists.get(i);
                KeyHolder keyHolder = new GeneratedKeyHolder();
                int finalI = i;
                insertedTotal += bhairJdbcTemplate.update(
                        connection -> {
                            PreparedStatement ps = connection.prepareStatement(
                                    "insert into bhair_stylist"
                                            + " (id, gmt_create, gmt_modified, version, attitude_score, "
                                            + " available_item_ids, available_item_names, avatar, "
                                            + " comment_quantity, cumulative_order_quantity, cumulative_turnover, "
                                            + " display_name, email, experience_score, favorites_quantity, "
                                            + " introduction, level_id, level_name, monthly_order_quantity, "
                                            + " monthly_turnover, overall_score, password, phone_num, "
                                            + " rated_quantity, signature, skill_score, slogan, status, type, "
                                            + " user_name, working_years, store_id, highlight, weights)"
                                            + " values (?, ?, ?, 0, 0, '', '', null, 0, 0, 0, ?, null, 0, 0, "
                                            + " null, 0, 'p365', 0, 0, 0, null, ?, 0, null, 0, null, 9, 1,"
                                            + " ?, 0, (select s.id from bhair_store s where s.name = 'POS365 Branch'), 0, 100);",
                                    new String[]{"id"});
                            int index = 1;
                            ps.setLong(index++, startId + finalI);
                            ps.setTimestamp(index++, Utils.convertTimestamp(stylist.getCreatedDate()));
                            ps.setTimestamp(index++, new Timestamp(System.currentTimeMillis()));
                            ps.setString(index++, stylist.getName());
                            ps.setString(index++, stylist.getPhone() == null ?
                                    Utils.genP365PhoneNumber(stylist.getId().toString()) : Utils
                                    .genP365PhoneNumber(stylist.getPhone()));
                            ps.setString(index, stylist.getCode());
                            return ps;
                        },
                        keyHolder
                );

                // migrate data to ams_account
                amsJdbcTemplate.update(
                        "insert into cosalon_ams.ams_account "
                                + " (id, gmt_create, gmt_modified, version, account_name, account_status, "
                                + " account_type, balance, today_expend, today_income, total_expend, "
                                + " total_income, unbalance, user_id, username) "
                                + " values (?, ?, ?, 0, null, 1, 2, ?, 0, "
                                + " 0, 0, 0, 0, ?, ?)",
                        startAccountId + finalI,
                        Utils.convertTimestamp(stylist.getCreatedDate()),
                        stylist.getModifiedDate() == null ? new Timestamp(System.currentTimeMillis())
                                : Utils.convertTimestamp(stylist.getModifiedDate()),
                        stylist.getTotalDebt(),
                        keyHolder.getKey().longValue(),
                        stylist.getCode()
                );
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            LOGGER.info("SUMMARY: insert/update total: {}, Pos365 Stylist total: {}", insertedTotal,
                assumptionTotal);
        }
    }
}
