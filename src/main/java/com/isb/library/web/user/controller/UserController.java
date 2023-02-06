package com.isb.library.web.user.controller;


import com.isb.library.web.user.dao.UserRepository;
import com.isb.library.web.user.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("user")
public class UserController {
    @Autowired
    private UserRepository eRepo;
    @GetMapping("/")
    public ModelAndView loginForm(){
        ModelAndView mav = new ModelAndView("login-form");
        return mav;
    }

    @GetMapping("/startSession")
    public String startSession(@RequestParam String password, @RequestParam String username, HttpServletRequest request){
        User user = new User();
        user.setPassword(password);
        user.setUsername(username);
        List<User> users = eRepo.findAll();
        for(int i = 0; i < users.size(); i++){
            if(users.get(i).getUsername().equals(user.getUsername()) && users.get(i).getPassword().equals(user.getPassword())){
                user = users.get(i);
                HttpSession session = request.getSession();
                String createTime = String.valueOf(session.getCreationTime());
                user.setCreateTime(createTime);
                eRepo.save(user);
                session.setMaxInactiveInterval(900);

                if(user.getType().equals("root")){
                    session.setAttribute(user.getName()+":"+user.getId(), user);
                    return "Login Successful : " + user.getUsername()+":"+user.getId();
                }
                session.setAttribute(user.getName()+user.getEmail()+":"+user.getId(), user);
                return "Login Successful : " + user.getName()+":"+user.getId();
            }
        }

            return "Login Information is incorrect/doesn't match" + user;


    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, String token) {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute(token);
        session.removeAttribute(token);
        return user.getUsername();
    }
    @GetMapping("/userinfo")
    public String userInfo(HttpServletRequest request, String token){
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute(token);
        if(user != null){
            return user.toString();
        }else{
            return "User not found";
        }
    }

}
