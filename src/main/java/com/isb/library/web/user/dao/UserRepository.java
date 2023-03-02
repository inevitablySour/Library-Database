package com.isb.library.web.user.dao;
import com.isb.library.web.user.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.servlet.http.HttpSession;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, String> {


   User findByUsername(String username);
}