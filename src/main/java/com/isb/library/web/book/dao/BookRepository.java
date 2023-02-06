package com.isb.library.web.book.dao;
import com.isb.library.web.book.entity.Book;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, String> {

}
