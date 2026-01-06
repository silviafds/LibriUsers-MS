package com.libriusers.demo.application.ports.out.repository;

import org.springframework.security.core.userdetails.UserDetails;

public interface UserRepository {
    UserDetails findByEmail(String username);
}
