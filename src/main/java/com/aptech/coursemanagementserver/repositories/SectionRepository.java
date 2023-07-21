package com.aptech.coursemanagementserver.repositories;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aptech.coursemanagementserver.models.Section;

import jakarta.transaction.Transactional;

public interface SectionRepository extends JpaRepository<Section, Long> {
    Section findSectionByName(String name);

    @Query(value = """
            SELECT TOP 1 s.id FROM section s
            ORDER BY s.id DESC
            """, nativeQuery = true)
    long findLastSectionId();

    @Query(value = """
            SELECT COALESCE(MAX(s.ordered), null) FROM section s INNER JOIN course c
                ON s.course_id = c.id
                WHERE c.id = :courseId
                    """, nativeQuery = true)
    Integer findMaxSectionOrderedByCourseId(long courseId);

    @Query(value = """
            SELECT s.* FROM section s INNER JOIN course c
            ON s.course_id = c.id
            WHERE c.id = :courseId
                """, nativeQuery = true)
    List<Section> findAllByCourseId(long courseId);

    @Query(value = """
            SELECT s.* FROM section s INNER JOIN course c
            ON s.course_id = c.id
            WHERE c.id = :courseId  AND s.status = 1
                """, nativeQuery = true)
    List<Section> findAllActiveByCourseId(long courseId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM section WHERE id= :id", nativeQuery = true)
    void deleteSectionsById(long id);

    @Query("SELECT s FROM Section s JOIN s.course c WHERE c.name = :courseName")
    Set<Section> findAllByCourseName(@Param("courseName") String courseName);
}
