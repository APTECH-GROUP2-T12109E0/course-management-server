package com.aptech.coursemanagementserver.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.aptech.coursemanagementserver.dtos.CourseInterface;
import com.aptech.coursemanagementserver.dtos.CourseTreeInterface;
import com.aptech.coursemanagementserver.models.Course;

public interface CourseRepository extends JpaRepository<Course, Long> {
        @Query(value = """
                        SELECT c.* FROM course c WHERE name like %?1%
                        AND c.status = 1
                        ORDER BY name DESC
                                                          """, nativeQuery = true)
        List<Course> findByNameLikeOrderByName(String name);

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
                        SELECT COUNT(*) FROM ENROLLMENT e
                        INNER JOIN users u on e.user_id = u.id AND u.role ='USER'
                        WHERE course_id = :courseId
                                """, nativeQuery = true)
        int findCourseTotalEnrolls(long courseId);

        @Query(value = """
                        SELECT (
                                SELECT COUNT(*) FROM ENROLLMENT e
                                INNER JOIN users u on e.user_id = u.id AND u.role ='USER'
                                WHERE course_id = c.id
                        ) AS enrollmentCount,
                        --OVER(PARTITION BY e.user_id) AS enrollmentCount,
                        c.*, cat.name AS [category_name] ,
                        au.name AS [author_name] , au.image AS [author_image],
                        e.progress , e.rating [userRating], e.id AS enrollId
                        FROM course c
                        INNER JOIN category cat
                        ON c.category_id = cat.id
                        INNER JOIN author au
                        ON c.author_id = au.id
                        INNER JOIN enrollment e
                        ON e.course_id = c.id
                        WHERE e.user_id = :userId
                                                    """, nativeQuery = true)
        List<CourseInterface> findAllCoursesByUserId(long userId);

        // @Query(value = """
        // SELECT COUNT(c.id) AS enrollmentCount , c.* , cat.name [category_name],
        // STUFF((SELECT DISTINCT ', ' + a.name
        // FROM course_achievement ca
        // LEFT JOIN achievement a ON ca.achievement_id = a.id
        // WHERE ca.course_id = c.id
        // FOR XML PATH('')),1,1,'') [achievements] ,

        // STUFF((SELECT DISTINCT ', ' + t.name
        // FROM course_tag ct
        // LEFT JOIN tag t ON ct.tag_id = t.id
        // WHERE ct.course_id = c.id
        // FOR XML PATH('')),1,1,'') [tags],

        // ISNULL(e.progress, 0) [progress],
        // --ISNULL(e.rating,0) [rating],
        // ISNULL(e.comment,'No comment') [comment]
        // FROM course c
        // LEFT JOIN enrollment e ON c.id = e.course_id
        // INNER JOIN category cat ON c.category_id = cat.id
        // LEFT JOIN users u ON e.user_id = u.id AND u.role = 'USER'
        // --WHERE u.role = 'USER'
        // GROUP BY
        // e.comment, e.progress,
        // --e.rating ,
        // cat.name, c.[id], c.[created_at], [description], [duration], c.rating ,
        // c.published_at,
        // [image], [level], c.[name], [net_price], [price], [slug], [status],
        // c.[updated_at], [category_id]
        // ORDER BY
        // --c.ordered DESC
        // c.created_at DESC
        // """, nativeQuery = true)
        // List<CourseInterface> findAllCourses();
        @Query(value = """
                        SELECT
                        COUNT(CASE WHEN(u.role = 'USER') THEN u.role END) [enrollmentCount],
                        c.* ,
                        cat.name [category_name], cat.description [category_description],
                        cat.image [category_image], cat.slug [category_slug],
                        au.name [author_name], au.image [author_image],

                        STUFF((SELECT DISTINCT ', ' + a.name
                        FROM  course_achievement ca
                        LEFT JOIN achievement a
                        ON ca.achievement_id = a.id
                        WHERE ca.course_id = c.id
                        FOR XML PATH('')),1,1,'') [achievements] ,

                        STUFF((SELECT DISTINCT ', ' + t.name
                        FROM  course_tag ct
                        LEFT JOIN tag t
                        ON ct.tag_id = t.id
                        WHERE ct.course_id = c.id
                        FOR XML PATH('')),1,1,'') [tags],

                        0 AS [progress],
                        0 AS  [userRating]


                        FROM course c
                        LEFT JOIN enrollment e
                        ON c.id = e.course_id
                        INNER JOIN category cat
                        ON c.category_id = cat.id
                        INNER JOIN author au
                        ON c.author_id = au.id
                        LEFT JOIN users u ON e.user_id = u.id AND u.role = 'USER'

                        GROUP BY
                        cat.name, cat.description, cat.image, cat.slug,
                        au.name, au.image,
                        c.[id], c.[created_at], c.[description], c.requirement,
                        [duration], c.rating , c.published_at, c.author_id,
                        c.image, [level], c.[name], [net_price], [price],
                        c.[slug], [status], c.[updated_at], c.[updated_by], [category_id]
                        ORDER BY c.created_at DESC
                                                                            """, nativeQuery = true)
        List<CourseInterface> findAllCourses();

        @Query(value = """
                        SELECT TOP 1 c.id as courseId,
                        ISNULL(s.id,0) as sectionId,
                        ISNULL(s.status,0) as sectionStatus,
                        ISNULL(l.id,0) as lessonId,
                        ISNULL(l.status,0) as lessonStatus,
                        ISNULL(v.lesson_id,0) as videoId, c.status
                        FROM course c LEFT JOIN section s on c.id = s.course_id
                        LEFT JOIN lesson l on s.id = l.section_id
                        LEFT JOIN video v on l.id = v.lesson_id
                        WHERE c.id = :courseId AND (section_id is null or lesson_id is null or v.lesson_id is null)

                                                        """, nativeQuery = true)
        CourseTreeInterface findCourseTreeByCourseId(long courseId);

        @Query(value = """
                        SELECT TOP 1 COUNT(CASE WHEN(u.role = 'USER') THEN u.role END) [enrollmentCount], c.id
                        FROM course c
                        LEFT JOIN enrollment e ON c.id = e.course_id
                        INNER JOIN category cat ON c.category_id = cat.id
                        LEFT JOIN users u ON e.user_id = u.id AND u.role = 'USER'
                        WHERE c.id = :courseId
                        GROUP BY  c.id
                                                """, nativeQuery = true)
        int findEnrollemntCountByCourseId(long courseId);

        @Modifying
        @Transactional
        @Query(value = """
                        UPDATE c
                        SET c.duration = isnull( (
                          SELECT SUM(l.duration)
                          FROM lesson l
                          INNER JOIN section s ON l.section_id = s.id
                          WHERE s.course_id = c.id
                        ),0)
                        FROM course c
                        WHERE c.id = :courseId
                                """, nativeQuery = true)
        void updateCourseDuration(long courseId);

        Course findByName(String courseName);

        Optional<Course> findBySlug(String slug);
}
