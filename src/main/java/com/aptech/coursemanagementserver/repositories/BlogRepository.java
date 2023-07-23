package com.aptech.coursemanagementserver.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.aptech.coursemanagementserver.dtos.BlogsInterface;
import com.aptech.coursemanagementserver.enums.BlogStatus;
import com.aptech.coursemanagementserver.models.Blog;

public interface BlogRepository extends JpaRepository<Blog, Long> {
    Blog findBlogByName(String name);

    @Query(value = """
            SELECT b.* FROM blog b WHERE name like %?1%
            AND b.status = 1
            ORDER BY name DESC
                                              """, nativeQuery = true)
    List<Blog> findByNameLikeOrderByName(String name);

    @Query(value = """
            SELECT
                b.*, cat.name [category_name]
            FROM blog b
                INNER JOIN category cat ON b.category_id = cat.id
            WHERE b.user_id = :userId
            ORDER BY
                b.created_at DESC
                                              """, nativeQuery = true)
    List<Blog> findByUserId(long userId);

    List<Blog> findByStatus(BlogStatus status);

    @Query(value = """
            SELECT
                b.*,
                cat.name [category_name],
                CONCAT(u.first_name, ' ', u.last_name) AS createdBy
            FROM blog b
                INNER JOIN category cat ON b.category_id = cat.id
                LEFT JOIN users u ON b.user_id = u.id
            ORDER BY
                b.created_at DESC
            """, nativeQuery = true)
    List<BlogsInterface> findAllBlogs();

    Optional<Blog> findBySlug(String slug);
}
