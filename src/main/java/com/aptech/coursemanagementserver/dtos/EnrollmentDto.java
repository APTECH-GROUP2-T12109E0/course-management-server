package com.aptech.coursemanagementserver.dtos;

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
public class EnrollmentDto {
    private long id;
    private long progress;
    private String comment;
    private double rating;
    private boolean isNotify;
    private long user_id;
    private long course_id;

}
