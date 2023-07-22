package com.aptech.coursemanagementserver.dtos;

import java.util.List;
import java.util.Map;

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
public class VideoDto {
    private long id;
    private String name;
    private String url;
    private String mobileUrl;
    private List<String> captionUrls;
    private Map<String, String> captionData;
    @Builder.Default
    private int status = 1;

    private long lessonId;
}
