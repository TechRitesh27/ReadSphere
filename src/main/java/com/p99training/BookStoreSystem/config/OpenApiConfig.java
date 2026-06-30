package com.p99training.BookStoreSystem.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BASIC_AUTH = "basicAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("BookStore System API")
                        .description("""
                                REST API for managing a bookstore inventory.
                                
                                **Features:**
                                - Browse, search, filter, and paginate books
                                - Add, update, and delete books
                                - Inventory reports
                                - Sorting by title, author, price, year, quantity
                                
                                **Auth:** DELETE /books/{id} requires Basic Auth with admin credentials.
                                Use the Authorize button (🔒) to set credentials before calling DELETE.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("p99training")
                        )
                )
                // Register HTTP Basic as a named security scheme
                .components(new Components()
                        .addSecuritySchemes(BASIC_AUTH, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("basic")
                                .description("Admin credentials required for DELETE /books/{id}. " +
                                        "Username: admin | Password: admin123")
                        )
                );
        // Note: security is applied per-operation on the DELETE endpoint only,
        // not globally here — so other endpoints show no lock icon
    }
}
