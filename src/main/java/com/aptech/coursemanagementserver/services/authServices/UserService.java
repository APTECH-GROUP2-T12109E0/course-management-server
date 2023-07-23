package com.aptech.coursemanagementserver.services.authServices;

import java.util.List;
import java.util.Optional;

import org.springframework.http.codec.ServerSentEvent;

import com.aptech.coursemanagementserver.dtos.UserDto;
import com.aptech.coursemanagementserver.models.Permissions;
import com.aptech.coursemanagementserver.models.Roles;
import com.aptech.coursemanagementserver.models.User;

import reactor.core.publisher.Flux;

public interface UserService {
    public Optional<User> findById(long id);

    public UserDto findByIdStream(long id);

    Optional<User> findByEmail(String email);

    public User findCurrentUser();

    public List<Roles> findAllRoleExceptRoleADMIN();

    public List<Roles> findManagerAndEmployeeRole();

    public List<Permissions> findAllPermissionExceptPermissionADMIN();

    public List<UserDto> findAllExceptRoleADMIN();

    public List<UserDto> findAllHasRoleUSER();

    public List<User> findAllExceptRoleUSER();

    public void deleteById(long userId);

    public boolean checkIsUser();

    public void save(User user);

    public boolean isContains(final List<User> list, final String email);

    public UserDto toDto(User user);

    public Flux<ServerSentEvent<UserDto>> streamCurrentUser(long userId);

}
