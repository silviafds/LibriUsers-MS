package com.libriusers.demo.application.ports.out.repository;

import com.libriusers.demo.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepositoryImpl extends JpaRepository<User, Long>, UserRepository {
    @Override
    UserDetails findByEmail(String username);

    @Override
    @Query(value = "SELECT users.name FROM users WHERE id = :user_id", nativeQuery = true)
    String searchUserById(@Param("user_id") Long id);
}
