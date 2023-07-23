package com.aptech.coursemanagementserver.services;

import java.util.List;

import com.aptech.coursemanagementserver.dtos.LessonDto;
import com.aptech.coursemanagementserver.dtos.baseDto.BaseDto;
import com.aptech.coursemanagementserver.models.Lesson;

public interface LessonService {
    public Lesson findLessonByName(String lessonName);

    public LessonDto findById(long lessonId);

    public long findLastLessonId();

    public Integer findMaxLessonOrderedBySectionId(long sectionId);

    public List<Lesson> findAll();

    public List<LessonDto> findAllBySectionId(long sectionId);

    public BaseDto save(LessonDto lessonDto);

    public BaseDto updateLesson(LessonDto lessonDto);

    public BaseDto delete(long lessonId);

}
