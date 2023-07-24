package com.aptech.coursemanagementserver.services.paymentServices;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.aptech.coursemanagementserver.dtos.payment.MomoRequestDto;
import com.aptech.coursemanagementserver.dtos.payment.MomoResponseDto;
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
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static com.aptech.coursemanagementserver.constants.GlobalStorage.MOMO_REDIRECT_API;

@Component
@RequiredArgsConstructor
@Slf4j
public class MomoService {
    private final ObjectMapper objectMapper;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final OrdersRepository ordersRepository;
    private final EnrollmentRepository enrollmentRepository;

    private final HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

    private final String accessKey = "mTCKt9W3eU1m39TW";
    private final String partnerCode = "MOMOLRJZ20181206";
    private final String secretKey = "SetA5RDnLHvt51AULf51DyauxUo3kDU6";
    private final String endpoint = "https://test-payment.momo.vn/v2/gateway/api/create";

    // Business Account
    // private final String accessKey = "SvDmj2cOTYZmQQ3H";
    // private final String partnerCode = "MOMOIQA420180417";
    // private final String secretKey = "PPuDXq1KowPT1ftR8DvlQTHhC03aul17";
    // private final String endpoint =
    // "https://test-payment.momo.vn/gw_payment/transactionProcessor";

    public MomoResponseDto initPaymentMomo(MomoRequestDto momoRequestDto) throws Exception {
        Course course = courseRepository.findById(momoRequestDto.getCourseId()).get();
        User user = userRepository.findById(momoRequestDto.getUserId()).get();
        Orders order = new Orders();
        order.setUser(user).setCourse(course)
                .setName(course.getName())
                .setDescription(course.getDescription())
                .setUserDescription(momoRequestDto.getUserDescription())
                .setDuration(course.getDuration())
                .setPrice(course.getPrice())
                .setNet_price(course.getNet_price())
                .setImage(course.getImage())
                .setPayment(PaymentType.MOMO).setStatus(OrderStatus.PROCESSING);
        ordersRepository.save(order);

        // set the necessary parameters for the payment request
        String requestId = String.valueOf(System.currentTimeMillis());
        String orderId = String.valueOf(System.currentTimeMillis());
        String orderInfo = "Payment with MoMo";
        String redirectUrl = MOMO_REDIRECT_API;

        String ipnUrl = MOMO_REDIRECT_API;
        String extraData = String.valueOf(order.getId());

        // set the parameters on the payment request
        momoRequestDto.setPartnerCode(partnerCode);
        momoRequestDto.setRequestId(requestId);
        momoRequestDto.setOrderId(orderId);

        long amount;
        if (course.getNet_price() == 0) {
            amount = Math.round(course.getPrice() * 23000);
        } else {
            amount = Math.round(course.getNet_price() * 23000);
        }
        momoRequestDto.setAmount(amount);
        // momoRequestDto.setAmount(Math.round(course.getNet_price() * 23000));
        momoRequestDto.setLang(momoRequestDto.getLang());
        momoRequestDto.setOrderInfo(orderInfo);
        momoRequestDto.setRedirectUrl(redirectUrl);
        momoRequestDto.setIpnUrl(ipnUrl);
        momoRequestDto.setRequestType(momoRequestDto.getRequestType());
        momoRequestDto.setExtraData(extraData);

        // generate the signature for the request
        String rawHash = "accessKey=" + accessKey
                + "&amount=" + momoRequestDto.getAmount()
                + "&extraData=" + extraData
                + "&ipnUrl=" + ipnUrl
                + "&orderId=" + orderId
                + "&orderInfo=" + orderInfo
                + "&partnerCode=" + partnerCode
                + "&redirectUrl=" + redirectUrl
                + "&requestId=" + requestId
                + "&requestType=" + momoRequestDto.getRequestType();
        log.info("rawData: {}", rawHash);

        String signature = signHmacSHA256(rawHash, secretKey);
        log.info("signature: {}", signature);

        momoRequestDto.setSignature(signature);

        var payload = objectMapper.writeValueAsString(momoRequestDto);
        // create a new HTTP client instance
        var request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();
        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        var content = response.body();
        return objectMapper.readValue(content, MomoResponseDto.class);
    }

    public void UpdateOrderAndCreateEnroll(int resultCode, String extraData, String orderId) {

        Orders order = ordersRepository.findById(Long.valueOf(extraData)).get();
        if (resultCode == 0 || resultCode == 9000) {
            order.setTransactionId(orderId);
            order.setStatus(OrderStatus.COMPLETED);
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

    private static final String HMAC_SHA256 = "HmacSHA256";

    static String signHmacSHA256(String data, String secretKey)
            throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), HMAC_SHA256);
        Mac mac = Mac.getInstance(HMAC_SHA256);
        mac.init(secretKeySpec);
        byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return toHexString(rawHmac);
    }

    private static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        try (Formatter formatter = new Formatter(sb)) {
            for (byte b : bytes) {
                formatter.format("%02x", b);
            }
        }
        return sb.toString();
    }
}