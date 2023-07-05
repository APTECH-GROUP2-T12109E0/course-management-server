package com.aptech.coursemanagementserver.services.servicesImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.aptech.coursemanagementserver.dtos.UserDto;
import com.aptech.coursemanagementserver.models.User;
import com.aptech.coursemanagementserver.repositories.UserRepository;
import com.aptech.coursemanagementserver.services.authServices.UserService;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User findCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return null;
        }
        return auth.getPrincipal() == "anonymousUser" ? null : (User) auth.getPrincipal();

    }

    @Override
    public Optional<User> findById(long id) {
        return userRepository.findById(id);
    }

    @Override
    public List<UserDto> findAllHasRoleUSER() {
        List<User> users = userRepository.findAllHasRoleUSER();

        List<UserDto> userDtos = new ArrayList<>();
        for (User user : users) {
            UserDto userDto = toDto(user);
            userDtos.add(userDto);
        }
        return userDtos;
    }

    @Override
    public List<UserDto> findAllExceptRoleADMIN() {
        List<User> users = userRepository.findAllExceptRoleADMIN();

        List<UserDto> userDtos = new ArrayList<>();
        for (User user : users) {
            UserDto userDto = toDto(user);
            userDtos.add(userDto);
        }
        return userDtos;
    }

    @Override
    public List<UserDto> findAllExceptRoleUSERAndRoleADMIN() {
        List<User> users = userRepository.findAllExceptRoleUSERAndRoleADMIN();

        List<UserDto> userDtos = new ArrayList<>();
        for (User user : users) {
            UserDto userDto = toDto(user);
            userDtos.add(userDto);
        }
        return userDtos;
    }

    @Override
    public boolean checkIsUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isUser = false;
        if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER"))) {
            isUser = true;
        }
        return isUser;
    }

    public boolean isContains(final List<User> list, final String email) {
        return list.stream().anyMatch(o -> o.getEmail().equals(email));
    }

    @Override
    public void save(User user) {
        userRepository.save(user);
    }

    @Override
    public void deleteById(long userId) {
        userRepository.deleteById(userId);
    }

    private UserDto toDto(User user) {
        UserDto userDto = UserDto.builder()
                .id(user.getId())
                .first_name(user.getFirst_name())
                .last_name(user.getLast_name())
                .name(user.getName())
                .role(user.getRole())
                .provider(user.getProvider())
                .providerId(user.getProviderId())
                .email(user.getEmail())
                .password(user.getPassword())
                .isVerified(user.isVerified())
                .imageUrl(user.getImageUrl())
                .status(user.getUserStatus())
                .created_at(user.getCreated_at())
                .updated_at(user.getUpdated_at())
                .build();
        return userDto;
    }

    @Override
    public List<UserDto> findAllRegisteredToday() {
        LocalDate today = LocalDate.now();
        Instant startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endOfDay = today.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant();

        List<User> users = userRepository.findByCreatedAtBetween(startOfDay, endOfDay);

        List<UserDto> userDtos = new ArrayList<>();
        for (User user : users) {
            UserDto userDto = toDto(user);
            userDtos.add(userDto);
        }
        return userDtos;
    }
}
