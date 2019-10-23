package com.jidian.cosalon.migration.pos365.thread.impl;

import com.jidian.cosalon.migration.pos365.domainpos365.Pos365Partner;
import com.jidian.cosalon.migration.pos365.domainpos365.Pos365Transfer;
import com.jidian.cosalon.migration.pos365.dto.BaseResponse;
import com.jidian.cosalon.migration.pos365.thread.MyThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Component("transferThread")
public class TransferThread extends MyThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransferThread.class);

    @Override
    public String getName() {
        return "TransferThread";
    }

    @Override
    public void doRun() {
        int count;
        int skip = 0;
        int top = 100;
        int insertedTotal = 0;
        int assumptionTotal = 0;
        try {
            jdbcTemplate.execute("TRUNCATE TABLE p365_transfers");
            do {
                BaseResponse<Pos365Transfer> response = pos365RetrofitService
                    .listTransfers(getMapHeaders2(), top, skip).execute().body();
                if (response != null) {
                    skip += top;
                    assumptionTotal = response.getCount();
                }
                count = 0;
                if (response != null && response.getResults() != null) {
                    count = response.getResults().size();

                    List<Pos365Transfer> transfers = response.getResults();
                    jdbcTemplate.batchUpdate("INSERT  " +
                            "INTO " +
                            "    p365_transfers " +
                            "    (id, code, from_branch_id, to_branch_id, status, document_date, created_date, created_by, retailer_id, modified_by, modified_date)  "
                            +
                            "  VALUES " +
                            "    (?,?,?,?,?,?,?,?,?,?,?)", new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            Pos365Transfer categories = transfers.get(i);
                            ps.setLong(1, categories.getId());
                            ps.setString(2, categories.getCode());
                            ps.setLong(3, categories.getFromBranchId() == null ? 0 : categories.getFromBranchId());
                            ps.setLong(4, categories.getToBranchId() == null ? 0 : categories.getToBranchId());
                            ps.setInt(5, categories.getStatus() == null ? 0 : categories.getStatus());
                            ps.setString(6, categories.getDocumentDate());
                            ps.setString(7, categories.getCreatedDate());
                            ps.setLong(8, categories.getCreatedBy() == null ? 0 : categories.getCreatedBy());
                            ps.setLong(9, categories.getRetailerId() == null ? 0 : categories.getRetailerId());
                            ps.setString(10, categories.getModifiedBy());
                            ps.setString(11, categories.getCreatedDate());
                        }

                        @Override
                        public int getBatchSize() {
                            return transfers.size();
                        }
                    });

//                    response.getResults().forEach(item -> jdbcTemplate.update("INSERT  " +
//                            "INTO " +
//                            "    p365_transfers " +
//                            "    (id, code, from_branch_id, to_branch_id, status, document_date, created_date, created_by, retailer_id, modified_by, modified_date)  "
//                            +
//                            "  VALUES " +
//                            "    (?,?,?,?,?,?,?,?,?,?,?)",
//                        item.getId(), item.getCode(), item.getFromBranchId(),
//                        item.getToBranchId(), item.getStatus(), item.getDocumentDate(),
//                        item.getCreatedDate(), item.getCreatedBy(), item.getRetailerId(),
//                        item.getModifiedBy(), item.getCreatedDate()));
//                    jdbcTemplate.execute("COMMIT");
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
