package com.jidian.cosalon.migration.pos365.thread.impl;

import com.jidian.cosalon.migration.pos365.dto.BaseResponse;
import com.jidian.cosalon.migration.pos365.domainpos365.Pos365Branch;
import com.jidian.cosalon.migration.pos365.thread.MyThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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
            response.getResults().forEach(item -> {
                jdbcTemplate.update("INSERT  " +
                                "INTO " +
                                "    p365_branchs " +
                                "    (id, address, created_by, created_date, modified_by, modified_date, name, online, retailer_id)  " +
                                "  VALUES " +
                                "    (?,?,?,?,?,?,?,?,?)",
                        item.getId(), item.getAddress(), item.getCreatedBy(), item.getCreatedDate(), item.getModifiedBy(), item.getModifiedDate(),
                        item.getName(), item.getOnline(), item.getRetailerId());
            });
            jdbcTemplate.execute("COMMIT");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
