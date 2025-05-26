package org.retrade.storage.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SwaggerConfig {
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Value("${server.port}")
    private String port;
    @Bean
    public OpenAPI openAPI() {
        List<Server> serverList = new ArrayList<>();
        var localServer = new Server();
        localServer.setUrl(String.format("http://localhost:%s%s", port, contextPath));
        serverList.add(localServer);
        Info info = new Info().title("Retrade API").version("1.0")
                .description("Retrade API Documentation");
        var openAPI =  new OpenAPI().info(info).components(new Components()
                .addSecuritySchemes("accessCookie", new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.COOKIE)
                        .name("ACCESS_TOKEN")
                ));
        openAPI.servers(serverList);
        return openAPI;
    };
}
