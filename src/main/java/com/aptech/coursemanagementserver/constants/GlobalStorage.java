package com.aptech.coursemanagementserver.constants;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public interface GlobalStorage {
    // import static com.aptech.coursemanagementserver.constants.GlobalStorage.*;

    // JWT
    String TOKEN_PREFIX = "Bearer ";
    String HEADER_STRING = "Authorization";

    // PATHS
    Path COURSE_PATH = Paths.get("assets", "images", "course");
    Path VIDEO_PATH = Paths.get("assets", "videos");
    Path CAPTION_PATH = Paths.get("assets", "captions");
    String VIDEO = "assets/videos";
    String CAPTION = "assets/captions";

    // DOMAIN EMAIL
    String DOMAIN_EMAIL = "contact@cmproj.com";

    // DOMAIN API URL
    String DEV_DOMAIN_API = "http://localhost:8080";
    String PROD_DOMAIN_API = "https://cmapi.com";

    // DOMAIN CLIENT URL
    String DEV_DOMAIN_CLIENT = "http://localhost:3000";
    String PROD_DOMAIN_CLIENT = "http://cmclient.com";

    // API
    String COURSE_DOWNLOAD_API = "http://localhost:8080/course/download/";
    String STREAM_API = "http://localhost:8080/video/stream/";
    String CAPTION_API = "http://localhost:8080/video/caption/";

    // CLIENT
    String PAYMENT_SUCCESS_CLIENT = DEV_DOMAIN_CLIENT + "/payment/success";
    String PAYMENT_CANCEL_CLIENT = DEV_DOMAIN_CLIENT + "/payment/cancel";

    // OAUTH2
    String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri";

    // BLOG
    String BLOG_EXISTED_EXCEPTION = "This title already exists, please change another !";

    // EXCEPTION
    String GLOBAL_EXCEPTION = "Something wrong. Please try again";
    String FETCHING_FAILED = "Fetch data failed!";
    String BAD_REQUEST_EXCEPTION = "Failed! Please check your infomation and try again.";
    String INVALID_TOKEN_EXCEPTION = "Failed! Token is not valid.";

    // FILE & VIDEO
    String CONTENT_TYPE = "Content-Type";
    String CONTENT_LENGTH = "Content-Length";
    String VIDEO_CONTENT = "video/";
    String VTT_CONTENT = "text/vtt";
    String CONTENT_RANGE = "Content-Range";
    String ACCEPT_RANGES = "Accept-Ranges";
    String BYTES = "bytes";
    int CHUNK_SIZE = 1048576;
    int BYTE_RANGE = 1024;

    // PAYPAL
    Map<String, String> PAYPAL_SANDBOX_MODE = Map.ofEntries(
            Map.entry("mode", "sandbox")
    // Map.entry("language","en_US"),
    // Map.entry("currency", "USD"),
    );
    String PAYPAL_CHECKOUT_API = DEV_DOMAIN_API + "/checkout/paypal";
    String PAYPAL_SUCCESS_URL = "/paypal/success";
    String PAYPAL_SUCCESS_API = DEV_DOMAIN_API + "/checkout/paypal/success";
    String PAYPAL_CANCEL_URL = "/paypal/cancel";
    String PAYPAL_CANCEL_API = DEV_DOMAIN_API + "/checkout/paypal/cancel";

    // MOMO
    String MOMO_CHECKOUT_API = DEV_DOMAIN_API + "/checkout/momo";
    String MOMO_REDIRECT_URL = "/momo/redirect";
    String MOMO_REDIRECT_API = DEV_DOMAIN_API + "/checkout/momo/redirect";
}
