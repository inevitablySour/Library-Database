package com.isb.library.web.user.controller;


import com.isb.library.web.user.dao.UserRepository;
import com.isb.library.web.user.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("user")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/create")
    public ModelAndView createUserForm() {
        User user = new User();
        ModelAndView mav = new ModelAndView("create-user-form");
        mav.addObject("user", user);

        return mav;
    }

    @PostMapping("/create")
    public ModelAndView createUserSubmit(@ModelAttribute User user){
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        String password = user.getPassword();
        user.setPassword(encoder.encode(password));
        userRepository.save(user);
        ModelAndView mav = new ModelAndView("book-catalogue");
        return mav;
    }


}
