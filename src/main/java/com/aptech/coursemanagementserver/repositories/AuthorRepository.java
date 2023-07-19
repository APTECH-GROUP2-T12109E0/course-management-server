package com.aptech.coursemanagementserver.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.aptech.coursemanagementserver.dtos.AuthorInterface;
import com.aptech.coursemanagementserver.models.Author;

public interface AuthorRepository extends JpaRepository<Author, Long>, JpaSpecificationExecutor<Author> {
        @Query(value = """
                        SELECT a.* FROM author a WHERE name like %?1%
                        ORDER BY name DESC
                                                          """, nativeQuery = true)
        List<Author> findByNameLikeOrderByName(String name);

        @Query(value = """
                        SELECT * FROM author ORDER BY created_at DESC
                        """, nativeQuery = true)
        List<Author> findAll();

        @Query(value = """
                        SELECT TOP 3 ISNULL(a1.enrollmentCount, 0) AS enrollmentCount, a.id, a.image,
                        a.information, a.name, a.title
                        FROM     author AS a LEFT OUTER JOIN
                        (SELECT COUNT(*) AS enrollmentCount, c.author_id AS AuthorId
                        FROM      author AS a LEFT OUTER JOIN
                        course AS c ON a.id = c.author_id INNER JOIN
                        enrollment AS e ON c.id = e.course_id
                        GROUP BY c.author_id) AS a1 ON a.id = a1.AuthorId
                        ORDER BY enrollmentCount DESC
                                            """, nativeQuery = true)
        List<AuthorInterface> findTop3();

        @Query(value = """
                        SELECT a.* FROM author a
                                                """, nativeQuery = true)
        Page<Author> findAllPagination(Pageable pageable);

        @Query(value = """
                        SELECT a.* FROM author a
                        INNER JOIN course c ON a.id = c.author_id
                        WHERE c.category_id = :categoryId
                        GROUP BY a.[id], a.[created_at], a.[image], a.[information], a.[name], a.[title], a.[updated_at]
                                                """, nativeQuery = true)
        Page<Author> findAllPaginationFilter(long categoryId, Pageable pageable);

}