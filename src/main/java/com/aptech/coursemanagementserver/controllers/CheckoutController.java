package com.aptech.coursemanagementserver.controllers;

import static com.aptech.coursemanagementserver.constants.GlobalStorage.GLOBAL_EXCEPTION;
import static com.aptech.coursemanagementserver.constants.GlobalStorage.MOMO_REDIRECT_URL;
import static com.aptech.coursemanagementserver.constants.GlobalStorage.PAYMENT_CANCEL_CLIENT;
import static com.aptech.coursemanagementserver.constants.GlobalStorage.PAYMENT_SUCCESS_CLIENT;
import static com.aptech.coursemanagementserver.constants.GlobalStorage.PAYPAL_CANCEL_API;
import static com.aptech.coursemanagementserver.constants.GlobalStorage.PAYPAL_CANCEL_URL;
import static com.aptech.coursemanagementserver.constants.GlobalStorage.PAYPAL_SUCCESS_API;
import static com.aptech.coursemanagementserver.constants.GlobalStorage.PAYPAL_SUCCESS_URL;
import com.aptech.coursemanagementserver.constants.GlobalStorageConfig;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import com.aptech.coursemanagementserver.dtos.OrderDto;
import com.aptech.coursemanagementserver.dtos.payment.CheckoutDto;
import com.aptech.coursemanagementserver.dtos.payment.MomoRequestDto;
import com.aptech.coursemanagementserver.dtos.payment.MomoResponseDto;
import com.aptech.coursemanagementserver.dtos.payment.PaypalRequestDto;
import com.aptech.coursemanagementserver.dtos.payment.PaypalResponseDto;
import com.aptech.coursemanagementserver.exceptions.BadRequestException;
import com.aptech.coursemanagementserver.services.OrderService;
import com.aptech.coursemanagementserver.services.paymentServices.CheckoutService;
import com.aptech.coursemanagementserver.services.paymentServices.MomoService;
import com.aptech.coursemanagementserver.services.paymentServices.PaypalService;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/checkout")
@Tag(name = "Checkout Payment Endpoints")
public class CheckoutController {
    private final CheckoutService checkoutService;
    private final MomoService momoService;
    private final PaypalService service;
    private final OrderService orderService;
    private final GlobalStorageConfig globalStorageConfig;

    @PostMapping()
    @Operation(summary = "[USER] - Initiate payment")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<?> checkout(@RequestBody CheckoutDto checkoutDto)
            throws Exception {
        try {
            return ResponseEntity.ok(checkoutService.checkoutPayment(checkoutDto));
        } catch (Exception e) {
            throw new BadRequestException(GLOBAL_EXCEPTION);
        }

    }

    @PostMapping(path = ("/momo"))
    @Operation(summary = "[USER] - Initiate payment MOMO")
    // @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<MomoResponseDto> initiatePaymentMomo(@RequestBody MomoRequestDto momoRequestDto)
            throws Exception {
        try {
            MomoResponseDto response = momoService.initPaymentMomo(momoRequestDto);
            // if (response.getResultCode() == 0 && response.getPayUrl().length() > 0) {
            // return
            // ResponseEntity.status(HttpStatus.FOUND).location(URI.create(response.getPayUrl()))
            // .build();
            // }
            if (response.getResultCode() == 0 && response.getPayUrl().length() > 0) {
                return ResponseEntity.ok(response);
            }
            throw new BadRequestException(GLOBAL_EXCEPTION);
        } catch (Exception e) {
            throw new BadRequestException(GLOBAL_EXCEPTION);
        }

    }

    @GetMapping(path = MOMO_REDIRECT_URL)
    @Operation(summary = "[ANORNYMOUS] - Redirect from MOMO")
    @PreAuthorize("permitAll()")
    public RedirectView redirect(@RequestParam int resultCode, @RequestParam String extraData,
            @RequestParam String orderId) {
        momoService.UpdateOrderAndCreateEnroll(resultCode, extraData, orderId);
        if (resultCode == 0 || resultCode == 9000) {
            return new RedirectView(globalStorageConfig.getClientURL() + PAYMENT_SUCCESS_CLIENT + "?transactionId=" + orderId);
        }
        // 1001: Giao dịch thanh toán thất bại do tài khoản người dùng không đủ tiền.
        // 1005: Giao dịch thất bại do url hoặc QR code đã hết hạn.
        // 1006: Giao dịch thất bại do người dùng đã từ chối xác nhận thanh toán.
        if (resultCode == 1001) {
            // momoService.UpdateOrderAndCreateEnroll(resultCode, extraData);
            return new RedirectView(globalStorageConfig.getClientURL() + PAYMENT_CANCEL_CLIENT + "?resultCode=1001");
        } else if (resultCode == 1005) {
            // momoService.UpdateOrderAndCreateEnroll(resultCode, extraData);
            return new RedirectView(globalStorageConfig.getClientURL() + PAYMENT_CANCEL_CLIENT + "?resultCode=1005");
        } else if (resultCode == 1006) {
            // momoService.UpdateOrderAndCreateEnroll(resultCode, extraData);
            return new RedirectView(globalStorageConfig.getClientURL() + PAYMENT_CANCEL_CLIENT + "?resultCode=1006");
        } else {
            resultCode = 9999;
            momoService.UpdateOrderAndCreateEnroll(resultCode, extraData, orderId);
            return new RedirectView(globalStorageConfig.getClientURL() + PAYMENT_CANCEL_CLIENT);
        }

    }

    // -------------- PAYPAL --------------

    @PostMapping(path = "/paypal")
    @Operation(summary = "[USER] - Initiate payment PAYPAL")
    // @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<PaypalResponseDto> payment(@RequestBody PaypalRequestDto dto) {
        try {
            Payment payment = service.createPayment(dto, globalStorageConfig.getApiURL() + PAYPAL_CANCEL_API,
                    globalStorageConfig.getApiURL() + PAYPAL_SUCCESS_API);

            for (Links link : payment.getLinks()) {
                if (link.getRel().equals("approval_url")) {
                    PaypalResponseDto response = new PaypalResponseDto();
                    response.setPayUrl(link.getHref());
                    return ResponseEntity.ok(response);
                }
            }
            // email: sb-c9mcj26117776@personal.example.com
            // password: e@LbdH4n
            throw new BadRequestException(GLOBAL_EXCEPTION);
        } catch (PayPalRESTException e) {
            e.printStackTrace();
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            throw new BadRequestException(GLOBAL_EXCEPTION);
        }

    }

    @GetMapping(path = PAYPAL_CANCEL_URL)
    @Operation(summary = "[ANORNYMOUS] - Redirect from PAYPAL")
    @PreAuthorize("permitAll()")
    public RedirectView cancelPay() {
        return new RedirectView(globalStorageConfig.getClientURL() + PAYMENT_CANCEL_CLIENT);
    }

    @GetMapping(path = PAYPAL_SUCCESS_URL)
    @Operation(summary = "[ANORNYMOUS] - Redirect from PAYPAL")
    @PreAuthorize("permitAll()")
    public RedirectView successPay(@RequestParam("paymentId") String paymentId,
            @RequestParam("PayerID") String payerId) {
        try {
            Payment payment = service.executePayment(paymentId, payerId);
            System.out.println(payment.toJSON());
            boolean isApproved = payment.getState().equals("approved");
            service.updateOrderAndCreateEnroll(payment, isApproved);
            if (isApproved) {
                return new RedirectView(globalStorageConfig.getClientURL() + PAYMENT_SUCCESS_CLIENT + "?transactionId=" + payment.getId());
            }
            return new RedirectView(globalStorageConfig.getClientURL() + PAYMENT_CANCEL_CLIENT);
        } catch (PayPalRESTException e) {
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            throw new BadRequestException(GLOBAL_EXCEPTION);
        }
    }

    @GetMapping(path = "/details/{transactionId}")
    @Operation(summary = "[USER] - Get Checkout Detail")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<OrderDto> getCheckoutDetail(@PathVariable String transactionId) {
        try {
            return ResponseEntity.ok(orderService.findByTransactionId(transactionId));
        } catch (Exception e) {
            throw new BadRequestException(GLOBAL_EXCEPTION);
        }
    }
}
