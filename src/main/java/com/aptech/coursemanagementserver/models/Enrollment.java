package com.aptech.coursemanagementserver.models;

import java.time.Instant;
import java.time.LocalTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@EqualsAndHashCode(callSuper = false)
@Entity
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "bigint")
    private long id;
    @Column(columnDefinition = "bigint")
    private long progress;
    @Column(columnDefinition = "datetime")
    private LocalTime resume_point;
    @Column(columnDefinition = "text")
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
    private Instant created_at;
    @UpdateTimestamp
    private Instant updated_at;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
