package com.aptech.coursemanagementserver.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.aptech.coursemanagementserver.enums.CommentType;
import com.aptech.coursemanagementserver.models.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByTypeIdAndType(long typeId, CommentType type);

    @Modifying
    @Transactional
    @Query(value = """
            DELETE FROM post_users
            WHERE post_id = :postId AND user_id = :userId
                    """, nativeQuery = true)
    void removeByPostIdAndUserId(long postId, long userId);
}
