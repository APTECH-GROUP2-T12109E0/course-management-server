package com.aptech.coursemanagementserver.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.aptech.coursemanagementserver.dtos.CourseInterface;
import com.aptech.coursemanagementserver.models.Course;

public interface CourseRepository extends JpaRepository<Course, Long> {
        @Query("""
                            SELECT c FROM Course c
                            JOIN c.tags t
                            WHERE t.name = :tagName
                        """)
        List<Course> findAllByTagName(String tagName);

        @Query(value = """
                        SELECT c.id FROM course c
                        JOIN enrollment e
                        ON e.course_id = c.id
                        GROUP BY c.id
                        ORDER BY COUNT(e.course_id) DESC
                                """, nativeQuery = true)
        List<Long> findBestSellerCourseIds();

        @Query(value = """
                        SELECT COUNT(e.id)
                        OVER(PARTITION BY e.user_id) AS enrollmentCount,
                        c.*, c.net_price AS sale_price,
                        cat.name AS categoryName, e.progress, e.rating, e.comment,
                        e.id AS enrollId
                        FROM course c
                        INNER JOIN category cat
                        ON c.category_id = cat.id
                        INNER JOIN enrollment e
                        ON e.course_id = c.id
                        WHERE e.user_id = :userId
                                                    """, nativeQuery = true)
        List<CourseInterface> findAllCoursesByUserId(long userId);

        @Query(value = """
                        SELECT count(c.id) AS enrollmentCount , c.* , cat.name [categoryName], e.progress, e.rating, e.comment
                        FROM course c
                        INNER JOIN enrollment e ON c.id = e.course_id
                        INNER JOIN category cat ON c.category_id = cat.id
                        GROUP BY e.comment, e.rating, e.progress, cat.name, c.[id], c.[created_at], [description], [duration],
                        [image], [level], c.[name], [net_price], [price], [slug], [status], c.[updated_at], [category_id]
                                """, nativeQuery = true)
        List<CourseInterface> findAllCourses();

        Course findByName(String courseName);

        Optional<Course> findBySlug(String slug);
}
