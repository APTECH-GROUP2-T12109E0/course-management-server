package com.aptech.coursemanagementserver.constants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GlobalStorageConfig {
    
    @Value("${domain.client}")
    private String clientURL;
    
    @Value("${domain.api}")
    private String apiURL;
    
    public String getClientURL() {
        return clientURL;
    }

    public String getApiURL() {
        return apiURL;
    }
}
