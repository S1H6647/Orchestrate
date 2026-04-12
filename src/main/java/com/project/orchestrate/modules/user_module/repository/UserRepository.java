package com.project.orchestrate.modules.user_module.repository;

import com.project.orchestrate.modules.user_module.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByRefreshToken(String hashedToken);

    boolean existsByEmail(String email);
}
