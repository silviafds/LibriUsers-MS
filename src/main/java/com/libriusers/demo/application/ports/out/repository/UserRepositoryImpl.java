package com.libriusers.demo.application.ports.out.repository;

import com.libriusers.demo.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepositoryImpl extends JpaRepository<User, Long>, UserRepository {
    @Override
    UserDetails findByEmail(String username);

}
