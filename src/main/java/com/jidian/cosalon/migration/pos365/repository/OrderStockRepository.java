package com.jidian.cosalon.migration.pos365.repository;

import com.jidian.cosalon.migration.pos365.domainpos365.Post365OrderStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderStockRepository extends JpaRepository<Post365OrderStock, Long> {
}
