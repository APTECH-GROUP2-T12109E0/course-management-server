package com.aptech.coursemanagementserver.models;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "bigint")
    private long id;
    @Column(columnDefinition = "bigint")
    private long progress;
    @Column(columnDefinition = "ntext")
    private String comment;
    @Column(columnDefinition = "decimal(3,2)")
    private double rating;
    @Column(columnDefinition = "bit")
    private Boolean isNotify;
    // @Column(columnDefinition = "bigint")
    // private long user_id;
    // @Column(columnDefinition = "bigint")
    // private long course_id;

    @CreationTimestamp
    private Instant created_at = Instant.now();
    @UpdateTimestamp
    private Instant updated_at;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false, foreignKey = @ForeignKey(name = "FK_Enrollment_Course"))
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "FK_Enrollment_User"))
    private User user;
}
