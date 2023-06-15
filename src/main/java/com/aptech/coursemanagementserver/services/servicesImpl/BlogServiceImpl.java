package com.aptech.coursemanagementserver.services.servicesImpl;

import static com.aptech.coursemanagementserver.constants.GlobalStorage.BAD_REQUEST_EXCEPTION;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.aptech.coursemanagementserver.dtos.BlogDto;
import com.aptech.coursemanagementserver.dtos.BlogsInterface;
import com.aptech.coursemanagementserver.dtos.baseDto.BaseDto;
import com.aptech.coursemanagementserver.enums.AntType;
import com.aptech.coursemanagementserver.enums.BlogStatus;
import com.aptech.coursemanagementserver.exceptions.BadRequestException;
import com.aptech.coursemanagementserver.models.Blog;
import com.aptech.coursemanagementserver.models.User;
import com.aptech.coursemanagementserver.repositories.BlogRepository;
import com.aptech.coursemanagementserver.services.BlogService;
import com.aptech.coursemanagementserver.services.authServices.UserService;
import com.github.slugify.Slugify;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BlogServiceImpl implements BlogService {

    private final BlogRepository blogRepository;
    private final UserService userService;

    @Override
    public Blog findBlogByName(String name) {
        return blogRepository.findBlogByName(name);
    }

    @Override
    public List<BlogDto> findAll() {
        List<Blog> blogs = blogRepository.findByStatus(BlogStatus.PROCCESSING);
        List<BlogDto> blogDtos = new ArrayList<>();
        for (Blog blog : blogs) {
            BlogDto blogDto = toBlogDto(blog);
            blogDtos.add(blogDto);
        }
        return blogDtos;
    }

    @Override
    public BaseDto create(BlogDto blogDto) {
        try {
            Blog blog = new Blog();
            Date now = new Date();
            User user = userService.findById(blogDto.getUser_id()).orElseThrow(() -> new NoSuchElementException(
                    "The user with userId: [" + blogDto.getUser_id() + "] is not exist."));
            blog.setId(blogDto.getId())
                    .setName(blogDto.getName())
                    .setSlug(Slugify.builder().build().slugify(blogDto.getName()))
                    .setUser(user)
                    .setView_count(blogDto.getView_count())
                    .setStatus(blogDto.getStatus())
                    .setDescription(blogDto.getDescription())
                    .setCreated_at(now)
                    .setUpdated_at(now);
            blogRepository.save(blog);
            return BaseDto.builder().type(AntType.success).message("Create blog successfully.").build();
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException(
                    "The blog with blogId: [" + blogDto.getId() + "] is not exist.");
        } catch (Exception e) {
            throw new BadRequestException(BAD_REQUEST_EXCEPTION);
        }
    }

    @Override
    public BaseDto update(BlogDto blogDto) {
        try {
            Blog blog = blogRepository.findById(blogDto.getId()).orElseThrow(() -> new NoSuchElementException(
                    "The blog with blogId: [" + blogDto.getId() + "] is not exist."));
            Date now = new Date();
            User user = userService.findById(blogDto.getUser_id()).orElseThrow(() -> new NoSuchElementException(
                    "The user with userId: [" + blogDto.getUser_id() + "] is not exist."));
            blog.setName(blogDto.getName())
                    .setSlug(Slugify.builder().build().slugify(blogDto.getName()))
                    .setUser(user)
                    .setView_count(blogDto.getView_count())
                    .setStatus(blogDto.getStatus())
                    .setDescription(blogDto.getDescription())
                    .setUpdated_at(now);

            blogRepository.save(blog);

            return BaseDto.builder().type(AntType.success).message("Update blog successfully.").build();
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException(e.getMessage());
        } catch (Exception e) {
            throw new BadRequestException(BAD_REQUEST_EXCEPTION);
        }
    }

    @Override
    public BaseDto delete(long blogId) {
        try {
            Blog blog = blogRepository.findById(blogId).orElseThrow(
                    () -> new NoSuchElementException("The blog with blogId: [" + blogId + "] is not exist."));
            blogRepository.delete(blog);
            return BaseDto.builder().type(AntType.success).message("Delete blog successfully.")
                    .build();
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException(e.getMessage());
        } catch (Exception e) {
            throw new BadRequestException(BAD_REQUEST_EXCEPTION);
        }
    }

    @Override
    public BlogDto findById(long blogId) {
        Blog blog = blogRepository.findById(blogId).orElseThrow(
                () -> new NoSuchElementException("This blog with blogId: [" + blogId + "] is not exist."));
        BlogDto blogDto = toBlogDto(blog);
        return blogDto;
    }

    private BlogDto toBlogDto(Blog blog) {
        BlogDto blogDto = BlogDto.builder()
                .id(blog.getId())
                .name(blog.getName())
                .slug(blog.getSlug())
                .view_count(blog.getView_count())
                .status(blog.getStatus())
                .description(blog.getDescription())
                .user_id(blog.getUser().getId())
                .build();
        return blogDto;
    }

    @Override
    public List<BlogDto> findAllBlogsByUserId(long userId) {
        List<Blog> blogs = blogRepository.findByUser_Id(userId);
        List<BlogDto> blogDtos = new ArrayList<>();
        for (Blog blog : blogs) {
            BlogDto blogDto = toBlogDto(blog);
            blogDtos.add(blogDto);
        }
        return blogDtos;
    }

    @Override
    public List<BlogsInterface> findAllBlogs() {
        List<BlogsInterface> blogDtos = blogRepository.findAllBlogs();

        if (userService.findCurrentUser() == null || userService.checkIsUser()) {
            blogDtos = blogDtos.stream().filter(c -> c.getStatus() == BlogStatus.ACTIVE).toList(); // load status = 1
                                                                                                   // (ACTIVE)
        }

        return blogDtos;
    }

}
