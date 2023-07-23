package com.aptech.coursemanagementserver.services.paymentServices;

import static com.aptech.coursemanagementserver.constants.GlobalStorage.GLOBAL_EXCEPTION;
import static com.aptech.coursemanagementserver.constants.GlobalStorage.PAYPAL_CANCEL_API;
import static com.aptech.coursemanagementserver.constants.GlobalStorage.PAYPAL_SUCCESS_API;

import java.util.NoSuchElementException;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.aptech.coursemanagementserver.dtos.payment.CheckoutDto;
import com.aptech.coursemanagementserver.dtos.payment.MomoRequestDto;
import com.aptech.coursemanagementserver.dtos.payment.MomoResponseDto;
import com.aptech.coursemanagementserver.dtos.payment.PaypalRequestDto;
import com.aptech.coursemanagementserver.dtos.payment.PaypalResponseDto;
import com.aptech.coursemanagementserver.enums.payment.PaymentType;
import com.aptech.coursemanagementserver.exceptions.BadRequestException;
import com.aptech.coursemanagementserver.models.Course;
import com.aptech.coursemanagementserver.repositories.CourseRepository;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CheckoutService {
    // private final RestTemplate restTemplate;
    private final CourseRepository courseRepository;
    private final PaypalService service;
    private final MomoService momoService;

    public ResponseEntity<?> checkoutPayment(CheckoutDto checkoutDto) throws Exception {
        Course course = courseRepository.findById(checkoutDto.getCourseId()).orElseThrow(
                () -> new NoSuchElementException(
                        "This course with courseId: [" + checkoutDto.getCourseId() + "] is not exist."));

        if (course.getStatus() == 0) {
            throw new BadRequestException("This course 've already deactivated.");
        }

        if (checkoutDto.getPaymentType() == PaymentType.PAYPAL) {
            PaypalRequestDto paypalRequestDto = new PaypalRequestDto();
            paypalRequestDto.setCourseId(checkoutDto.getCourseId());
            paypalRequestDto.setUserId(checkoutDto.getUserId());
            paypalRequestDto.setPrice(checkoutDto.getAmount());
            paypalRequestDto.setUserDescription(checkoutDto.getUserDescription());
            PaypalResponseDto response = paypalCheckout(paypalRequestDto);
            // restTemplate.postForObject(PAYPAL_CHECKOUT_API, paypalRequestDto,
            // PaypalResponseDto.class);
            return ResponseEntity.ok(response);
        }

        MomoRequestDto MomoRequestDto = new MomoRequestDto();
        MomoRequestDto.setCourseId(checkoutDto.getCourseId());
        MomoRequestDto.setUserId(checkoutDto.getUserId());
        MomoRequestDto.setAmount(checkoutDto.getAmount());
        MomoRequestDto.setUserDescription(checkoutDto.getUserDescription());
        MomoResponseDto response = momoCheckout(MomoRequestDto);
        return ResponseEntity.ok(response);
    }

    private PaypalResponseDto paypalCheckout(PaypalRequestDto paypalRequestDto) throws PayPalRESTException {
        Payment payment = service.createPayment(paypalRequestDto, PAYPAL_CANCEL_API,
                PAYPAL_SUCCESS_API);

        for (Links link : payment.getLinks()) {
            if (link.getRel().equals("approval_url")) {
                PaypalResponseDto response = new PaypalResponseDto();
                response.setPayUrl(link.getHref());
                return response;
            }
        }
        throw new BadRequestException(GLOBAL_EXCEPTION);
    }

    private MomoResponseDto momoCheckout(MomoRequestDto momoRequestDto) throws Exception {
        MomoResponseDto response = momoService.initPaymentMomo(momoRequestDto);
        // if (response.getResultCode() == 0 && response.getPayUrl().length() > 0) {
        // return
        // ResponseEntity.status(HttpStatus.FOUND).location(URI.create(response.getPayUrl()))
        // .build();
        // }
        if (response.getResultCode() == 0 && response.getPayUrl().length() > 0) {
            return response;
        }
        throw new BadRequestException(GLOBAL_EXCEPTION);
    }
}
