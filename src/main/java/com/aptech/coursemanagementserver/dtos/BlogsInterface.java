package com.aptech.coursemanagementserver.dtos;

import java.time.Instant;

public interface BlogsInterface {
    public long getId();

    public String getName();

    public String getSlug();

    public int getStatus();

    public String getDescription();

    public int getView_count();

    public String getImage();

    public long getCategory_id();

    public String getCategory_name();

    Long getUser_id();

    public String getCreatedBy();

    public Instant getCreated_at();
}
