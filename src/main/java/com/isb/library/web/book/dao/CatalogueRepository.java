package com.isb.library.web.book.dao;

import com.isb.library.web.book.entity.Catalogue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CatalogueRepository extends JpaRepository<Catalogue, String> {

}
