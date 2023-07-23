package com.aptech.coursemanagementserver.services;

import java.util.List;

import com.aptech.coursemanagementserver.dtos.SectionDto;
import com.aptech.coursemanagementserver.dtos.baseDto.BaseDto;
import com.aptech.coursemanagementserver.models.Section;

public interface SectionService {
    public SectionDto findById(long sectionId);

    public long findLastSectionId();

    public Integer findMaxSectionOrderedByCourseId(long courseId);

    public Section findSectionByName(String sectionName);

    public List<SectionDto> findAllByCourseId(long courseId);

    // public List<Section> findAllByCourseId(long courseId);

    public List<Section> findAll();

    public BaseDto saveSectionsToCourseByStringSplit(SectionDto sectionDto, long courseId);

    public BaseDto saveSection(SectionDto sectionDto);

    public BaseDto updateSection(SectionDto sectionDto);

    public BaseDto delete(long sectionId);
}
