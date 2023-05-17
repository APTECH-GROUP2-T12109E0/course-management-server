package com.aptech.coursemanagementserver.services.servicesImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.aptech.coursemanagementserver.dtos.LessonDto;
import com.aptech.coursemanagementserver.dtos.baseDto.BaseDto;
import com.aptech.coursemanagementserver.enums.AntType;
import com.aptech.coursemanagementserver.models.Lesson;
import com.aptech.coursemanagementserver.models.Section;
import com.aptech.coursemanagementserver.models.Video;
import com.aptech.coursemanagementserver.repositories.LessonRepository;
import com.aptech.coursemanagementserver.repositories.SectionRepository;
import com.aptech.coursemanagementserver.services.LessonService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements LessonService {
    private final LessonRepository lessonRepository;
    private final SectionRepository sectionRepository;

    @Override
    public Lesson findLessonByName(String lessonName) {
        return lessonRepository.findLessonByName(lessonName);
    }

    @Override
    public List<Lesson> findAll() {
        return lessonRepository.findAll();
    }

    @Override
    public List<LessonDto> findAllBySectionId(long sectionId) {
        Section section = sectionRepository.findById(sectionId).get();
        List<LessonDto> lessonDtos = new ArrayList<>();

        for (Lesson lesson : section.getLessons()) {
            LessonDto lessonDto = LessonDto.builder().name(lesson.getName()).description(lesson.getDescription())
                    .duration(lesson.getDuration()).sectionId(sectionId).build();
            lessonDtos.add(lessonDto);
        }

        return lessonDtos;
    }

    @Override
    public BaseDto saveLessonToSection(LessonDto lessonDto, long sectionId) {
        Lesson lesson = new Lesson();
        Section section = sectionRepository.findById(sectionId).get();

        if (section == null) {
            return BaseDto.builder().type(AntType.error)
                    .message("This section with id: [" + sectionId + "]does not exist.").build();

        }

        for (Lesson l : section.getLessons()) {
            if (lessonDto.getName().contains(l.getName())) {
                return BaseDto.builder().type(AntType.error).message(lessonDto.getName() + " is already existed.")
                        .build();
            }
        }

        Video video = new Video();
        lesson.setName(lessonDto.getName()).setDescription(lessonDto.getDescription())
                .setDuration(lessonDto.getDuration()).setSection(section).setVideo(video);

        video.setLesson(lesson);

        lessonRepository.save(lesson);
        return BaseDto.builder().type(AntType.success).message("Create lesson successfully.").build();
    }

    @Override
    public BaseDto delete(long lessonId) {
        try {
            Lesson lesson = lessonRepository.findById(lessonId).get();
            lessonRepository.delete(lesson);
            return BaseDto.builder().type(AntType.success).message("Delete lesson successfully.")
                    .build();
        } catch (NoSuchElementException e) {
            return BaseDto.builder().type(AntType.error)
                    .message("This lesson with lessonId: [" + lessonId + "] is not exist.")
                    .build();
        } catch (Exception e) {
            return BaseDto.builder().type(AntType.error)
                    .message("Failed! Please check your infomation and try again.")
                    .build();
        }
    }
}
