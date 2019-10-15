package com.jidian.cosalon.migration.pos365.repository;

import com.jidian.cosalon.migration.pos365.domainpos365.Post365Items;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemsRepository extends JpaRepository<Post365Items, Long> {
}
