package com.aptech.coursemanagementserver.controllers;

import static com.aptech.coursemanagementserver.constants.GlobalStorage.BAD_REQUEST_EXCEPTION;
import static com.aptech.coursemanagementserver.constants.GlobalStorage.COURSE_PATH;
import static com.aptech.coursemanagementserver.constants.GlobalStorage.FETCHING_FAILED;
import static com.aptech.coursemanagementserver.constants.GlobalStorage.GLOBAL_EXCEPTION;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.aptech.coursemanagementserver.dtos.CourseDto;
import com.aptech.coursemanagementserver.dtos.baseDto.BaseDto;
import com.aptech.coursemanagementserver.enums.AntType;
import com.aptech.coursemanagementserver.exceptions.BadRequestException;
import com.aptech.coursemanagementserver.exceptions.InvalidFileExtensionException;
import com.aptech.coursemanagementserver.exceptions.InvalidTokenException;
import com.aptech.coursemanagementserver.exceptions.ResourceNotFoundException;
import com.aptech.coursemanagementserver.models.Course;
import com.aptech.coursemanagementserver.services.CourseService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.slugify.Slugify;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/course")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
@Tag(name = "Course Endpoints")
public class CourseController {
        private final CourseService courseService;

        @GetMapping
        @Operation(summary = "[ANORNYMOUS] - GET All Courses")
        @PreAuthorize("permitAll()")
        public ResponseEntity<List<CourseDto>> getCourses() {
                try {
                        List<CourseDto> courseDtos = courseService.findAll();
                        return ResponseEntity.ok(courseDtos);

                } catch (Exception e) {
                        throw new BadRequestException(FETCHING_FAILED);
                }
        }

        @GetMapping(path = "/free-course")
        @Operation(summary = "[ANORNYMOUS] - GET Free Courses")
        @PreAuthorize("permitAll()")
        public ResponseEntity<List<CourseDto>> getFreeCourses() {
                try {
                        List<CourseDto> courseDtos = courseService.findFreeCourses();
                        return ResponseEntity.ok(courseDtos);
                } catch (Exception e) {
                        throw new BadRequestException(FETCHING_FAILED);
                }
        }

        @GetMapping(path = "/related-course")
        @Operation(summary = "[ANORNYMOUS] - GET Related Courses")
        @PreAuthorize("permitAll()")
        public ResponseEntity<List<CourseDto>> getRelatedCourses(long categoryId, long tagId) {
                try {
                        List<CourseDto> courseDtos = courseService.findRelatedCourses(categoryId, tagId);
                        return ResponseEntity.ok(courseDtos);
                } catch (NoSuchElementException e) {
                        throw new ResourceNotFoundException(e.getMessage());
                } catch (Exception e) {
                        throw new BadRequestException(FETCHING_FAILED);
                }
        }

        @GetMapping(path = "/{id}")
        @Operation(summary = "[ANY ROLE] - GET Course By Id")
        @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MANAGER', 'EMPLOYEE')")

        public ResponseEntity<CourseDto> getCourseById(@PathVariable("id") long id) {
                try {
                        CourseDto courseDto = courseService.findById(id);
                        return ResponseEntity.ok(courseDto);
                } catch (NoSuchElementException e) {
                        throw new ResourceNotFoundException(e.getMessage());
                } catch (Exception e) {
                        throw new BadRequestException(FETCHING_FAILED);
                }

        }

        @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
        @Operation(summary = "[ADMIN, MANAGER, EMPLOYEE] - Create Course")
        public ResponseEntity<BaseDto> create(@RequestPart("courseJson") String courseJson,
                        @RequestPart("file") MultipartFile file) throws JsonMappingException, JsonProcessingException {
                ObjectMapper objectMapper = new ObjectMapper();

                try {
                        String extension = FilenameUtils.getExtension(file.getOriginalFilename());

                        if (!isImageFile(extension))
                                throw new InvalidFileExtensionException(extension);

                        CourseDto courseDto = objectMapper.readValue(courseJson, CourseDto.class);
                        courseDto.setImage(
                                        Slugify.builder().build().slugify(courseDto.getName()) + "_InDB." + extension);
                        Course savedCourse = courseService.save(courseDto);

                        Files.createDirectories(COURSE_PATH);

                        Files.copy(file.getInputStream(),
                                        COURSE_PATH.resolve(generateFilename(Instant.now(), extension, savedCourse)),
                                        StandardCopyOption.REPLACE_EXISTING);

                        return new ResponseEntity<BaseDto>(
                                        BaseDto.builder().type(AntType.success).message("Create course successfully")
                                                        .build(),
                                        HttpStatus.OK);

                } catch (InvalidFileExtensionException e) {
                        throw new InvalidFileExtensionException(e.getMessage());
                } catch (Exception e) {
                        throw new BadRequestException(BAD_REQUEST_EXCEPTION);
                }
        }

        @GetMapping(path = "/download")
        @Operation(summary = "[ANORNYMOUS] - Load Course Image")
        @PreAuthorize("permitAll()")
        public ResponseEntity<Resource> download(@RequestParam long courseId)
                        throws MalformedURLException {
                try {
                        Course course = courseService.findCourseById(courseId);
                        String fileExtension = FilenameUtils.getExtension(course.getImage());
                        // Auto add slash
                        Path root = COURSE_PATH.resolve(
                                        generateFilename(course.getUpdated_at(), fileExtension, course));

                        Resource file = new UrlResource(root.toUri());

                        return ResponseEntity.ok()
                                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                                        "attachment; filename=\"" + file.getFilename() + "\"")
                                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                        .body(file);
                } catch (NoSuchElementException e) {
                        throw new ResourceNotFoundException(e.getMessage());
                } catch (Exception e) {
                        throw new BadRequestException(GLOBAL_EXCEPTION);
                }
        }

        @PutMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
        @Operation(summary = "[ADMIN, MANAGER, EMPLOYEE] - Update Course")
        public ResponseEntity<BaseDto> updateCourse(@RequestPart("courseJson") String courseJson,
                        @RequestPart("file") MultipartFile file) {
                ObjectMapper objectMapper = new ObjectMapper();

                try {
                        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
                        if (!isImageFile(extension))
                                throw new InvalidFileExtensionException(extension);

                        CourseDto courseDto = objectMapper.readValue(courseJson, CourseDto.class);

                        courseDto.setImage(
                                        Slugify.builder().build().slugify(courseDto.getName()) + "_InDB." + extension);

                        Course course = courseService.findCourseById(courseDto.getId());
                        Course savedCourse = courseService.setProperties(courseDto, course);

                        Files.createDirectories(COURSE_PATH);

                        System.out.println(file.getOriginalFilename());

                        Files.copy(file.getInputStream(),
                                        COURSE_PATH.resolve(generateFilename(course.getUpdated_at(), extension,
                                                        savedCourse)),
                                        StandardCopyOption.REPLACE_EXISTING);

                        return new ResponseEntity<BaseDto>(
                                        BaseDto.builder().type(AntType.success).message("Create course successfully")
                                                        .build(),
                                        HttpStatus.OK);

                } catch (InvalidFileExtensionException e) {
                        throw new InvalidFileExtensionException(e.getMessage());
                } catch (NoSuchElementException e) {
                        throw new ResourceNotFoundException(e.getMessage());
                } catch (Exception e) {
                        throw new BadRequestException(BAD_REQUEST_EXCEPTION);
                }
        }

        @DeleteMapping
        @Operation(summary = "[ADMIN, MANAGER, EMPLOYEE] - Delete Course")
        public ResponseEntity<BaseDto> deleteCourse(long courseId) {
                try {
                        if (courseId == 1)
                                throw new NoSuchFileException("Cannot delete course created by SuperAdmin");
                        Course course = courseService.findCourseById(courseId);
                        String fileExtension = FilenameUtils.getExtension(course.getImage());
                        // Auto add slash
                        Path root = COURSE_PATH.resolve(
                                        generateFilename(course.getUpdated_at(), fileExtension, course));
                        Files.delete(root);

                        return new ResponseEntity<BaseDto>(courseService.delete(courseId), HttpStatus.OK);
                } catch (InvalidTokenException e) {
                        return new ResponseEntity<BaseDto>(BaseDto.builder().type(AntType.error)
                                        .message(BAD_REQUEST_EXCEPTION)
                                        .build(), HttpStatus.BAD_REQUEST);
                } catch (NoSuchElementException e) {
                        throw new ResourceNotFoundException(e.getMessage());
                } catch (Exception e) {
                        throw new BadRequestException(e.getMessage());
                }

        }

        private String generateFilename(Instant instant, String extension, Course savedCourse) {
                return instant.atZone(ZoneId.systemDefault())
                                .format(DateTimeFormatter.ofPattern("ddMMyyyy")) + "_"
                                + Slugify.builder().build().slugify(savedCourse.getName())
                                + "_" + savedCourse.getId() + "." + extension;
        }

        private boolean isImageFile(String extension) {
                return extension.equals("jpeg") || extension.equals("jpg") || extension.equals("png")
                                || extension.equals("gif") || extension.equals("bmp")
                                || extension.equals("tiff") || extension.equals("tif")
                                || extension.equals("webp") || extension.equals("svg");
        }

}
