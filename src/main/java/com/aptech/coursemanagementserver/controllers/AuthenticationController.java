package com.aptech.coursemanagementserver.controllers;

import static com.aptech.coursemanagementserver.constants.GlobalStorage.DEV_DOMAIN_CLIENT;
import static com.aptech.coursemanagementserver.constants.GlobalStorage.GLOBAL_EXCEPTION;

import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aptech.coursemanagementserver.dtos.AuthenticationRequestDto;
import com.aptech.coursemanagementserver.dtos.AuthenticationResponseDto;
import com.aptech.coursemanagementserver.dtos.RegisterRequestDto;
import com.aptech.coursemanagementserver.dtos.baseDto.BaseDto;
import com.aptech.coursemanagementserver.enums.AntType;
import com.aptech.coursemanagementserver.events.RegistrationCompleteEvent;
import com.aptech.coursemanagementserver.exceptions.BadRequestException;
import com.aptech.coursemanagementserver.exceptions.IsExistedException;
import com.aptech.coursemanagementserver.exceptions.ResourceNotFoundException;
import com.aptech.coursemanagementserver.models.Token;
import com.aptech.coursemanagementserver.models.User;
import com.aptech.coursemanagementserver.repositories.TokenRepository;
import com.aptech.coursemanagementserver.services.authServices.AuthenticationService;
import com.aptech.coursemanagementserver.services.authServices.JwtService;
import com.aptech.coursemanagementserver.services.authServices.UserService;

import io.jsonwebtoken.ExpiredJwtException;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication Endpoints")
public class AuthenticationController {
  private final TokenRepository tokenRepository;
  private final UserService userService;
  private final AuthenticationService authService;
  private final JwtService jwtService;
  private final ApplicationEventPublisher publisher;
  private final PasswordEncoder passwordEncoder;

  @PostMapping("/register")
  public ResponseEntity<BaseDto> register(
      @RequestBody RegisterRequestDto request) {

    try {
      var user = authService.register(request);

      publisher.publishEvent(new RegistrationCompleteEvent(user));

      return new ResponseEntity<BaseDto>(BaseDto.builder().type(AntType.success)
          .message("Success! Please, check your email to complete your registration.").build(), HttpStatus.OK);

    } catch (IsExistedException e) {
      throw new IsExistedException(e.getMessage());
    } catch (Exception e) {
      throw new BadRequestException(e.getMessage());
    }
  }

  @GetMapping("/verifyEmail")
  public ResponseEntity<BaseDto> verifyEmail(@RequestParam("token") String token) throws ParseException {
    Token t = tokenRepository.findByToken(token).get();
    if (t.getUser().isVerified()) {
      return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(DEV_DOMAIN_CLIENT + "/login?verify=verified"))
          .build();
    }
    try {
      jwtService.isTokenValid(token, t.getUser());
      authService.verifyEmailRegister(token);
      return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(DEV_DOMAIN_CLIENT + "/login?verify=success"))
          .build();

    } catch (ExpiredJwtException e) {
      userService.deleteById(t.getUser().getId());
      return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(DEV_DOMAIN_CLIENT + "/token-expire"))
          .build();
    }
    // return new ResponseEntity<String>("Email verified successfully. Now
    // login to your account",
    // HttpStatus.OK);
  }

  @PostMapping("/reset-password")
  public ResponseEntity<BaseDto> resetPassword(@RequestParam("token") String token, AuthenticationRequestDto request)
      throws ParseException {
    Token t = tokenRepository.findByToken(token)
        .orElseThrow(() -> new NoSuchElementException("The reset password Token is not exist or has been used."));

    try {
      jwtService.isTokenValid(token, t.getUser());
      User user = userService.findById(t.getUser().getId()).get();
      user.setPassword(passwordEncoder.encode(request.getPassword()));
      userService.save(user);
      // Xóa token lúc reset password thành công
      tokenRepository.delete(t);

      return ResponseEntity.ok(BaseDto.builder().message("Reset password successfully.").type(AntType.success).build());

    } catch (ExpiredJwtException e) {
      throw new BadRequestException("Reset password token is expired.");
    } catch (NoSuchElementException e) {
      throw new ResourceNotFoundException(e.getMessage());
    } catch (Exception e) {
      throw new BadRequestException(GLOBAL_EXCEPTION);
    }
  }

  @PostMapping("/login")
  public ResponseEntity<AuthenticationResponseDto> login(
      @RequestBody AuthenticationRequestDto request) {
    try {
      return new ResponseEntity<AuthenticationResponseDto>(authService.login(request), HttpStatus.OK);
    } catch (NoSuchElementException e) {
      throw new ResourceNotFoundException(e.getMessage());
    } catch (Exception e) {
      throw new BadRequestException(e.getMessage());
    }
  }

  @PostMapping("/authenticate")
  public ResponseEntity<AuthenticationResponseDto> authenticate(
      @RequestBody AuthenticationRequestDto request) {
    return ResponseEntity.ok(authService.authenticate(request));
  }

  @PostMapping("/refresh-token")
  public void refreshToken(
      HttpServletRequest request,
      HttpServletResponse response) throws IOException {
    authService.refreshToken(request, response);
  }

  @GetMapping("/refresh-token/{refreshToken}")
  public ResponseEntity<AuthenticationResponseDto> renewToken(@PathVariable String refreshToken) {

    String email = jwtService.extractUsername(refreshToken);

    Optional<User> user = userService.findByEmail(email);
    if (user.isPresent()) {
      List<Token> previousTokenByUser = tokenRepository.findAllValidTokenByUser(user.get().getId());
      tokenRepository.deleteAll(previousTokenByUser);
      String newToken = jwtService.saveUserToken(user.get());
      String newRefreshToken = jwtService.saveUserRefreshToken(user.get());
      AuthenticationResponseDto responseDto = AuthenticationResponseDto.builder()
          .accessToken(newToken).refreshToken(newRefreshToken)
          .type(AntType.success).message("Refresh token successfully.").build();
      return ResponseEntity.ok(responseDto);
    }
    return ResponseEntity.badRequest().body(null);
  }

  @PostMapping("/forget-password")
  public ResponseEntity<?> forgetPassword(@RequestBody AuthenticationRequestDto request) {
    try {
      authService.forgetPassword(request);
      return ResponseEntity.ok("");
    } catch (NoSuchElementException e) {
      throw new ResourceNotFoundException(e.getMessage());
    } catch (Exception e) {
      throw new BadRequestException(GLOBAL_EXCEPTION);
    }
  }

  @PostMapping("/change-password")
  public ResponseEntity<?> changePassword(@RequestBody AuthenticationRequestDto request) {
    try {
      authService.changePassword(request);
      return ResponseEntity.ok("");
    } catch (NoSuchElementException e) {
      throw new ResourceNotFoundException(e.getMessage());
    } catch (BadRequestException e) {
      throw new BadRequestException(e.getMessage());
    } catch (Exception e) {
      throw new BadRequestException(GLOBAL_EXCEPTION);
    }
  }

  @GetMapping("/noauth")
  public ResponseEntity<?> noAuth() {
    Map<String, String> body = new HashMap<>();
    body.put("message", "unauthorized");
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
  }

}
