package com.aptech.coursemanagementserver.configs;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

import static com.aptech.coursemanagementserver.constants.GlobalStorage.DEV_DOMAIN_API;
import static com.aptech.coursemanagementserver.constants.GlobalStorage.PROD_DOMAIN_API;
import static com.aptech.coursemanagementserver.constants.GlobalStorage.DOMAIN_EMAIL;

//OpenAPI is an API description format for REST APIs use with
// info: Show info of Swagger include: name , email, url, description, title, version, license, terms of service
// server: url of Development Environment, Production Environment, ...
// security: add 
@OpenAPIDefinition(info = @Info(contact = @Contact(name = "ClickAndLearnProject", email = DOMAIN_EMAIL, url = PROD_DOMAIN_API), description = "OpenApi documentation for Spring Security", title = "OpenApi Specification - Nguyen Test ClickAndLearn", version = "1.0", license = @License(name = "Licences", url = "https://some-url.com"), termsOfService = "Terms of service"), servers = {
        @Server(description = "DEV ENV", url = DEV_DOMAIN_API),
        @Server(description = "PROD ENV", url = PROD_DOMAIN_API)
}, security = {
        @SecurityRequirement(name = "bearerAuth")
})

/*
 * HTTP authentication:
 * + BasicAuth: type HTTP, scheme Basic
 * + BearerAth: type HTTP, scheme Bearer
 * 
 * Other authentication:
 * + ApiKeyAuth
 * + OpenID
 * + OAuth2
 */

// To add OpenAPI security we need to add SecuritySCheme
// + name: explicit use in security property of OpenAPI
// + scheme: bearer -> Because we are using Bearer Authentication
// + type: {SecuritySchemeType.HTTP included:
// 1. HTTP authentication schemes use the "Authorization" Header,
// 2. Basic Authentication
// 3. Bearer Authentication (HTTP authentication schemes use Header
// Authorization: Bearer <token>)}
// + in: SecuritySchemeIn.HEADER inject this Token in Header
@SecurityScheme(name = "bearerAuth", description = "JWT Nguyen Test", scheme = "bearer", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", in = SecuritySchemeIn.HEADER)
public class OpenAPIConfiguration {

}
