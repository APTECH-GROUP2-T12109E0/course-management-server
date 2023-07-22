package com.aptech.coursemanagementserver.services.servicesImpl;

import static com.aptech.coursemanagementserver.constants.GlobalStorage.BAD_REQUEST_EXCEPTION;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.aptech.coursemanagementserver.dtos.LearningDto;
import com.aptech.coursemanagementserver.dtos.LessonDto;
import com.aptech.coursemanagementserver.dtos.LessonTrackingDto;
import com.aptech.coursemanagementserver.dtos.LessonTrackingInterface;
import com.aptech.coursemanagementserver.dtos.SectionDto;
import com.aptech.coursemanagementserver.dtos.VideoDto;
import com.aptech.coursemanagementserver.exceptions.BadRequestException;
import com.aptech.coursemanagementserver.models.Enrollment;
import com.aptech.coursemanagementserver.models.Lesson;
import com.aptech.coursemanagementserver.models.LessonTracking;
import com.aptech.coursemanagementserver.models.LessonTrackingId;
import com.aptech.coursemanagementserver.models.Video;
import com.aptech.coursemanagementserver.repositories.EnrollmentRepository;
import com.aptech.coursemanagementserver.repositories.LessonRepository;
import com.aptech.coursemanagementserver.repositories.LessonTrackingRepository;
import com.aptech.coursemanagementserver.repositories.VideoRepository;
import com.aptech.coursemanagementserver.services.LessonTrackingService;
import com.aptech.coursemanagementserver.services.SectionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LessonTrackingImpl implements LessonTrackingService {
    private final LessonTrackingRepository lessonTrackingRepository;
    private final LessonRepository lessonRepository;
    private final VideoRepository videoRepository;
    private final SectionService sectionService;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    public LessonTrackingDto loadTrack(LessonTrackingDto lessonTrackingDto) {
        try {
            LessonTracking lessonTracking;
            if (lessonTrackingDto.getLessonId() > 0) {
                lessonTracking = lessonTrackingRepository
                        .findTrackedByEnrollmentIdAndCourseIdAndLessonId(
                                lessonTrackingDto.getEnrollmentId(), lessonTrackingDto.getCourseId(),
                                lessonTrackingDto.getLessonId());
            } else {

                lessonTracking = lessonTrackingRepository
                        .findTrackedByEnrollmentIdAndCourseId(
                                lessonTrackingDto.getEnrollmentId(), lessonTrackingDto.getCourseId());
            }
            if (lessonTracking == null) {
                return new LessonTrackingDto();
            }
            LessonTrackingDto returnTrackingDto = LessonTrackingDto.builder()
                    .id(lessonTracking.getId())
                    .enrollmentId(lessonTracking.getTrackId().getEnrollment_id())
                    .courseId(lessonTracking.getTrackId().getCourse_id())
                    .sectionId(lessonTracking.getTrackId().getSection_id())
                    .lessonId(lessonTracking.getTrackId().getLession_id())
                    .videoId(lessonTracking.getTrackId().getVideo_id())
                    .isCompleted(lessonTracking.isCompleted())
                    .isTracked(lessonTracking.isTracked())
                    .resumePoint(lessonTracking.getResumePoint()).build();
            return returnTrackingDto;

        } catch (NoSuchElementException e) {
            throw new NoSuchElementException(e.getMessage());
        } catch (Exception e) {
            throw new BadRequestException(BAD_REQUEST_EXCEPTION);
        }
    }

    @Override
    public LessonTrackingDto saveTrackingLesson(LessonTrackingDto dto) {
        try {
            LessonTracking trackedLesson = lessonTrackingRepository
                    .findTrackedByEnrollmentIdAndCourseId(dto.getEnrollmentId(), dto.getCourseId());

            if (trackedLesson == null) {

            } else {
                trackedLesson.setTracked(false);
                lessonTrackingRepository.save(trackedLesson);
            }

            Optional<LessonTracking> trackingLesson = lessonTrackingRepository.findById(dto.getId());
            if (trackingLesson.isPresent()) {
                trackingLesson.get().setTracked(true);
                lessonTrackingRepository.save(trackingLesson.get());

                LessonTrackingDto lessonTrackingDto = LessonTrackingDto.builder()
                        .id(trackingLesson.get().getId())
                        .enrollmentId(trackingLesson.get().getTrackId().getEnrollment_id())
                        .courseId(trackingLesson.get().getTrackId().getCourse_id())
                        .sectionId(trackingLesson.get().getTrackId().getSection_id())
                        .lessonId(trackingLesson.get().getTrackId().getLession_id())
                        .videoId(trackingLesson.get().getTrackId().getVideo_id())
                        .isCompleted(trackingLesson.get().isCompleted())
                        .isTracked(trackingLesson.get().isTracked())
                        .resumePoint(trackingLesson.get().getResumePoint()).build();
                return lessonTrackingDto;
            }
            return new LessonTrackingDto();
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException(e.getMessage());
        } catch (Exception e) {
            throw new BadRequestException(BAD_REQUEST_EXCEPTION);
        }
    }

    @Override
    public LessonTrackingDto saveTrack(LessonTrackingDto lessonTrackingDto) {
        try {
            LessonTrackingId trackId = setTrackId(lessonTrackingDto);
            boolean isUpdated = lessonTrackingRepository.findByTrackId(trackId).isPresent();
            var tracking = addTrack(lessonTrackingDto, isUpdated);
            LessonTrackingDto dto = LessonTrackingDto.builder()
                    .id(tracking.getId())
                    .enrollmentId(tracking.getTrackId().getEnrollment_id())
                    .courseId(tracking.getTrackId().getCourse_id())
                    .sectionId(tracking.getTrackId().getSection_id())
                    .lessonId(tracking.getTrackId().getLession_id())
                    .videoId(tracking.getTrackId().getVideo_id())
                    .isCompleted(tracking.isCompleted())
                    .isTracked(tracking.isTracked())
                    .resumePoint(tracking.getResumePoint()).build();

            return dto;
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException(e.getMessage());
        } catch (Exception e) {
            throw new BadRequestException(BAD_REQUEST_EXCEPTION);
        }
    }

    @Override
    public double complete(LessonTrackingDto lessonTrackingDto) {
        try {
            LessonTrackingId trackId = setTrackId(lessonTrackingDto);
            Optional<LessonTracking> lessonTracking = lessonTrackingRepository.findByTrackId(trackId);

            if (lessonTracking.isPresent()) {
                lessonTracking.get().setCompleted(true);
                lessonTrackingRepository.save(lessonTracking.get());

            } else {
                LessonTracking newTracking = new LessonTracking();
                newTracking.setTrackId(trackId);
                newTracking.setCompleted(true);
                lessonTrackingRepository.save(newTracking);
            }
            double progress = updateProgress(lessonTrackingDto.getEnrollmentId(), lessonTrackingDto.getCourseId());
            return progress;

        } catch (NoSuchElementException e) {
            throw new NoSuchElementException(e.getMessage());
        } catch (Exception e) {
            throw new BadRequestException(BAD_REQUEST_EXCEPTION);
        }
    }

    @Override
    public double loadProgress(long enrollmentId, long courseId) {
        try {
            Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                    .orElseThrow(() -> new NoSuchElementException(
                            "The enrollment with enrollmentId:[" + enrollmentId + "] is not exist."));
            return enrollment.getProgress();
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException(e.getMessage());
        } catch (Exception e) {
            throw new BadRequestException(BAD_REQUEST_EXCEPTION);
        }
    }

    @Override
    public LearningDto getLearnDetails(long courseId) {
        try {
            LearningDto learningDto = new LearningDto();
            List<SectionDto> sectionDtos = sectionService.findAllByCourseId(courseId);
            learningDto.setSectionDto(sectionDtos);

            List<Lesson> lessons = lessonRepository.findAllByCourseId(courseId);
            for (Lesson lesson : lessons) {
                LessonDto lessonDto = LessonDto.builder()
                        .id(lesson.getId()).name(lesson.getName())
                        .description(lesson.getDescription())
                        .duration(lesson.getDuration())
                        .sectionId(lesson.getSection().getId())
                        .status(lesson.getStatus())
                        .ordered(lesson.getOrdered()).build();
                learningDto.getLessonDto().add(lessonDto);
            }

            List<Video> videos = videoRepository.findAllByCourseId(courseId);
            for (Video video : videos) {
                String[] parts = video.getCaptionUrls().split(",");
                Map<String, String> map = new HashMap<>();
                for (String part : parts) {
                    int lastDotIndex = part.lastIndexOf(".");
                    if (lastDotIndex > 0) {
                        String langCode = part.substring(lastDotIndex - 2, lastDotIndex);
                        map.put(langCode, part);
                    }
                }
                // ObjectMapper objectMapper = new ObjectMapper();
                // String captionData = objectMapper.writeValueAsString(map);

                VideoDto videoDto = VideoDto.builder().id(video.getId()).name(video.getName()).url(video.getUrl())
                        .mobileUrl(video.getMobileUrl())
                        .captionData(map).lessonId(video.getLesson().getId()).build();

                learningDto.getVideoDto().add(videoDto);
            }

            // "Cannot invoke "String.split(String)" because the return value of
            // "com.aptech.coursemanagementserver.models.Video.getCaptionUrls()" is null"
            return learningDto;
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException(e.getMessage());
        } catch (Exception e) {
            throw new BadRequestException(BAD_REQUEST_EXCEPTION);
        }
    }

    @Override
    public LearningDto getLearnDetails(long courseId, long enrollId) {
        try {
            LearningDto learningDto = new LearningDto();
            List<SectionDto> sectionDtos = sectionService.findAllByCourseId(courseId);
            learningDto.setSectionDto(sectionDtos);

            List<LessonTrackingInterface> lessons = lessonRepository.findAllByCourseIdAndEnrollId(courseId, enrollId);
            for (LessonTrackingInterface lesson : lessons) {
                LessonDto lessonDto = LessonDto.builder()
                        .id(lesson.getId()).name(lesson.getName())
                        .description(lesson.getDescription())
                        .duration(lesson.getDuration())
                        .sectionId(lesson.getSection_id())
                        .isCompleted(lesson.getIs_completed())
                        .ordered(lesson.getOrdered()).build();
                learningDto.getLessonDto().add(lessonDto);
            }

            List<Video> videos = videoRepository.findAllByCourseId(courseId);
            for (Video video : videos) {
                String[] parts = video.getCaptionUrls().split(",");
                Map<String, String> map = new HashMap<>();
                for (String part : parts) {
                    int lastDotIndex = part.lastIndexOf(".");
                    if (lastDotIndex > 0) {
                        String langCode = part.substring(lastDotIndex - 2, lastDotIndex);
                        map.put(langCode, part);
                    }
                }
                // ObjectMapper objectMapper = new ObjectMapper();
                // String captionData = objectMapper.writeValueAsString(map);

                VideoDto videoDto = VideoDto.builder().id(video.getId()).name(video.getName()).url(video.getUrl())
                        .mobileUrl(video.getMobileUrl())
                        .captionData(map).lessonId(video.getLesson().getId()).build();

                learningDto.getVideoDto().add(videoDto);
            }

            // "Cannot invoke "String.split(String)" because the return value of
            // "com.aptech.coursemanagementserver.models.Video.getCaptionUrls()" is null"
            return learningDto;
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException(e.getMessage());
        } catch (Exception e) {
            throw new BadRequestException(BAD_REQUEST_EXCEPTION);
        }
    }

    private LessonTracking addTrack(LessonTrackingDto lessonTrackingDto, boolean isUpdated) {
        LessonTrackingId trackId = setTrackId(lessonTrackingDto);

        LessonTracking lessonTracking = isUpdated ? lessonTrackingRepository.findByTrackId(trackId).orElseThrow(
                () -> new NoSuchElementException(
                        "The track with trackId:[" + trackId.toString() + "] is not exist."))
                : new LessonTracking();

        LessonTracking track = lessonTrackingRepository
                .findTrackedByEnrollmentIdAndCourseId(lessonTrackingDto.getEnrollmentId(),
                        lessonTrackingDto.getCourseId());

        if (track != null) {
            track.setTracked(false);
            lessonTrackingRepository.save(track);
        }

        if (isUpdated) {
            if (lessonTrackingDto.getResumePoint() > 0) {
                lessonTracking.setResumePoint(lessonTrackingDto.getResumePoint());
            }

            lessonTracking.setTrackId(trackId).setTracked(true);
        } else {
            lessonTracking.setTrackId(trackId).setCompleted(false).setTracked(true)
                    .setResumePoint(lessonTrackingDto.getResumePoint());
        }

        return lessonTrackingRepository.save(lessonTracking);
    }

    private LessonTrackingId setTrackId(LessonTrackingDto lessonTrackingDto) {
        LessonTrackingId trackId = new LessonTrackingId();

        trackId.setEnrollment_id(lessonTrackingDto.getEnrollmentId())
                .setCourse_id(lessonTrackingDto.getCourseId())
                .setSection_id(lessonTrackingDto.getSectionId())
                .setLession_id(lessonTrackingDto.getLessonId())
                .setVideo_id(lessonTrackingDto.getVideoId());
        return trackId;
    }

    private double updateProgress(long enrollmentId, long courseId) {
        try {
            Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                    .orElseThrow(() -> new NoSuchElementException(
                            "The enrollment with enrollmentId:[" + enrollmentId + "] is not exist."));

            List<LessonTracking> completeTracks = lessonTrackingRepository
                    .findAllCompletedByEnrollmentIdAndCourseId(enrollmentId, courseId);
            List<Lesson> lessonsInCourse = lessonRepository.findAllByCourseId(courseId);

            double progress = (Double.valueOf(completeTracks.size()) / lessonsInCourse.size()) * 100;
            enrollment.setProgress(progress);
            enrollmentRepository.save(enrollment);

            return progress;
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException(e.getMessage());
        } catch (Exception e) {
            throw new BadRequestException(BAD_REQUEST_EXCEPTION);
        }
    }

}
