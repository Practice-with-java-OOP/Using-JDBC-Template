package com.jidian.cosalon.migration.pos365.thread.imsimpl;

import com.jidian.cosalon.migration.pos365.domain.CustomerSuggestion;
import com.jidian.cosalon.migration.pos365.thread.MyThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component("imsCustomerSuggestionThread")
public class ImsCustomerSuggestionThread extends MyThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImsCustomerSuggestionThread.class);

    @Override
    public String getName() {
        return "ImsCustomerSuggestionThread";
    }

    private int insertedTotal = 0;

    @Override
    public void doRun() {
        insertedTotal = 0;
        int assumptionTotal = 0;
        try {
            // migrate data
            final List<CustomerSuggestion> UserCustomerSuggestions = upmsJdbcTemplate
                    .query("select * from upms_user",
                            (rs, rowNum) -> {
                                final CustomerSuggestion result = new CustomerSuggestion();
                                result.setCustomerName(rs.getString("nickname") != null
                                        ? rs.getString("nickname") : rs.getString("username"));
                                result.setPhoneNumber(rs.getString("phone_num") == null
                                        ? rs.getString("username") : rs.getString("phone_num"));
                                result.setType(1);
                                result.setReferentialId(rs.getLong("id"));
                                return result;
                            }
                    );

            final List<CustomerSuggestion> StylistCustomerSuggestions = bhairJdbcTemplate
                    .query("select * from bhair_stylist",
                            (rs, rowNum) -> {
                                final CustomerSuggestion result = new CustomerSuggestion();
                                result.setCustomerName(rs.getString("display_name") != null
                                        ? rs.getString("display_name") : rs.getString("user_name"));
                                result.setPhoneNumber(rs.getString("phone_num") == null
                                        ? rs.getString("user_name") : rs.getString("phone_num"));
                                result.setType(2);
                                result.setReferentialId(rs.getLong("id"));
                                return result;
                            }
                    );

            final List<CustomerSuggestion> StaffCustomerSuggestions = bhairJdbcTemplate
                    .query("select * from bhair_nursing_staff",
                            (rs, rowNum) -> {
                                final CustomerSuggestion result = new CustomerSuggestion();
                                result.setCustomerName(rs.getString("display_name") != null
                                        ? rs.getString("display_name") : rs.getString("name"));
                                result.setPhoneNumber(rs.getString("phone_num") == null
                                        ? rs.getString("name") : rs.getString("phone_num"));
                                result.setType(4);
                                result.setReferentialId(rs.getLong("id"));
                                return result;
                            }
                    );

            final List<CustomerSuggestion> customerSuggestions
                    = Stream.of(UserCustomerSuggestions, StylistCustomerSuggestions, StaffCustomerSuggestions)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());


            jdbcTemplate.batchUpdate("INSERT INTO ims_customer_suggestion (gmt_create, gmt_modified,"
                    + " version, customer_name, phone_number, type, referential_id) "
                    + " VALUES (CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP(),0,?,?,?,?) "
                    + " ON DUPLICATE KEY UPDATE gmt_modified = CURRENT_TIMESTAMP(), "
                    + " version = version + 1, customer_name = ?, phone_number = ?, "
                    + " type = ?, referential_id = ?", new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setString(1, customerSuggestions.get(i).getCustomerName());
                    ps.setString(2, customerSuggestions.get(i).getPhoneNumber());
                    ps.setInt(3, customerSuggestions.get(i).getType());
                    ps.setLong(4, customerSuggestions.get(i).getReferentialId());
                    ps.setString(1, customerSuggestions.get(i).getCustomerName());
                    ps.setString(2, customerSuggestions.get(i).getPhoneNumber());
                    ps.setInt(3, customerSuggestions.get(i).getType());
                    ps.setLong(4, customerSuggestions.get(i).getReferentialId());
                    ps.setString(5, customerSuggestions.get(i).getCustomerName());
                    ps.setString(6, customerSuggestions.get(i).getPhoneNumber());
                    ps.setInt(7, customerSuggestions.get(i).getType());
                    ps.setLong(8, customerSuggestions.get(i).getReferentialId());
                }

                @Override
                public int getBatchSize() {
                    return customerSuggestions.size();
                }
            });

//            final List<Pos365Partner> items = jdbcTemplate
//                    .query("select * from p365_partners a where a.type = 1 ",
//                            (rs, rowNum) -> {
//                                final Pos365Partner result = new Pos365Partner();
//                                result.setId(rs.getLong("id"));
//                                result.setCode(rs.getString("code"));
//                                result.setName(rs.getString("name"));
//                                result.setPhone(rs.getString("phone"));
//                                return result;
//                            }
//                    );
//            assumptionTotal = items.size();
//            items.forEach(customer -> {
//                KeyHolder keyHolder = new GeneratedKeyHolder();
//                insertedTotal += jdbcTemplate.update(
//                    connection -> {
//                        PreparedStatement ps = connection.prepareStatement(
//                            "INSERT INTO ims_customer_suggestion (gmt_create, gmt_modified, "
//                                + " version, customer_name, phone_number, type) "
//                                + " VALUES (CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP(),0,?,?,1) "
//                                + " ON DUPLICATE KEY UPDATE gmt_modified = CURRENT_TIMESTAMP(), "
//                                + " version = version + 1, customer_name = ?, phone_number = ?, "
//                                + " type = 1",
//                            new String[]{"id"});
//                        int index = 1;
//                        ps.setString(index++, customer.getName());
//                        ps.setString(index++, Utils.isBlank(Utils.normalize(customer.getPhone())) ?
//                            Utils.normalize(customer.getCode())
//                            : Utils.normalize(customer.getPhone()));
//                        ps.setString(index++, customer.getName());
//                        ps.setString(index, Utils.isBlank(Utils.normalize(customer.getPhone())) ?
//                            Utils.normalize(customer.getCode())
//                            : Utils.normalize(customer.getPhone()));
//                        return ps;
//                    },
//                    keyHolder);
//            });
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            LOGGER.info("SUMMARY: insert/update total: {}, pos365 product total: {}", insertedTotal,
                    assumptionTotal);
        }
    }
}
