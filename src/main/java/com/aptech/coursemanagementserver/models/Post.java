package com.aptech.coursemanagementserver.models;

import java.time.Instant;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String content;
    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_post_id", nullable = false, foreignKey = @ForeignKey(name = "FK_Post_UserPost"))
    private User userPost;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "PostUsers", joinColumns = @JoinColumn(name = "post_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> likedUsers;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "post")
    private List<Comment> comments;

    private String postImageUrl;

    @CreationTimestamp
    @Builder.Default
    private Instant created_at = Instant.now();
}