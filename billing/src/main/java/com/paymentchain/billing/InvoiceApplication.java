package com.paymentchain.billing;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;


@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class InvoiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InvoiceApplication.class, args);
	}
        
         @Bean
        public OpenAPI springShopOpenAPI() {
            return new OpenAPI()
                    .info(new Info().title("Billing API")
                    .description("API encargada de las facturas de paymentchainparent")
                    .version("v0.0.1")
                    .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                    .externalDocs(new ExternalDocumentation()
                    .description("SpringShop Wiki Documentation")
                    .url("https://springshop.wiki.github.org/docs"));
        }
}
