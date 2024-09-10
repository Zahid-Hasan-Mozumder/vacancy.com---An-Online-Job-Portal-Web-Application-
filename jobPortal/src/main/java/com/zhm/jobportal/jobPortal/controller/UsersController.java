package com.zhm.jobportal.jobPortal.controller;

import com.zhm.jobportal.jobPortal.entity.Users;
import com.zhm.jobportal.jobPortal.entity.UsersType;
import com.zhm.jobportal.jobPortal.service.UsersService;
import com.zhm.jobportal.jobPortal.service.UsersTypeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.Optional;

@Controller
public class UsersController {

    private UsersTypeService usersTypeService;
    private UsersService usersService;

    @Autowired
    public UsersController(UsersTypeService usersTypeService, UsersService usersService){
        this.usersTypeService = usersTypeService;
        this.usersService = usersService;
    }

    @GetMapping("/register")
    public String register(Model theModel){
        List<UsersType> usersTypes = usersTypeService.getAll();
        theModel.addAttribute("getAllTypes", usersTypes);
        theModel.addAttribute("user", new Users());
        return "register";
    }

    @PostMapping("/register/new")
    public String registerNew(@Valid Users users, Model theModel){
        Optional<Users> optionalUsers = usersService.getUserByEmail(users.getEmail());
        if(optionalUsers.isPresent()){
            theModel.addAttribute("error", "Email already registered. Try to login or register using another email");
            List<UsersType> usersTypes = usersTypeService.getAll();
            theModel.addAttribute("getAllTypes", usersTypes);
            theModel.addAttribute("user", new Users());
            return "register";
        }
        usersService.addNew(users);
        return "redirect:/dashboard/";
    }

    @GetMapping("/login")
    public String login(){
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null){
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }
        return "redirect:/";
    }
}
