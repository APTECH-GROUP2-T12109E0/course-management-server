package com.aptech.coursemanagementserver.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.aptech.coursemanagementserver.dtos.LessonTrackingInterface;
import com.aptech.coursemanagementserver.models.Lesson;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    Lesson findLessonByName(String name);

    @Query(value = """
            SELECT TOP 1 l.id FROM lesson l
            ORDER BY l.id DESC
            """, nativeQuery = true)
    long findLastLessonId();

    @Query(value = """
            SELECT COALESCE(MAX(l.ordered), null) FROM lesson l INNER JOIN section s
                ON l.section_id = s.id
                WHERE s.id = :sectionId
                    """, nativeQuery = true)
    Integer findMaxLessonOrderedBySectionId(long sectionId);

    @Query(value = """
            SELECT l FROM Lesson l INNER JOIN Section s\s
            ON l.section.id = s.id\s
            WHERE s.id = :sectionId
            """)
    List<Lesson> findAllBySectionId(long sectionId);

    @Query(value = """
            SELECT l.id, l.created_at, l.description, l.duration, l.name, l.ordered,
            l.status, l.updated_at, l.section_id, l.updated_by
            FROM lesson l
            JOIN section s
            ON l.section_id = s.id
            JOIN course c
            ON s.course_id = c.id
            WHERE c.id = :courseId
            ORDER BY l.section_id, l.ordered
                """, nativeQuery = true)
    List<Lesson> findAllByCourseId(long courseId);

    @Query(value = """
            SELECT l.id, l.created_at, l.description, l.duration, l.name, l.ordered,
            l.status, l.updated_at,l.section_id, isnull(t.is_completed,0) is_completed
            FROM lesson l
            JOIN section s
            ON l.section_id = s.id
            JOIN course c
            ON s.course_id = c.id
            LEFT JOIN lesson_tracking t on l.id = t.lesson_id
            AND s.id = t.section_id and t.enrollment_id = :enrollId
            WHERE c.id = :courseId
            ORDER BY l.section_id, l.ordered
                                        """, nativeQuery = true)
    List<LessonTrackingInterface> findAllByCourseIdAndEnrollId(long courseId, long enrollId);
}
