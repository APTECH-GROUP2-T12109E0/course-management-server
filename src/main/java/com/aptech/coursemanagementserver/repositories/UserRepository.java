package com.aptech.coursemanagementserver.repositories;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.aptech.coursemanagementserver.models.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);// SELECT username FROM users WHERE email = email

    Optional<User> findById(long id);

    @Query(value = """
            SELECT u.* FROM users u
            WHERE u.role = 'USER'
                """, nativeQuery = true)
    List<User> findAllHasRoleUSER();

    @Query(value = """
                  SELECT u.* FROM users u
            WHERE u.role <> 'ADMIN'
                      """, nativeQuery = true)
    List<User> findAllExceptRoleADMIN();

    @Query(value = """
                  SELECT u.* FROM users u
            WHERE u.role <> 'USER' AND u.role <> 'ADMIN'
                      """, nativeQuery = true)
    List<User> findAllExceptRoleUSERAndRoleADMIN();

    @Query(value = """
            SELECT * FROM users u
            WHERE u.created_at BETWEEN ?1 AND ?2
                AND u.role = 'USER'
                """, nativeQuery = true)
    List<User> findByCreatedAtBetween(Instant startOfDay, Instant endOfDay);
}
