package com.aptech.coursemanagementserver.controllers;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aptech.coursemanagementserver.dtos.CommentDto;
import com.aptech.coursemanagementserver.dtos.NotificationRequestDto;
import com.aptech.coursemanagementserver.dtos.PostDto;
import com.aptech.coursemanagementserver.exceptions.BadRequestException;
import com.aptech.coursemanagementserver.exceptions.ResourceNotFoundException;
import com.aptech.coursemanagementserver.models.Post;
import com.aptech.coursemanagementserver.services.PostService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import static com.aptech.coursemanagementserver.constants.GlobalStorage.GLOBAL_EXCEPTION;

@RestController
@RequiredArgsConstructor
@RequestMapping("/post")
@Tag(name = "Post Endpoints")
public class PostController {
    private final PostService postService;

    @GetMapping("/stream/{typeId}/{type}")
    @Operation(summary = "[ANY ROLE] - Stream Posts")
    // @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MANAGER', 'EMPLOYEE')")
    public Flux<ServerSentEvent<List<PostDto>>> streamPosts(@PathVariable("typeId") long typeId,
            @PathVariable("type") String type) {
        try {
            return postService.streamPosts(typeId, type);
        } catch (NoSuchElementException e) {
            throw new ResourceNotFoundException(e.getMessage());
        } catch (Exception e) {
            throw new BadRequestException(GLOBAL_EXCEPTION);
        }
    }

    // @GetMapping("/{courseId}")
    // @Operation(summary = "[ANY ROLE] - Get All Posts")
    // @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MANAGER', 'EMPLOYEE')")
    // public ResponseEntity<List<PostDto>>
    // getAllPostsByCourseId(@PathVariable("courseId") long courseId) {
    // try {
    // return ResponseEntity.ok(postService.findAllByCourseId(courseId));
    // } catch (NoSuchElementException e) {
    // throw new ResourceNotFoundException(e.getMessage());
    // } catch (Exception e) {
    // throw new BadRequestException(GLOBAL_EXCEPTION);
    // }
    // }

    @GetMapping("/{postId}")
    @Operation(summary = "[ANY ROLE] - Get Post By PostId")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<PostDto> getPostById(@PathVariable int postId) {
        try {
            return ResponseEntity.ok(postService.getPostById(postId));
        } catch (NoSuchElementException e) {
            throw new ResourceNotFoundException(e.getMessage());
        } catch (Exception e) {
            throw new BadRequestException(GLOBAL_EXCEPTION);
        }
    }

    @PostMapping
    @Operation(summary = "[ANY ROLE] - Create Post [Input userId, content]")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Void> createPost(@RequestBody PostDto postDto) {
        try {
            postService.create(postDto);
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            throw new ResourceNotFoundException(e.getMessage());
        } catch (Exception e) {
            throw new BadRequestException(GLOBAL_EXCEPTION);
        }
    }

    @GetMapping("/comment/{postId}")
    @Operation(summary = "[ANY ROLE] - Get Comments By PostId")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<CommentDto>> getCommentsByPostId(@PathVariable long postId) {
        try {
            return ResponseEntity.ok(postService.getCommentsByPostId(postId));
        } catch (NoSuchElementException e) {
            throw new ResourceNotFoundException(e.getMessage());
        } catch (Exception e) {
            throw new BadRequestException(GLOBAL_EXCEPTION);
        }
    }

    @PostMapping("/comment")
    @Operation(summary = "[ANY ROLE] - Create Comment [Input postId, commentId]")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Post> addComment(@RequestBody NotificationRequestDto request) {
        try {
            postService.addComment(request);
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            throw new ResourceNotFoundException(e.getMessage());
        } catch (Exception e) {
            throw new BadRequestException(GLOBAL_EXCEPTION);
        }
    }

    @DeleteMapping("/comment/{commentId}")
    @Operation(summary = "[ANY ROLE] - Delete Comment by commentId")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Void> removeComment(@PathVariable long commentId) {
        try {
            postService.removeComment(commentId);
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            throw new ResourceNotFoundException(e.getMessage());
        } catch (Exception e) {
            throw new BadRequestException(GLOBAL_EXCEPTION);
        }
    }

    @PostMapping("/like")
    @Operation(summary = "[ANY ROLE] - Create Like [Input postId, userId]")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Void> addLike(@RequestBody NotificationRequestDto request) {
        try {
            postService.addLike(request);
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            throw new ResourceNotFoundException(e.getMessage());
        } catch (Exception e) {
            throw new BadRequestException(GLOBAL_EXCEPTION);
        }
    }

    @DeleteMapping("/like")
    @Operation(summary = "[ANY ROLE] - Delete Like [Input postId, userId]")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Void> removeLike(@RequestBody NotificationRequestDto request) {
        try {
            postService.removeLike(request);
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            throw new ResourceNotFoundException(e.getMessage());
        } catch (Exception e) {
            throw new BadRequestException(GLOBAL_EXCEPTION);
        }
    }

    @DeleteMapping("/{postId}")
    @Operation(summary = "[ANY ROLE] - Delete Post By PostId")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Void> deletePostById(@PathVariable int postId) {
        try {
            postService.deletePostById(postId);
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            throw new ResourceNotFoundException(e.getMessage());
        } catch (Exception e) {
            throw new BadRequestException(GLOBAL_EXCEPTION);
        }
    }
}
