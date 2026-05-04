package com.project.orchestrate.modules.user_module.repository;

import com.project.orchestrate.modules.user_module.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    Optional<User> findByRefreshToken(String hashedToken);

    boolean existsByEmail(String email);

    Optional<User> findByVerificationToken(String token);

    @Query("""
                SELECT u
                FROM User u
                WHERE (
                    :q IS NULL
                    OR LOWER(u.name) LIKE LOWER(CONCAT('%', :q, '%'))
                    OR LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%'))
                )
            """)
    Page<User> searchUsers(
            @Param("q") String q,
            Pageable pageable
    );
}
