package com.aptech.coursemanagementserver.controllers;

import static com.aptech.coursemanagementserver.constants.GlobalStorage.CAPTION_API;
import static com.aptech.coursemanagementserver.constants.GlobalStorage.CAPTION_PATH;
import static com.aptech.coursemanagementserver.constants.GlobalStorage.FETCHING_FAILED;
import static com.aptech.coursemanagementserver.constants.GlobalStorage.STREAM_API;
import static com.aptech.coursemanagementserver.constants.GlobalStorage.VIDEO_PATH;

import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.io.FilenameUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.aptech.coursemanagementserver.dtos.VideoDto;
import com.aptech.coursemanagementserver.dtos.baseDto.BaseDto;
import com.aptech.coursemanagementserver.enums.AntType;
import com.aptech.coursemanagementserver.exceptions.BadRequestException;
import com.aptech.coursemanagementserver.exceptions.InvalidFileExtensionException;
import com.aptech.coursemanagementserver.exceptions.ResourceNotFoundException;
import com.aptech.coursemanagementserver.services.VideoService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
@RequestMapping("lesson/{lessonId}/video")
@Tag(name = "Video Endpoints")
@Slf4j
public class VideoController {
    private final VideoService videoService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "[ADMIN, MANAGER , EMPLOYEE] - Get Video By Lesson Id")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<VideoDto> getVideoByLessonId(
            @PathVariable("lessonId") long lessonId) {
        try {
            return new ResponseEntity<VideoDto>(videoService.findByLessonId(lessonId), HttpStatus.OK);
        } catch (NoSuchElementException e) {
            throw new ResourceNotFoundException(e.getMessage());
        } catch (Exception e) {
            throw new BadRequestException(FETCHING_FAILED);
        }
    }

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    @Operation(summary = "[ADMIN, MANAGER, EMP_COURSE] - Create Video By Lesson Id")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMP_COURSE')")
    public ResponseEntity<BaseDto> createVideoByLessonId(@PathVariable("lessonId") long lessonId,
            @RequestPart("videoFile") MultipartFile videoFile,
            @RequestPart("captionFiles") MultipartFile[] captionFiles)
            throws JsonMappingException, JsonProcessingException {
        try {
            String videoExtension = FilenameUtils.getExtension(videoFile.getOriginalFilename());

            if (!isVideoFile(videoExtension))
                throw new InvalidFileExtensionException(videoExtension);

            List<String> captionUrls = new ArrayList<>();

            for (MultipartFile captionFile : captionFiles) {
                Files.copy(captionFile.getInputStream(),
                        CAPTION_PATH.resolve(captionFile.getOriginalFilename()),
                        StandardCopyOption.REPLACE_EXISTING);
                captionUrls.add(CAPTION_API + captionFile.getOriginalFilename());
            }

            VideoDto videoDto = new VideoDto();
            videoDto.setLessonId(lessonId);
            videoDto.setName(generateFilename(Instant.now()) + videoFile.getOriginalFilename());
            videoDto.setUrl(
                    STREAM_API + videoExtension + "/" + videoDto.getName().split("_")[0] + "_"
                            + FilenameUtils.getBaseName(videoFile.getOriginalFilename()));
            videoDto.setCaptionUrls(captionUrls);

            videoService.save(videoDto, lessonId);

            Files.createDirectories(VIDEO_PATH);
            Files.createDirectories(CAPTION_PATH);

            Files.copy(videoFile.getInputStream(),
                    VIDEO_PATH.resolve(videoDto.getName()),
                    StandardCopyOption.REPLACE_EXISTING);

            return new ResponseEntity<BaseDto>(
                    BaseDto.builder().type(AntType.success).message("Create video successfully")
                            .build(),
                    HttpStatus.OK);

        } catch (InvalidFileExtensionException e) {
            throw new InvalidFileExtensionException(e.getMessage());
        } catch (NoSuchElementException e) {
            throw new ResourceNotFoundException(e.getMessage());
        } catch (Exception e) {
            log.error("Caused: " + e.getCause() + " ,Message: " + e.getMessage(), e);
            throw new BadRequestException(e.getMessage());
        }
    }

    @DeleteMapping
    @Operation(summary = "[ADMIN, MANAGER, EMP_COURSE] - Delete Video")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMP_COURSE')")
    public ResponseEntity<BaseDto> deleteVideo(long videoId) {
        try {
            return new ResponseEntity<BaseDto>(videoService.delete(videoId), HttpStatus.OK);
        } catch (NoSuchElementException e) {
            throw new ResourceNotFoundException(e.getMessage());
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    private String generateFilename(Instant instant) {
        return instant.atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyMMddHHmmss")) + "_";
    }

    private boolean isVideoFile(String videoExtension) {
        return videoExtension.equals("mp4") || videoExtension.equals("avi") || videoExtension.equals("mov")
                || videoExtension.equals("wmv") || videoExtension.equals("flv") || videoExtension.equals("mkv")
                || videoExtension.equals("webm") || videoExtension.equals("mpeg") || videoExtension.equals("mpg");
    }
}
