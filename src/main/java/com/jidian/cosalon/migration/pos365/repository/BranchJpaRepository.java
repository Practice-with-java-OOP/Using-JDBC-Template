package com.jidian.cosalon.migration.pos365.repository;

import com.jidian.cosalon.migration.pos365.domainpos365.Pos365Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BranchJpaRepository extends JpaRepository<Pos365Branch, Long> {
}
