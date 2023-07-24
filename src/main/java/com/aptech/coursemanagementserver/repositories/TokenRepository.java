package com.aptech.coursemanagementserver.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.aptech.coursemanagementserver.models.Token;

public interface TokenRepository extends JpaRepository<Token, Long> {

        @Query(value = """
                        SELECT t FROM Token t INNER JOIN User u\s
                        ON t.user.id = u.id\s
                        WHERE u.id = :id AND (t.isExpired = false OR t.isRevoked = false)\s
                        """)
        List<Token> findAllValidTokenByUser(long id);

        @Query(value = """
                        SELECT t FROM Token t INNER JOIN User u\s
                        ON t.user.id = u.id\s
                        WHERE u.id = :id\s
                        """)
        List<Token> findAllTokenByUserId(long id);

        Optional<Token> findByToken(String token);
}
