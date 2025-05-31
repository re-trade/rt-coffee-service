package org.retrade.voucher.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Value("${server.port}")
    private String port;
    private final HostConfig hostConfig;
    @Bean
    public OpenAPI openAPI() {
        List<Server> serverList = getServerList();

        Contact contact = new Contact()
                .name("ReTrade Development Team")
                .email("dev@retrade.com")
                .url("https://retrades.trade");

        License license = new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");

        Info info = new Info()
                .title("ReTrade API")
                .version("1.0.0")
                .description("ReTrade E-commerce Platform API Documentation\n\n" +
                        "## Authentication\n" +
                        "This API supports multiple authentication methods:\n" +
                        "1. **Bearer Token**: Use the `Authorization` header with `Bearer <token>`\n" +
                        "2. **Cookie Authentication**: Use the `ACCESS_TOKEN` cookie\n\n" +
                        "## Getting Started\n" +
                        "1. Register a new account using `/registers/customers/account`\n" +
                        "2. Login using `/auth/login` to get your access token\n" +
                        "3. Use the token in the Authorization header or cookie for protected endpoints")
                .contact(contact)
                .license(license);

        Components components = new Components()
                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT Bearer Token Authentication\n\n" +
                                "Enter your JWT token in the format: `your-jwt-token`\n" +
                                "You can get this token from the `/auth/login` endpoint."))
                .addSecuritySchemes("cookieAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.COOKIE)
                        .name("ACCESS_TOKEN")
                        .description("Cookie-based Authentication\n\n" +
                                "The ACCESS_TOKEN cookie is automatically set after successful login."));

        List<SecurityRequirement> securityRequirements = new ArrayList<>();
        securityRequirements.add(new SecurityRequirement().addList("bearerAuth"));
        securityRequirements.add(new SecurityRequirement().addList("cookieAuth"));

        return new OpenAPI()
                .info(info)
                .servers(serverList)
                .components(components)
                .security(securityRequirements);
    }

    private List<Server> getServerList() {
        List<Server> serverList = new ArrayList<>();
        if (contextPath.equals("/")) {
            var productionServer = new Server();
            productionServer.setUrl(String.format("https://%s%s", hostConfig.getBaseHost(), hostConfig.getSwaggerContextPath()));
            productionServer.setDescription("Production Server");
            serverList.add(productionServer);
            return serverList;
        }
        var localServer = new Server();
        localServer.setUrl(String.format("http://localhost:%s%s", port, contextPath));
        localServer.setDescription("Local Development Server");
        serverList.add(localServer);
        return serverList;
    }
}
