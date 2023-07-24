package com.aptech.coursemanagementserver.services.paymentServices;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.aptech.coursemanagementserver.dtos.payment.PaypalRequestDto;
import com.aptech.coursemanagementserver.enums.OrderStatus;
import com.aptech.coursemanagementserver.enums.payment.PaymentType;
import com.aptech.coursemanagementserver.models.Course;
import com.aptech.coursemanagementserver.models.Enrollment;
import com.aptech.coursemanagementserver.models.Orders;
import com.aptech.coursemanagementserver.models.User;
import com.aptech.coursemanagementserver.repositories.CourseRepository;
import com.aptech.coursemanagementserver.repositories.EnrollmentRepository;
import com.aptech.coursemanagementserver.repositories.OrdersRepository;
import com.aptech.coursemanagementserver.repositories.UserRepository;
import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Payer;
import com.paypal.api.payments.PayerInfo;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentExecution;
import com.paypal.api.payments.RedirectUrls;
import com.paypal.api.payments.Transaction;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaypalService {
    @Value("${paypal.account.personal.email}")
    private String personalEmail;
    @Value("${paypal.account.personal.password}")
    private String personalPassword;
    private final APIContext apiContext;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final OrdersRepository ordersRepository;

    public Payment createPayment(
            PaypalRequestDto dto,
            String cancelUrl,
            String successUrl) throws PayPalRESTException {
        Course course = courseRepository.findById(dto.getCourseId()).get();
        User user = userRepository.findById(dto.getUserId()).get();
        Orders order = new Orders();
        order.setUser(user)
                .setCourse(course)
                .setName(course.getName())
                .setDescription(course.getDescription())
                .setUserDescription(dto.getUserDescription())
                .setDuration(course.getDuration())
                .setPrice(course.getPrice())
                .setNet_price(course.getNet_price())
                .setImage(course.getImage())
                .setPayment(PaymentType.PAYPAL).setStatus(OrderStatus.PROCESSING);
        ordersRepository.save(order);

        Amount amount = new Amount();
        amount.setCurrency(dto.getCurrency());
        double total;
        if (course.getNet_price() == 0) {
            total = new BigDecimal(course.getPrice()).setScale(2, RoundingMode.HALF_UP).doubleValue();
        } else {
            total = new BigDecimal(course.getNet_price()).setScale(2, RoundingMode.HALF_UP).doubleValue();
        }
        amount.setTotal(String.format("%.2f", total));

        Transaction transaction = new Transaction();
        transaction.setDescription(dto.getDescription());
        transaction.setAmount(amount);
        transaction.setCustom(String.valueOf(order.getId()));

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        // Set payer info
        Payer payer = new Payer();
        payer.setPaymentMethod(dto.getMethod().toString());

        // Display custom user
        PayerInfo payerInfo = new PayerInfo();
        payerInfo.setEmail(personalEmail);

        payer.setPayerInfo(payerInfo);

        Payment payment = new Payment();

        // // Create presentation object
        // Presentation presentation = new Presentation();
        // presentation.setBrandName("My Custom Brand Name");
        // presentation.setLogoImage("http://localhost:8080/course/download/1");

        // // Create a web profile object
        // WebProfile webProfile = new WebProfile();
        // webProfile.setName("MyWebProfiles");
        // webProfile.setFlowConfig(new FlowConfig().setLandingPageType("Billing"));
        // webProfile.setPresentation(presentation);
        // // Use the WebProfile.create() method to create the web profile
        // WebProfile createdProfile = webProfile.create(apiContext);
        // // Set the experience profile ID inthe payment object
        // payment.setExperienceProfileId(createdProfile.getId());

        // Set the payment experience ID
        // payment.setExperienceProfileId("Your experience_profile_id");
        payment.setIntent(dto.getIntent().toString());
        payment.setPayer(payer);
        payment.setTransactions(transactions);

        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(cancelUrl);
        redirectUrls.setReturnUrl(successUrl);
        payment.setRedirectUrls(redirectUrls);

        return payment.create(apiContext);
    }

    public Payment executePayment(String paymentId, String payerId) throws PayPalRESTException {
        // Use the PaymentGetRequest to retrieve the payment
        Payment payment = Payment.get(apiContext, paymentId);

        PaymentExecution paymentExecute = new PaymentExecution();
        paymentExecute.setPayerId(payerId);
        return payment.execute(apiContext, paymentExecute);

    }

    public void updateOrderAndCreateEnroll(Payment payment, boolean isApproved) {

        Orders order = ordersRepository.findById(Long.valueOf(payment.getTransactions().get(0).getCustom())).get();
        if (isApproved) {
            order.setStatus(OrderStatus.COMPLETED);
            order.setTransactionId(payment.getId());
            Enrollment enrollment = new Enrollment();
            enrollment
                    .setProgress(0)
                    .setRating(0)
                    .setCourse(order.getCourse())
                    .setUser(order.getUser());

            enrollmentRepository.save(enrollment);
        } else {
            order.setStatus(OrderStatus.CANCELED);
        }
        ordersRepository.save(order);

    }
}
