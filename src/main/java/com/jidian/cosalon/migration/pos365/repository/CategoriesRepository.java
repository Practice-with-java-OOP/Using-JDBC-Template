package com.jidian.cosalon.migration.pos365.repository;

import com.jidian.cosalon.migration.pos365.domainpos365.Post365Categories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriesRepository extends JpaRepository<Post365Categories, Long> {
}
