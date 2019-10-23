package com.jidian.cosalon.migration.pos365.thread.impl;

import com.jidian.cosalon.migration.pos365.domainpos365.Pos365Order;
import com.jidian.cosalon.migration.pos365.dto.BaseResponse;
import com.jidian.cosalon.migration.pos365.domainpos365.Pos365Branch;
import com.jidian.cosalon.migration.pos365.thread.MyThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Component("branchThread")
public class BranchThread extends MyThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(BranchThread.class);


    @Override
    public String getName() {
        return "BranchThread";
    }

    @Override
    public void doRun() {
        try {
            jdbcTemplate.execute("TRUNCATE TABLE p365_branchs");

            BaseResponse<Pos365Branch> response = pos365RetrofitService.listBranchs(getMapHeaders2()).execute().body();
            LOGGER.info("Response: {}", response);

//                    branchJpaRepository.saveAll(response.getResults());
            List<Pos365Branch> pos365Branches = response.getResults();
            jdbcTemplate.batchUpdate("INSERT  " +
                    "INTO " +
                    "    p365_branchs " +
                    "    (id, address, created_by, created_date, modified_by, modified_date, name, online, retailer_id)  " +
                    "  VALUES " +
                    "    (?,?,?,?,?,?,?,?,?)", new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setLong(1, pos365Branches.get(i).getId());
                    ps.setString(2, pos365Branches.get(i).getAddress());
                    ps.setLong(3, pos365Branches.get(i).getCreatedBy() == null ? 0 : pos365Branches.get(i).getCreatedBy());
                    ps.setString(4, pos365Branches.get(i).getCreatedDate());
                    ps.setLong(5, pos365Branches.get(i).getModifiedBy() == null ? 0 : pos365Branches.get(i).getModifiedBy());
                    ps.setString(6, pos365Branches.get(i).getModifiedDate());
                    ps.setString(7, pos365Branches.get(i).getName());
                    ps.setBoolean(8, pos365Branches.get(i).getOnline());
                    ps.setLong(9, pos365Branches.get(i).getRetailerId() == null ? 0 : pos365Branches.get(i).getRetailerId());
                }

                @Override
                public int getBatchSize() {
                    return pos365Branches.size();
                }
            });
//            response.getResults().forEach(item -> {
//                jdbcTemplate.update("INSERT  " +
//                                "INTO " +
//                                "    p365_branchs " +
//                                "    (id, address, created_by, created_date, modified_by, modified_date, name, online, retailer_id)  " +
//                                "  VALUES " +
//                                "    (?,?,?,?,?,?,?,?,?)",
//                        item.getId(), item.getAddress(), item.getCreatedBy(), item.getCreatedDate(), item.getModifiedBy(), item.getModifiedDate(),
//                        item.getName(), item.getOnline(), item.getRetailerId());
//            });
//            jdbcTemplate.execute("COMMIT");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
