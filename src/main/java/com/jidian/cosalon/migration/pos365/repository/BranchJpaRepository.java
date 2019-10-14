package com.jidian.cosalon.migration.pos365.repository;

import com.jidian.cosalon.migration.pos365.dto.BranchResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BranchJpaRepository extends JpaRepository<BranchResponse, Long> {
}
