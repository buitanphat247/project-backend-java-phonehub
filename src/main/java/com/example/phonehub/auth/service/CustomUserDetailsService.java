package com.example.phonehub.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.phonehub.entity.User;
import com.example.phonehub.repository.UserRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException("User Not Found with username: " + username);
        }
        
        User user = userOptional.get();
        return createUserDetails(user);
    }
    
    // Load user by ID (more reliable than username)
    public UserDetails loadUserById(Integer userId) throws UsernameNotFoundException {
        Optional<User> userOptional = userRepository.findByIdWithRole(userId);
        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException("User Not Found with id: " + userId);
        }
        
        User user = userOptional.get();
        return createUserDetails(user);
    }
    
    // Create UserDetails from User entity
    public UserDetails createUserDetails(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (user.getRole() != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName().toUpperCase()));
        }
        
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities.isEmpty() ? Collections.emptyList() : authorities);
    }
}