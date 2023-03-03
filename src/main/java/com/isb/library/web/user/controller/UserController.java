package com.isb.library.web.user.controller;


import com.google.zxing.qrcode.decoder.Mode;
import com.isb.library.web.book.dao.BookRepository;
import com.isb.library.web.book.dao.CatalogueRepository;
import com.isb.library.web.user.dao.UserRepository;
import com.isb.library.web.user.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    /**
     * A list of all the users
     * @return Returns a ModelAndView object populated with all the users in the UserRepository
     */
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

    /**
     * Method to open the create user form and populate it with a new user object
     * @return
     */
    @GetMapping("/create")
    public ModelAndView createUserForm() {
        User user = new User();
        ModelAndView mav = new ModelAndView("create-user-form");
        mav.addObject("user", user);

        return mav;
    }

    /**
     * Post mapping for the create method that saves the user to the repository
     * @param user The user object to be saved to the repository
     * @return Returns a ModelAndView object of the user list
     */
    @PostMapping("/create")
    public ModelAndView createUserSubmit(@ModelAttribute User user){
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

        //Checks if the password field was left empty
        if (!user.getPassword().equals("")) {
            //If the password isn't empty a new password is set
            String password = user.getPassword();
            user.setPassword(encoder.encode(password));
        }
        else{
            //If it is empty the existing password is fetched from the user repository and added to the user object
            user.setPassword(userRepository.findById(user.getId()).get().getPassword());
        }

        userRepository.save(user);

        //Populates the user-list view with a list of all the users (Excluding the root user)
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

    /**
     * Method to update the information of an existing user
     * @param userId The Id of the user to be updated
     * @return Returns a ModelAndView object of the form to update the user information that is populated
     * with the user who's information is to be updated
     */
    @GetMapping("/update")
    public ModelAndView updateUserForm(@RequestParam String userId){

        User user = userRepository.findById(userId).get();

        ModelAndView mav = new ModelAndView("create-user-form");
        mav.addObject("user", user);

        return mav;
    }

    /**
     * Method to delete a user from a given ID
     * @param userId Id of the user to be deleted
     * @return Returns a ModelAndView object of a list of users
     */
    @GetMapping("/delete")
    public ModelAndView deleteUser(@RequestParam String userId){
        //Deletes the user from a given Id
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

    /**
     * Method for each user to be able to update their own information (Excluding user type)
     * @return REturns a model and view object of a form where the user can update their own information
     */
    @GetMapping("/updateUserInfo")
    public ModelAndView updateInfo(){
        ModelAndView mav = new ModelAndView("update-user-info");

        //Gets the information of the current user from Springboot secufity
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username);
        mav.addObject("user", user);
        return mav;
    }

    /**
     * Post mapping for a method for each user to be able to update their own information (Excluding user type)
     * @param user User object to be saved
     * @return Returns a ModelAndView of the catalogue page
     */
    @PostMapping("/updateUserInfo")
    public ModelAndView updateInfo(@ModelAttribute User user){
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        //Checks if the password field was left empty
        if (!user.getPassword().equals("")) {
            //If it wasn't left empty the new password is set
            String password = user.getPassword();
            user.setPassword(encoder.encode(password));
        }
        else{
            //If it is empty the existing password is fetched from the user repository and added to the user object
            user.setPassword(userRepository.findById(user.getId()).get().getPassword());
        }


        userRepository.save(user);



        ModelAndView mav = new ModelAndView("book-catalogue");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User mavUser = userRepository.findByUsername(username);
        mav.addObject("user", mavUser);
        mav.addObject("catalogue", catalogueRepository.findAll());
        return mav;
    }



}
