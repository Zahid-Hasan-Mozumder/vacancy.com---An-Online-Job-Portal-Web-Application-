package com.zhm.jobportal.jobPortal.service;

import com.zhm.jobportal.jobPortal.entity.Users;
import com.zhm.jobportal.jobPortal.repository.UsersRepository;
import com.zhm.jobportal.jobPortal.util.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService{

    private UsersRepository usersRepository;

    @Autowired
    public CustomUserDetailsService(UsersRepository usersRepository){
        this.usersRepository = usersRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users user = usersRepository.findByEmail(username).orElseThrow(()->new UsernameNotFoundException("Could not found the user"));
        return new CustomUserDetails(user);
    }
}
