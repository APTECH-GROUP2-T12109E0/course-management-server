package com.aptech.coursemanagementserver.services.servicesImpl;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;

import com.aptech.coursemanagementserver.dtos.CommentDto;
import com.aptech.coursemanagementserver.dtos.NotificationRequestDto;
import com.aptech.coursemanagementserver.dtos.PostDto;
import com.aptech.coursemanagementserver.dtos.UserDto;
import com.aptech.coursemanagementserver.enums.CommentType;
import com.aptech.coursemanagementserver.enums.NotificationType;
import com.aptech.coursemanagementserver.models.Comment;
import com.aptech.coursemanagementserver.models.Notification;
import com.aptech.coursemanagementserver.models.Post;
import com.aptech.coursemanagementserver.models.User;
import com.aptech.coursemanagementserver.repositories.CommentRepository;
import com.aptech.coursemanagementserver.repositories.PostRepository;
import com.aptech.coursemanagementserver.services.NotificationService;
import com.aptech.coursemanagementserver.services.PostService;
import com.aptech.coursemanagementserver.services.authServices.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostServiceImpl implements PostService {

        private final PostRepository postRepository;
        private final CommentRepository commentRepository;
        private final UserService userService;
        private final NotificationService notificationService;

        // Input: userId, content
        public void create(PostDto postDto) {
                User user = userService.findById(postDto.getUserId())
                                .orElseThrow(() -> new NoSuchElementException(
                                                "The user with userId: [" + postDto.getUserId()
                                                                + "] is not exist."));
                Post post = new Post();
                post.setTypeId(postDto.getTypeId());
                post.setType(postDto.getType());
                post.setContent(postDto.getContent());
                post.setUserPost(user);
                post.setComments(new ArrayList<>());
                post.setLikedUsers(new ArrayList<>());
                postRepository.save(post);

        }

        public List<PostDto> findAllByTypeIdAndType(long typeId, String type) {
                List<Post> posts = postRepository.findAllByTypeIdAndType(typeId, CommentType.valueOf(type));
                List<PostDto> postDtos = new ArrayList<>();

                for (Post post : posts) {
                        PostDto postDto = toPostDto(post);
                        postDtos.add(postDto);
                }

                return postDtos;
        }

        public PostDto getPostById(long postId) {
                Post post = postRepository.findById(postId).orElseThrow(() -> new NoSuchElementException(
                                "The post with postId: [" + postId + "] is not exist."));
                PostDto postDto = toPostDto(post);

                return postDto;
        }

        public void deletePostById(long postId) {
                var post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("post not found!"));
                postRepository.delete(post);
        }

        // Input: postId, commentId, userId
        public void addComment(NotificationRequestDto dto) {
                Post post = postRepository.findById(dto.getPostId()).orElseThrow(() -> new NoSuchElementException(
                                "The post with postId: [" + dto.getPostId() + "] is not exist."));

                Comment comment = new Comment();
                User commentUser = userService.findById(dto.getUserId())
                                .orElseThrow(() -> new NoSuchElementException(
                                                "The user with userId: [" + dto.getUserId()
                                                                + "] is not exist."));
                comment.setUser(commentUser);
                comment.setContent(dto.getContent());
                comment.setPost(post);

                User postUser = userService.findByEmail(post.getUserPost().getEmail())
                                .orElseThrow(() -> new NoSuchElementException(
                                                "The User with email: [" + post.getUserPost().getEmail()
                                                                + "] is not exist."));
                post.setUserPost(postUser);

                post.getComments().add(comment);

                if (dto.getUserId() != postUser.getId()) {
                        notificationService.save(Notification.builder()
                                        .isDelivered(false)
                                        .content("New comment from " + commentUser.getName())
                                        .notificationType(NotificationType.COMMENT)
                                        .userFrom(commentUser)
                                        .userTo(postUser).build());
                }

                postRepository.save(post);

        }

        // Input: commentId
        public void removeComment(long commentId) {
                // Comment comment = getCommentById(commentId);
                commentRepository.deleteCommentById(commentId);
        }

        // Input: postId, userId
        public void addLike(NotificationRequestDto dto) {
                Post post = postRepository.findById(dto.getPostId()).orElseThrow(() -> new NoSuchElementException(
                                "The post with postId: [" + dto.getPostId() + "] is not exist."));
                User postOfUser = post.getUserPost();
                // it's checking users at the same time
                User likedUser = userService.findById(dto.getUserId()).orElseThrow(() -> new NoSuchElementException(
                                "The UserId with UserId: [" + dto.getUserId() + "] is not exist."));
                if (post.getLikedUsers() != null
                                && userService.isContains(post.getLikedUsers(),
                                                likedUser.getEmail())) {
                        log.info("call remove like: " + likedUser.getEmail());
                        removeLike(dto);
                        return;
                }
                post.getLikedUsers().add(likedUser);

                if (dto.getUserId() != postOfUser.getId()) {
                        notificationService.save(Notification.builder()
                                        .isDelivered(false)
                                        .content("Like from " + likedUser.getName())
                                        .notificationType(NotificationType.LIKE)
                                        .userFrom(likedUser)
                                        .userTo(postOfUser).build());
                }
                postRepository.save(post);
        }

        // Input: postId, userId
        public void removeLike(NotificationRequestDto dto) {
                postRepository.removeByPostIdAndUserId(dto.getPostId(), dto.getUserId());
        }

        public List<CommentDto> getCommentsByPostId(long postId) {
                Post post = postRepository.findById(postId).orElseThrow(() -> new NoSuchElementException(
                                "The post with postId: [" + postId + "] is not exist."));

                List<Comment> comments = post.getComments();
                List<CommentDto> commentDtos = new ArrayList<>();

                for (Comment comment : comments) {
                        CommentDto commentDto = toCommentDto(comment);
                        commentDtos.add(commentDto);
                }

                return commentDtos;
        }

        public Flux<ServerSentEvent<List<PostDto>>> streamPosts(long typeId, String type) {
                return Flux.interval(Duration.ofSeconds(2))
                                .publishOn(Schedulers.boundedElastic())
                                .map(sequence -> ServerSentEvent.<List<PostDto>>builder().id(String.valueOf(sequence))
                                                .event("post-list-event").data(findAllByTypeIdAndType(typeId, type))
                                                .build());
        }

        private PostDto toPostDto(Post post) {
                List<Comment> comments = post.getComments().stream().distinct().toList();
                List<User> users = post.getLikedUsers();
                List<CommentDto> commentDtos = new ArrayList<>();
                List<UserDto> userDtos = new ArrayList<>();

                for (Comment comment : comments) {
                        CommentDto commentDto = toCommentDto(comment);
                        commentDtos.add(commentDto);
                }

                for (User user : users) {
                        UserDto userDto = toUserDto(user);
                        userDtos.add(userDto);
                }

                PostDto postDto = PostDto.builder()
                                .id(post.getId())
                                // .commentsId(post.getComments().stream().map(c -> c.getId()).toList())
                                .comments(commentDtos)
                                .userId(post.getUserPost().getId())
                                .userName(post.getUserPost().getName())
                                .likedUsers(userDtos)
                                .content(post.getContent())
                                .postImageUrl(post.getUserPost().getImageUrl())
                                .typeId(post.getTypeId())
                                .type(post.getType())
                                .role(post.getUserPost().getRole())
                                .created_at(post.getCreated_at())
                                .build();
                return postDto;
        }

        private CommentDto toCommentDto(Comment comment) {
                CommentDto commentDto = CommentDto.builder()
                                .id(comment.getId())
                                .userId(comment.getUser().getId())
                                .userName(comment.getUser().getName())
                                .imageUrl(comment.getUser().getImageUrl())
                                .role(comment.getUser().getRole())
                                .postId(comment.getPost().getId())
                                .content(comment.getContent())
                                .created_at(comment.getCreated_at())
                                .build();
                return commentDto;
        }

        private UserDto toUserDto(User user) {
                UserDto userDto = UserDto.builder()
                                .id(user.getId())
                                .email(user.getEmail())
                                .first_name(user.getFirst_name())
                                .last_name(user.getLast_name())
                                .imageUrl(user.getImageUrl())
                                .role(user.getRole())
                                .build();
                return userDto;
        }

        // private Comment getCommentById(long commentId) {
        // Optional<Comment> comment = commentRepository.findById(commentId);
        // if (comment.isPresent()) {
        // return comment.get();
        // }

        // return new Comment();
        // }

        // private User getUserById(long userId) {
        // User user = userService.findById(userId).orElseThrow(() -> new
        // NoSuchElementException(
        // "The user with userId: [" + userId + "] is not exist."));

        // return user;
        // }
}
