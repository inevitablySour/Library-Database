package com.isb.library.web.user.controller;


import com.google.zxing.qrcode.decoder.Mode;
import com.isb.library.web.book.dao.BookRepository;
import com.isb.library.web.book.dao.CatalogueRepository;
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

    @Autowired
    private CatalogueRepository catalogueRepository;

    @GetMapping("/list")
    public ModelAndView userList(){
        List<User> users = userRepository.findAll();
        List<User> finalUsers = new ArrayList<>();
        for(User u : users){
            if(!u.getId().equals("1")){
                finalUsers.add(u);
            }
        }
        ModelAndView mav = new ModelAndView("user-list");

        mav.addObject("users", finalUsers);

        return mav;
    }

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

        if (user.getPassword() != null) {
            String password = user.getPassword();
            user.setPassword(encoder.encode(password));
        }

        userRepository.save(user);

        List<User> users = userRepository.findAll();
        List<User> finalUsers = new ArrayList<>();
        for(User u : users){
            if(!u.getId().equals("1")){
                finalUsers.add(u);
            }
        }
        ModelAndView mav = new ModelAndView("user-list");

        mav.addObject("users", finalUsers);

        return mav;
    }

    @GetMapping("/update")
    public ModelAndView updateUserForm(@RequestParam String userId){

        User user = userRepository.findById(userId).get();

        ModelAndView mav = new ModelAndView("create-user-form");
        mav.addObject("user", user);

        return mav;
    }

    @GetMapping("/delete")
    public ModelAndView deleteUser(@RequestParam String userId){

        userRepository.deleteById(userId);

        List<User> users = userRepository.findAll();
        List<User> finalUsers = new ArrayList<>();
        for(User u : users){
            if(!u.getId().equals("1")){
                finalUsers.add(u);
            }
        }
        ModelAndView mav = new ModelAndView("user-list");

        mav.addObject("users", finalUsers);

        return mav;
    }


}
