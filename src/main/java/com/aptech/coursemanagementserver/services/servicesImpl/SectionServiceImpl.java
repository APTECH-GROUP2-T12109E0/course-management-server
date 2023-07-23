package com.aptech.coursemanagementserver.services.servicesImpl;

import static com.aptech.coursemanagementserver.constants.GlobalStorage.BAD_REQUEST_EXCEPTION;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.aptech.coursemanagementserver.dtos.SectionDto;
import com.aptech.coursemanagementserver.dtos.baseDto.BaseDto;
import com.aptech.coursemanagementserver.enums.AntType;
import com.aptech.coursemanagementserver.exceptions.BadRequestException;
import com.aptech.coursemanagementserver.exceptions.IsExistedException;
import com.aptech.coursemanagementserver.models.Course;
import com.aptech.coursemanagementserver.models.Section;
import com.aptech.coursemanagementserver.models.User;
import com.aptech.coursemanagementserver.repositories.CourseRepository;
import com.aptech.coursemanagementserver.repositories.SectionRepository;
import com.aptech.coursemanagementserver.services.SectionService;
import com.aptech.coursemanagementserver.services.authServices.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SectionServiceImpl implements SectionService {
    private final CourseRepository courseRepository;
    private final SectionRepository sectionRepository;
    private final UserService userService;

    @Override
    public SectionDto findById(long sectionId) {
        try {
            Section section = sectionRepository.findById(sectionId).get();
            SectionDto sectionDto = new SectionDto();
            sectionDto.setId(sectionId);
            sectionDto.setName(section.getName());
            sectionDto.setCourseId(section.getCourse().getId());
            sectionDto.setStatus(section.getStatus());
            sectionDto.setOrdered(section.getOrdered());
            sectionDto.setUpdatedBy(section.getUpdatedBy());
            return sectionDto;
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("The section with sectionId: [" + sectionId + "] is not exist.");
        } catch (Exception e) {
            throw new BadRequestException(BAD_REQUEST_EXCEPTION);
        }

    }

    @Override
    public long findLastSectionId() {
        try {
            return sectionRepository.findLastSectionId();
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException(e.getMessage());
        } catch (Exception e) {
            throw new BadRequestException(BAD_REQUEST_EXCEPTION);
        }
    }

    @Override
    public Integer findMaxSectionOrderedByCourseId(long courseId) {
        try {
            return sectionRepository.findMaxSectionOrderedByCourseId(courseId);
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException(e.getMessage());
        } catch (Exception e) {
            throw new BadRequestException(BAD_REQUEST_EXCEPTION);
        }
    }

    @Override
    public Section findSectionByName(String sectionName) {
        return sectionRepository.findSectionByName(sectionName);
    }

    private List<Section> findAllSectionByCourseId(long courseId) {
        return sectionRepository.findAllByCourseId(courseId);
    }

    @Override
    public List<SectionDto> findAllByCourseId(long courseId) {
        Course course = courseRepository.findById(courseId).get();
        List<Section> sectionsOfCourse = course.getSections().stream()
                // Sort by Ordered then Created_at
                .sorted(Comparator.comparing(Section::getOrdered)
                        .thenComparing(Comparator.comparing(Section::getCreated_at).reversed()))
                .collect(Collectors.toList());
        List<SectionDto> sectionsDto = new ArrayList<>();

        if (userService.checkIsUser() || userService.findCurrentUser() == null) {
            sectionsOfCourse = sectionsOfCourse.stream().filter(sec -> sec.getStatus() == 1).toList();
        }

        for (Section section : sectionsOfCourse) {

            SectionDto sectionDto = new SectionDto();
            sectionDto.setCourseId(courseId);
            sectionDto.setName(section.getName());
            sectionDto.setId(section.getId());
            sectionDto.setStatus(section.getStatus());
            sectionDto.setOrdered(section.getOrdered());
            sectionDto.setUpdatedBy(section.getUpdatedBy());
            sectionsDto.add(sectionDto);

        }

        return sectionsDto;
    }

    @Override
    public List<Section> findAll() {
        return sectionRepository.findAll();
    }

    @Override
    public BaseDto saveSectionsToCourseByStringSplit(SectionDto sectionDto, long courseId) {
        try {
            List<String> list = Arrays.asList(sectionDto.getName().split(","));
            Course course = courseRepository.findById(courseId).get();

            List<Section> sectionsInCourse = findAllSectionByCourseId(courseId);
            List<String> sectionNamesInCourse = sectionsInCourse.stream().map(s -> s.getName())
                    .collect(Collectors.toList());
            List<Section> temp = new ArrayList<>();

            if (sectionsInCourse.size() > 0) {
                for (String str : list) {
                    if (sectionNamesInCourse.contains(str)) {
                        throw new IsExistedException(str);
                    }

                    Section section = new Section();
                    section.setName(str);
                    section.setCourse(course);
                    section.setStatus(sectionDto.getStatus());
                    section.setOrdered(sectionDto.getOrdered());
                    temp.add(section);
                }
            }

            sectionsInCourse.addAll(temp);

            sectionRepository.saveAll(sectionsInCourse);

            return BaseDto.builder().type(AntType.success).message("Create section successfully.").build();

        } catch (IsExistedException e) {
            throw new BadRequestException(e.getMessage());
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("The course with courseId: [" + courseId + "] is not exist.");

        } catch (Exception e) {
            throw new BadRequestException(BAD_REQUEST_EXCEPTION);
        }
    }

    @Override
    public BaseDto saveSection(SectionDto sectionDto) {
        try {
            Course course = courseRepository.findById(sectionDto.getCourseId())
                    .orElseThrow(() -> new NoSuchElementException(
                            "The course with courseId: [" + sectionDto.getCourseId() + "] is not exist."));

            Set<Section> sections = course.getSections();
            User user = userService.findCurrentUser();
            Section section = new Section();
            section.setName(sectionDto.getName())
                    .setCourse(course)
                    .setStatus(0)
                    .setUpdatedBy(user.getEmail().split("@")[0])
                    .setOrdered(sectionDto.getOrdered());

            if (sections.contains(section))
                throw new IsExistedException(section.getName());

            sectionRepository.save(section);

            return BaseDto.builder().type(AntType.success).message("Create section successfully.").build();
        } catch (IsExistedException e) {
            throw new BadRequestException(e.getMessage());
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException(e.getMessage());
        } catch (Exception e) {
            throw new BadRequestException(BAD_REQUEST_EXCEPTION);
        }

    }

    @Override
    public BaseDto updateSection(SectionDto sectionDto) {
        try {
            Section section = sectionRepository.findById(sectionDto.getId())
                    .orElseThrow(() -> new NoSuchElementException(
                            "The section with sectionId: [" + sectionDto.getId() + "] is not exist."));

            Course course = courseRepository.findById(sectionDto.getCourseId())
                    .orElseThrow(() -> new NoSuchElementException(
                            "The course with courseId: [" + sectionDto.getCourseId() + "] is not exist."));

            if (section.getStatus() == 0 && sectionDto.getStatus() == 1 && section.getLessons().size() == 0) {
                throw new BadRequestException("Cannot active section that not contains any lesson.");
            }
            User user = userService.findCurrentUser();
            section.setName(sectionDto.getName())
                    .setCourse(course)
                    .setStatus(sectionDto.getStatus())
                    .setUpdatedBy(user.getEmail().split("@")[0])
                    .setOrdered(sectionDto.getOrdered());
            sectionRepository.save(section);

            return BaseDto.builder().type(AntType.success).message("Update section successfully.").build();
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException(e.getMessage());
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            throw new BadRequestException(BAD_REQUEST_EXCEPTION);
        }
    }

    @Override
    public BaseDto delete(long sectionId) {
        try {
            Section section = sectionRepository.findById(sectionId).get();
            sectionRepository.delete(section);
            return BaseDto.builder().type(AntType.success).message("Delete section successfully.")
                    .build();
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("The section with sectionId: [" + sectionId + "] is not exist.");

        } catch (Exception e) {
            throw new BadRequestException(BAD_REQUEST_EXCEPTION);
        }
    }
}
