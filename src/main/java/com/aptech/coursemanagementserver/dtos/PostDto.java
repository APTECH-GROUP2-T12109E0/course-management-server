package com.aptech.coursemanagementserver.dtos;

import java.time.Instant;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.aptech.coursemanagementserver.enums.CommentType;
import com.aptech.coursemanagementserver.enums.Role;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PostDto {
    private long id;
    private String content;

    private long userId;
    private String userName;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;

    private long typeId;
    @Enumerated(EnumType.STRING)
    private CommentType type;

    private List<UserDto> likedUsers;

    private List<CommentDto> comments;

    private String postImageUrl;

    @CreationTimestamp
    @Builder.Default
    private Instant created_at = Instant.now();
}
