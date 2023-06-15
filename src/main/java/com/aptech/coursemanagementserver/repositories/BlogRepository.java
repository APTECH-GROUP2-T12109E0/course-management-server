package com.aptech.coursemanagementserver.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.aptech.coursemanagementserver.dtos.BlogsInterface;
import com.aptech.coursemanagementserver.enums.BlogStatus;
import com.aptech.coursemanagementserver.models.Blog;

public interface BlogRepository extends JpaRepository<Blog, Long> {
    Blog findBlogByName(String name);

    List<Blog> findByUser_Id(long userId);

    List<Blog> findByStatus(BlogStatus status);

    @Query(value = """
            SELECT
            b.* , cat.name [category_name]
            FROM blog b
            INNER JOIN category cat ON b.category_id = cat.id
            LEFT JOIN users u ON e.user_id = u.id AND u.role = 'USER'
            GROUP BY
            cat.name, b.*
            ORDER BY
            b.created_at DESC
                                              """, nativeQuery = true)
    List<BlogsInterface> findAllBlogs();
}
