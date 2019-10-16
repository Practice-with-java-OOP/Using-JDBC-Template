package com.jidian.cosalon.migration.pos365.thread.imsimpl;

import com.jidian.cosalon.migration.pos365.domainpos365.Pos365Branch;
import com.jidian.cosalon.migration.pos365.thread.MyThread;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.util.List;

@Component("imsWarehouseThread")
public class ImsWarehouseThread extends MyThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImsWarehouseThread.class);

    @Override
    public String getName() {
        return "ImsWarehouseThread";
    }

    @Override
    public void doRun() {
        try {
            // select p365_branchs voi mapping voi bang ims_warehouse
            final List<BranchWarehouseQueryDto> branchs = jdbcTemplate.query("select * from p365_branchs a " +
                    "left outer join p365_branchs_ims_warehouse b on a.id = b.p365_branchs_id", (rs, rowNum) -> {
                    final BranchWarehouseQueryDto result = new BranchWarehouseQueryDto();
                    result.setId(rs.getLong("id"));
                    result.setName(rs.getString("name"));
                    result.setAddress(rs.getString("address"));
                    result.setRetailerId(rs.getLong("retailer_id"));
                    result.setCreatedDate(rs.getString("created_date"));
                    result.setCreatedBy(rs.getLong("created_by"));
                    result.setModifiedBy(rs.getLong("modified_by"));
                    result.setModifiedDate(rs.getString("modified_date"));
                    result.setOnline(rs.getBoolean("online"));
                    result.setP365BranchId(rs.getLong("p365_branchs_id"));
                    result.setImsWarehouseId(rs.getLong("ims_warehouse_id"));
                    return result;
                }
            );
            branchs.forEach(branchWarehouseQueryDto -> {
                if (branchWarehouseQueryDto.getImsWarehouseId() == null || branchWarehouseQueryDto.getImsWarehouseId() == 0L) {
                    KeyHolder keyHolder = new GeneratedKeyHolder();
                    jdbcTemplate.update(
                            connection -> {
                                PreparedStatement ps = connection.prepareStatement("INSERT INTO ims_warehouse (gmt_create, gmt_modified, version, name, address, parent_id, status) " +
                                                "VALUES (CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP(),0,?,?,0,?)",
                                        new String[] {"id"});
                                ps.setString(1, branchWarehouseQueryDto.getName());
                                ps.setString(2, branchWarehouseQueryDto.getAddress());
                                ps.setInt(3, 1/*branchWarehouseQueryDto.getOnline() != null && branchWarehouseQueryDto.getOnline().booleanValue() ? 1 : 2*/);
                                return ps;
                            },
                            keyHolder);
                    if (keyHolder.getKey() != null) {
                        try {
                            jdbcTemplate.update("INSERT INTO p365_branchs_ims_warehouse (p365_branchs_id, ims_warehouse_id) " +
                                    "VALUES (?,?) ON DUPLICATE KEY UPDATE ims_warehouse_id = ?",
                                    branchWarehouseQueryDto.getId(),
                                    keyHolder.getKey().longValue(),
                                    keyHolder.getKey().longValue());
                        } catch (DataAccessException e) {
                        }
                    }
                } else {
                    jdbcTemplate.update("UPDATE ims_warehouse SET gmt_modified = CURRENT_TIMESTAMP(), version = version + 1, name = ?, address = ?, status = ? WHERE id = ?",
                            branchWarehouseQueryDto.getName(),
                            branchWarehouseQueryDto.getAddress(),
                            1/*branchWarehouseQueryDto.getOnline() != null && branchWarehouseQueryDto.getOnline().booleanValue() ? 1 : 2*/,
                            branchWarehouseQueryDto.getImsWarehouseId());
                }
            });
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class BranchWarehouseQueryDto extends Pos365Branch {
        private Long p365BranchId;
        private Long imsWarehouseId;
    }
}
