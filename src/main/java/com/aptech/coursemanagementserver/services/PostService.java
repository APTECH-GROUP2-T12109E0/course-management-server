package com.aptech.coursemanagementserver.services;

import java.util.List;

import org.springframework.http.codec.ServerSentEvent;

import com.aptech.coursemanagementserver.dtos.CommentDto;
import com.aptech.coursemanagementserver.dtos.NotificationRequestDto;
import com.aptech.coursemanagementserver.dtos.PostDto;

import reactor.core.publisher.Flux;

public interface PostService {

    public PostDto getPostById(long postId);

    public List<CommentDto> getCommentsByPostId(long postId);

    public List<PostDto> findAllByTypeIdAndType(long typeId, String type);

    public void create(PostDto postDto);

    public void deletePostById(long postId);

    public void addComment(NotificationRequestDto dto);

    public void removeComment(long commentId);

    public void addLike(NotificationRequestDto dto);

    public void removeLike(NotificationRequestDto dto);

    public Flux<ServerSentEvent<List<PostDto>>> streamPosts(long typeId, String type);
}
