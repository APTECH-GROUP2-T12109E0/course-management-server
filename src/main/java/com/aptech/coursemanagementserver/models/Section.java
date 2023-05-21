package com.aptech.coursemanagementserver.models;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
public class Section {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "bigint")
    private long id;
    @Column(columnDefinition = "nvarchar(100)")
    @EqualsAndHashCode.Include
    private String name;
    // @Column(columnDefinition = "bigint")
    // private long course_id;

    @CreationTimestamp
    private Instant created_at = Instant.now();
    @UpdateTimestamp
    private Instant updated_at;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "section")
    private Set<Lesson> lessons = new HashSet<>();

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "course_id", nullable = false, foreignKey = @ForeignKey(name = "FK_Section_Course"))
    private Course course;
}
