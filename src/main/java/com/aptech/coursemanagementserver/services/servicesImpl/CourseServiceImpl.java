package com.aptech.coursemanagementserver.services.servicesImpl;

import static com.aptech.coursemanagementserver.constants.GlobalStorage.BAD_REQUEST_EXCEPTION;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.aptech.coursemanagementserver.dtos.CourseDto;
import com.aptech.coursemanagementserver.dtos.CourseInterface;
import com.aptech.coursemanagementserver.dtos.CourseRelatedDto;
import com.aptech.coursemanagementserver.dtos.CourseTreeInterface;
import com.aptech.coursemanagementserver.dtos.baseDto.BaseDto;
import com.aptech.coursemanagementserver.enums.AntType;
import com.aptech.coursemanagementserver.exceptions.BadRequestException;
import com.aptech.coursemanagementserver.mappers.CourseMapper;
import com.aptech.coursemanagementserver.models.Achievement;
import com.aptech.coursemanagementserver.models.Course;
import com.aptech.coursemanagementserver.models.Enrollment;
import com.aptech.coursemanagementserver.models.Section;
import com.aptech.coursemanagementserver.models.Tag;
import com.aptech.coursemanagementserver.models.User;
import com.aptech.coursemanagementserver.repositories.AchievementRepository;
import com.aptech.coursemanagementserver.repositories.AuthorRepository;
import com.aptech.coursemanagementserver.repositories.CategoryRepository;
import com.aptech.coursemanagementserver.repositories.CourseRepository;
import com.aptech.coursemanagementserver.repositories.EnrollmentRepository;
import com.aptech.coursemanagementserver.repositories.NotificationRepository;
import com.aptech.coursemanagementserver.repositories.SectionRepository;
import com.aptech.coursemanagementserver.repositories.SubcribesRepository;
import com.aptech.coursemanagementserver.repositories.TagRepository;
import com.aptech.coursemanagementserver.services.CourseService;
import com.aptech.coursemanagementserver.services.authServices.UserService;
import com.github.slugify.Slugify;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {
    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;
    private final TagRepository tagRepository;
    private final SectionRepository sectionRepository;
    private final AchievementRepository achievementRepository;
    private final CategoryRepository categoryRepository;
    private final AuthorRepository authorRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserService userService;
    private final NotificationRepository notificationRepository;
    private final SubcribesRepository subcribesRepository;

    @Override
    public List<Course> findAllByTagName(String tagName) {
        return courseRepository.findAllByTagName(tagName);
    }

    @Override
    public Course findCourseById(long courseId) {
        return courseRepository.findById(courseId).orElseThrow(
                () -> new NoSuchElementException("This course with courseId: [" + courseId + "] is not exist."));
    }

    @Override
    public CourseDto findById(long courseId) {
        Course course = courseRepository.findById(courseId).orElseThrow(
                () -> new NoSuchElementException("This course with courseId: [" + courseId + "] is not exist."));

        CourseDto courseDto = toCourseDto(course);

        return courseDto;
    }

    @Override
    public CourseDto findBySlug(String slug) {
        Course course = courseRepository.findBySlug(slug).orElseThrow(
                () -> new NoSuchElementException("This course with slug: [" + slug + "] is not exist."));

        CourseDto courseDto = toCourseDto(course);

        return courseDto;
    }

    @Override
    public Course findByName(String courseName) {
        return courseRepository.findByName(courseName);
    }

    @Override
    public List<CourseInterface> findAllCoursesByUserId(long userId) {
        List<CourseInterface> courseDtos = courseRepository.findAllCoursesByUserId(userId);

        return courseDtos;
    }

    @Override
    public List<CourseInterface> findAllCourses() {
        List<CourseInterface> courseDtos = courseRepository.findAllCourses();

        if (userService.findCurrentUser() == null || userService.checkIsUser()) {
            courseDtos = courseDtos.stream().filter(c -> c.getStatus() == 1).toList();
        }

        return courseDtos;
    }

    @Override
    public List<CourseDto> findBestSellerCourses() {
        // Free Course: Query theo enrolled, count thằng nào enrolled nhiều nhất là lên
        // top đầu
        List<Long> courseIds = courseRepository.findBestSellerCourseIds();

        List<CourseDto> courseDtos = new ArrayList<>();

        for (Long courseId : courseIds) {
            Course course = courseRepository.findById(courseId).get();
            CourseDto courseDto = toCourseDto(course);
            courseDtos.add(courseDto);
        }

        // if (userService.findCurrentUser() == null || userService.checkIsUser()) {
        courseDtos = courseDtos.stream().filter(c -> c.getStatus() == 1).toList();
        // }
        return courseDtos;
    }

    @Override
    public List<CourseDto> findFreeCourses() {
        // Free Course: Query theo price, nếu Price == 0 thì lấy ra
        List<Course> courses = courseRepository.findAll();
        List<CourseDto> courseDtos = new ArrayList<>();

        courses = courses.stream().filter(c -> c.getStatus() == 1 && c.getPrice() == 0).toList();

        // if (userService.findCurrentUser() != null && !userService.checkIsUser()) {
        // courses = courses.stream().filter(c -> c.getPrice() == 0).toList();
        // }

        for (Course course : courses) {
            CourseDto courseDto = toCourseDto(course);
            courseDtos.add(courseDto);
        }
        return courseDtos;
    }

    @Override
    public List<CourseDto> findRelatedCourses(CourseRelatedDto relatedDto) {
        List<String> tagNames = new ArrayList<>();
        if (!(relatedDto.getTagName() == null || relatedDto.getTagName().isBlank())) {
            Arrays.asList(relatedDto.getTagName().trim().split(","));
        }

        boolean isExistTag = tagNames.size() > 0;
        List<Course> courses = courseRepository.findAll();
        List<CourseDto> courseDtos = new ArrayList<>();
        courses = courses.stream()
                .filter(c -> c.getStatus() == 1 &&
                        (c.getCategory().getId() == relatedDto.getCategoryId()))
                .toList();

        if (isExistTag) {

            // Set<Tag> tags = new HashSet<>();
            // for (String tagName : tagNames) {
            // Tag tag = new Tag();
            // tag.setName(tagName);
            // tags.add(tag);
            // }

            courses = courses.stream().filter(c -> c.getTags().stream().anyMatch(t -> tagNames.contains(t.getName())))
                    .toList();

        }
        for (Course course : courses) {
            CourseDto courseDto = toCourseDto(course);
            courseDtos.add(courseDto);
        }
        return courseDtos;
    }

    @Override
    public List<CourseDto> findAll() {
        List<Course> courses = courseRepository.findAll();
        List<CourseDto> courseDtos = new ArrayList<>();
        if (userService.findCurrentUser() == null || userService.checkIsUser()) {
            courses = courses.stream().filter(c -> c.getStatus() == 1).toList();
        }
        for (Course course : courses) {

            CourseDto courseDto = toCourseDto(course);
            courseDtos.add(courseDto);

        }
        return courseDtos;
    }

    @Override
    public Course save(Course course) {
        return courseRepository.save(course);
    }

    @Override
    public Course save(CourseDto courseDto) {
        Course course = new Course();

        setProperties(courseDto, course);
        List<User> organizeUsers = userService.findAllExceptRoleUSER();
        if (organizeUsers != null) {

            organizeUsers.forEach(user -> {
                Enrollment enrollment = new Enrollment();
                enrollment.setCourse(course);
                enrollment.setUser(user);
                enrollmentRepository.save(enrollment);
            });

        }

        return course;
    }

    public Course setProperties(CourseDto courseDto, Course course) {
        User user = userService.findCurrentUser();

        if (courseDto.getStatus() == 0 && courseDto.getEnrollmentCount() > 0) {
            throw new BadRequestException("Cannot deactivate course that 've already had user's enrollment");
        }

        if (courseDto.getAuthor() == 0) {
            throw new BadRequestException(
                    (course.getId() > 0 ? "Cannot update" : "Cannot create") + " course without Author.");
        }

        if (course.getStatus() == 0 && courseDto.getStatus() == 1) {
            course.setPublished_at(new Date());
            CourseTreeInterface courseTree = courseRepository.findCourseTreeByCourseId(courseDto.getId());
            if (courseTree != null) {
                if (courseTree.getSectionId() == 0
                        || (courseTree.getSectionId() != 0 && courseTree.getSectionStatus() == 0)) {
                    throw new BadRequestException("Cannot activate course that don't have enough section.");
                }
                if (courseTree.getLessonId() == 0
                        || (courseTree.getLessonId() != 0 && courseTree.getLessonStatus() == 0)) {
                    throw new BadRequestException("Cannot activate course that don't have enough lesson.");
                }
                if (courseTree.getVideoId() == 0) {
                    throw new BadRequestException("Cannot activate course that don't have enough video.");
                }
            }
        }

        course.setName(courseDto.getName().replaceAll("\\s{2,}", " "))
                .setCategory(categoryRepository.findById(courseDto.getCategory()).get())
                .setAuthor(authorRepository.findById(courseDto.getAuthor()).get())
                .setTags(splitTag(courseDto.getTagName(), course))
                .setAchievements(
                        splitAchievement(courseDto.getAchievementName(), course))
                .setLevel(courseDto.getLevel())
                .setStatus(courseDto.getStatus())
                .setRating(courseDto.getRating())
                .setImage(courseDto.getImage())
                .setSlug(Slugify.builder().build().slugify(courseDto.getName()))
                // .setDuration(courseDto.getDuration())
                .setDescription(courseDto.getDescription())
                .setPrice(courseDto.getPrice())
                .setNet_price(courseDto.getNet_price())
                .setRequirement(courseDto.getRequirement())
                .setUpdatedBy(user.getEmail().split("@")[0]);

        // courseRepository.save(course);

        // Proccessing Section

        List<String> sectionsStrings = courseDto.getSections();
        if (sectionsStrings != null) {
            Set<Section> sections = course.getSections();
            Set<Section> sectionFromString = new HashSet<>();
            // Create new set Section from List<String> sectionsStrings
            for (String sectionStr : sectionsStrings) {
                Section section = new Section();
                section.setName(sectionStr);
                section.setCourse(course);
                sectionFromString.add(section);
            }
            // Merge current set and set from list<String>
            sections.addAll(sectionFromString); // Will ignore Section existed.

            // Remove Section has been delete (Section not in List<String> sectionsStrings)
            Set<Section> tempSections = new HashSet<>();
            tempSections.addAll(sections);
            for (Section section : sections) {

                if (!sectionsStrings.contains(section.getName())) {
                    sectionRepository.deleteSectionsById(section.getId());
                    tempSections.remove(section);
                }
            }
            sections = tempSections;
            course.setSections(sections);
        }

        // Nếu course status == 1
        Course courseNotif = courseRepository.save(course);
        if (courseNotif.getStatus() == 1) {
            notificationRepository.pushCourseNotificationToUser(courseNotif.getAuthor().getId(),
                    courseNotif.getId());
        }

        return course;
    }

    @Override
    public List<Course> saveAll(List<CourseDto> coursesDto) {
        List<Course> courses = courseMapper.toEntityList(coursesDto);
        courseRepository.saveAll(courses);
        return courses;
    }

    @Override
    public Set<Tag> splitTag(String tag, Course course) {
        boolean isUpdatedCourse = course.getId() > 0 ? true : false;

        Set<Tag> newTags = new HashSet<>();
        List<Tag> allTags = tagRepository.findAll();
        List<Tag> tempAllTags = new ArrayList<>();
        tempAllTags.addAll(allTags);

        String[] tags = tag.split(",");
        List<String> list = Arrays.asList(tags);

        for (String tagName : tags) {
            // Error case 1: Add new tag and old tag in same String -> return only new Tag
            // Error case 2: No new tag -> return nothing
            Tag foundedTag = tagRepository.findTagByName(tagName);
            if (foundedTag == null) {
                Tag newTag = new Tag();
                newTag.setName(tagName);
                newTags.add(newTag);
                // Add new tag to temp
                tempAllTags.add(newTag);
            }
        }

        if (allTags.size() > 0) {
            for (Tag t : allTags) {
                if (!list.contains(t.getName())) {
                    tempAllTags.remove(t);
                }
            }
        }
        // To return with for create tag
        Set<Tag> returnTags = tempAllTags.stream().collect(Collectors.toSet());

        if (newTags.size() > 0) {
            // Only create new tag
            tagRepository.saveAll(newTags);
        }

        if (isUpdatedCourse) {
            var tagsOfCourse = course.getTags();
            Set<Tag> tempTag = new HashSet<>();
            tempTag.addAll(tagsOfCourse);

            for (Tag curreTag : tagsOfCourse) {
                if (!list.contains(curreTag.getName())) {
                    tempTag.remove(curreTag);
                }
            }

            tagsOfCourse = tempTag;
            if (newTags.size() > 0) {
                for (Tag newTag : newTags) {
                    tagsOfCourse.add(newTag);
                }
            }
            return tagsOfCourse;
        }

        return returnTags;
    }

    @Override
    public Set<Achievement> splitAchievement(String achievement, Course course) {

        boolean isUpdatedCourse = course.getId() > 0 ? true : false;

        // Check achievement if not exist add new.
        Set<Achievement> newAchievements = new HashSet<>();
        List<Achievement> allAchievements = achievementRepository.findAll();
        List<Achievement> tempAllAchievements = new ArrayList<>();
        tempAllAchievements.addAll(allAchievements);

        String[] achievements = achievement.split(",");
        List<String> list = Arrays.asList(achievements);
        for (String achievementName : achievements) {
            // Chỗ này nếu tạo course mới cùng tag sẽ mặc định tìm đc achievement -> null
            Achievement foundedAchievement = achievementRepository.findAchievementByName(achievementName);
            if (foundedAchievement == null) {
                Achievement newAchievement = new Achievement();
                newAchievement.setName(achievementName);
                newAchievements.add(newAchievement);
                // Add new achievement to temp
                tempAllAchievements.add(newAchievement);
            }
        }

        if (allAchievements.size() > 0) {
            for (Achievement a : allAchievements) {
                if (!list.contains(a.getName())) {
                    tempAllAchievements.remove(a);
                }
            }
        }
        // To return with for create tag
        Set<Achievement> returnAchievements = tempAllAchievements.stream().collect(Collectors.toSet());

        if (newAchievements.size() > 0) {
            // Only create new achievement
            achievementRepository.saveAll(newAchievements);
        }

        if (isUpdatedCourse) {
            var achievementOfCoure = course.getAchievements();

            Set<Achievement> tempAchievement = new HashSet<>();
            tempAchievement.addAll(achievementOfCoure);

            for (Achievement curreAchievement : achievementOfCoure) {
                if (!list.contains(curreAchievement.getName())) {
                    tempAchievement.remove(curreAchievement);
                }
            }

            achievementOfCoure = tempAchievement;
            if (newAchievements.size() > 0) {
                for (Achievement newAchievement : newAchievements) {
                    achievementOfCoure.add(newAchievement);
                }
            }
            return achievementOfCoure;
        }

        return returnAchievements;
    }

    @Override
    public BaseDto delete(long courseId) {
        try {
            Course course = courseRepository.findById(courseId).get();

            if (courseRepository.findEnrollemntCountByCourseId(courseId) > 0) {
                throw new BadRequestException("Cannot delete course that 've already had user's enrollment");
            }

            courseRepository.delete(course);
            return BaseDto.builder().type(AntType.success).message("Delete course successfully.")
                    .build();
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("This course with courseId: [" + courseId + "] is not exist.");
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            throw new BadRequestException(BAD_REQUEST_EXCEPTION);
        }
    }

    private CourseDto toCourseDto(Course course) {
        List<String> achievementsList = course.getAchievements().stream()
                .map(achievement -> achievement.getName())
                .toList();
        List<String> tagsList = course.getTags().stream().map(tag -> tag.getName()).toList();
        int enrollmentCount = courseRepository.findEnrollemntCountByCourseId(course.getId());
        CourseDto courseDto = CourseDto.builder().id(course.getId())
                .name(course.getName())
                .price(course.getPrice())
                .net_price(course.getNet_price()).slug(course.getSlug())
                .image(course.getImage())
                .description(course.getDescription())
                .level(course.getLevel())
                .status(course.getStatus())
                .rating(course.getRating())
                .sections(course.getSections().stream()
                        .map(section -> section.getName())
                        .toList())
                .enrollmentCount(enrollmentCount)
                .category(course.getCategory().getId())
                .category_name(course.getCategory().getName())
                .author(course.getAuthor().getId())
                .author_name(course.getAuthor().getName())
                .author_image(course.getAuthor().getImage())
                .achievementName(String.join(",", achievementsList))
                .tagName(String.join(",", tagsList))
                .duration(course.getDuration())
                .requirement(course.getRequirement())
                .updatedBy(course.getUpdatedBy())
                .build();

        return courseDto;
    }
}
