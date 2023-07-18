package com.aptech.coursemanagementserver.services.paymentServices;

import static com.aptech.coursemanagementserver.constants.GlobalStorage.GLOBAL_EXCEPTION;
import static com.aptech.coursemanagementserver.constants.GlobalStorage.PAYPAL_CANCEL_API;
import static com.aptech.coursemanagementserver.constants.GlobalStorage.PAYPAL_SUCCESS_API;
import com.aptech.coursemanagementserver.constants.GlobalStorageConfig;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.aptech.coursemanagementserver.dtos.payment.CheckoutDto;
import com.aptech.coursemanagementserver.dtos.payment.MomoRequestDto;
import com.aptech.coursemanagementserver.dtos.payment.MomoResponseDto;
import com.aptech.coursemanagementserver.dtos.payment.PaypalRequestDto;
import com.aptech.coursemanagementserver.dtos.payment.PaypalResponseDto;
import com.aptech.coursemanagementserver.enums.payment.PaymentType;
import com.aptech.coursemanagementserver.exceptions.BadRequestException;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CheckoutService {
    // private final RestTemplate restTemplate;
    private final PaypalService service;
    private final MomoService momoService;
    private GlobalStorageConfig globalStorageConfig;

    public ResponseEntity<?> checkoutPayment(CheckoutDto checkoutDto) throws Exception {
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
        Payment payment = service.createPayment(paypalRequestDto, globalStorageConfig.getApiURL() + PAYPAL_CANCEL_API,
                globalStorageConfig.getApiURL() + PAYPAL_SUCCESS_API);

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
