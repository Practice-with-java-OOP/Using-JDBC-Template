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
import java.util.List;

@Component("imsCustomerSuggestionThread")
public class ImsCustomerSuggestionThread extends MyThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImsCustomerSuggestionThread.class);

    @Override
    public String getName() {
        return "ImsCustomerSuggestionThread";
    }

    private int insertedTotal = 0;
    private int assumptionTotal = 0;

    @Override
    public void doRun() {
        insertedTotal = 0;
        assumptionTotal = 0;
        try {
            // mirgate data
            final List<Pos365Partner> items = jdbcTemplate.query("select * from p365_partners a where a.type = 1 ",
                    (rs, rowNum) -> {
                        final Pos365Partner result = new Pos365Partner();
                        result.setId(rs.getLong("id"));
                        result.setCode(rs.getString("code"));
                        result.setName(rs.getString("name"));
                        result.setPhone(rs.getString("phone"));
                        return result;
                    }
            );
            assumptionTotal = items.size();
            items.forEach(item -> {
                KeyHolder keyHolder = new GeneratedKeyHolder();
                insertedTotal += jdbcTemplate.update(
                        connection -> {
                            PreparedStatement ps = connection.prepareStatement(
                                    "INSERT INTO ims_customer_suggestion (gmt_create, gmt_modified, version, customer_name, phone_number, " +
                                    "    is_system_user, is_stylist, is_non_resident_customer) " +
                                    "VALUES (CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP(),0,?,?,1,0,0) " +
                                    "ON DUPLICATE KEY UPDATE gmt_modified = CURRENT_TIMESTAMP(), version = version+1, customer_name = ?, phone_number = ?, " +
                                    "    is_system_user = 1, is_stylist = 0, is_non_resident_customer = 0 ",
                                    new String[] {"id"});
                            ps.setString(1, item.getName());
                            ps.setString(2, Utils.isBlank(Utils.normalize(item.getPhone())) ?
                                    Utils.normalize(item.getCode()) : Utils.normalize(item.getPhone()));
                            ps.setString(3, item.getName());
                            ps.setString(4, Utils.isBlank(Utils.normalize(item.getPhone())) ?
                                    Utils.normalize(item.getCode()) : Utils.normalize(item.getPhone()));
                            return ps;
                        },
                        keyHolder);
            });
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            LOGGER.info("SUMMARY: insert/update total: {}, pos365 product total: {}", insertedTotal, assumptionTotal);
        }
    }
}
