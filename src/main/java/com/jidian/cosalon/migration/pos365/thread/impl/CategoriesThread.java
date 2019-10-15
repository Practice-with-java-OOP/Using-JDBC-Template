package com.jidian.cosalon.migration.pos365.thread.impl;

import com.jidian.cosalon.migration.pos365.domainpos365.Post365Categories;
import com.jidian.cosalon.migration.pos365.dto.BaseResponse;
import com.jidian.cosalon.migration.pos365.thread.MyThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("categoriesThread")
public class CategoriesThread extends MyThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(BranchThread.class);

    @Override
    public String getName() {
        return "categoriesThread";
    }

    @Override
    public void doRun() {
        try {
            jdbcTemplate.execute("TRUNCATE TABLE p365_categories");

            BaseResponse<Post365Categories> response = pos365RetrofitService.listCategories(getMapHeaders2()).execute().body();
            LOGGER.info("Response: {}", response);

//                    branchJpaRepository.saveAll(response.getResults());
            response.getResults().forEach(item -> {
                jdbcTemplate.update("INSERT  " +
                                "INTO " +
                                "    p365_categories " +
                                "    (id, name, retailer_id, created_date, created_by)  " +
                                "  VALUES " +
                                "    (?,?,?,?,?)",
                        item.getId(), item.getName(), item.getRetailerId(), item.getCreatedDate(), item.getCreatedBy());
            });
            jdbcTemplate.execute("COMMIT");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
