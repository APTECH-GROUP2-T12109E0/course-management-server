package com.aptech.coursemanagementserver.events.listeners;

import static com.aptech.coursemanagementserver.constants.GlobalStorage.DEV_DOMAIN_API;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.aptech.coursemanagementserver.events.RegistrationCompleteEvent;
import com.aptech.coursemanagementserver.models.User;
import com.aptech.coursemanagementserver.services.authServices.JwtService;
import com.aptech.coursemanagementserver.utils.EmailSender;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegistrationCompleteEventListener implements ApplicationListener<RegistrationCompleteEvent> {

    @Value("${spring.mail.username}")
    private String fromEmail;
    private final EmailSender emailSender;
    private final JwtService jwtService;
    private User user;

    @Override
    public void onApplicationEvent(RegistrationCompleteEvent event) {
        // 1. Get the newly registered user
        user = event.getUser();
        // 2. Create a verification token for the user
        String verificationToken = jwtService.generateToken(user);
        // 3. Save the verification token for the user
        jwtService.saveUserVerificationToken(user, verificationToken);
        // 4 Build the verification url to be sent to the user
        String url = DEV_DOMAIN_API + "/auth/verifyEmail?token=" + verificationToken;
        // 5. Send the email.
        try {
            sendVerificationEmail(url);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        log.info("Click the link to verify your registration :  {}", url);
    }

    public void sendVerificationEmail(String url) throws MessagingException, UnsupportedEncodingException {
        String subject = "Email Verification";
        String displayName = "CoursePortal";
        String mailContent = "<p> Hi, " + user.getFirst_name() + ", </p>" +
                "<p>Thank you for registering with us," + "" +
                "Please, follow the link below to complete your registration.</p>" +
                "<a href=\"" + url + "\">Verify your email to activate your account</a>" +
                "<p> Thank you <br> CoursePortal";

        emailSender.sendEmail(fromEmail, displayName, user.getEmail(), subject, mailContent);
    }
}
