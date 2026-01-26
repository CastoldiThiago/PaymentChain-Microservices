package com.paymentchain.billing.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private static final String[] NO_AUTH_LIST = {
            "/v3/api-docs",//
            "/configuration/ui", //
            "/swagger-resources", //
            "/configuration/security", //
            "/webjars/**", //
            "/login",
            "/swagger-ui/**",
            "/h2-console/**"};


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        //Choose one configuration

        //01- Full security in order to ask by user and password before to acces swagger ui
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .httpBasic(withDefaults())
                .formLogin(withDefaults());

        return http.build();



        //02- Custom security configuration, we can excluse some paths and ask by user and password before each request to acces swagger ui
//        http
//                /*- This property is active by default, we need to deactivate in order to allow ask by user and password on post methods.
//
//             - Cross site request forgery (CSRF or XSRF) refers to an attack that makes the end-user perform unwanted actions within a web application
//             that has already granted them authentication.
//
//            - The best option for protect is use a Token on each request, like  a JWT
//                 */
//                .csrf(AbstractHttpConfigurer::disable)
//                //Configure custom restrictions in order to ask by user and password
//                .authorizeHttpRequests((authz) -> authz
//                        .requestMatchers(NO_AUTH_LIST).permitAll()
//                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/billing/**").authenticated()
//                        //Using defauls values, we can define role on .properties file that will be set whne user is authetnticate
//                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/billing/**").hasRole("ADMIN")
//                )
//                //Use default credentials on .properties file
//                .httpBasic(withDefaults())
//                //use default UI.
//                .formLogin(withDefaults());
//        return http.build();
    }


    //This Handlers implement the CorsConfigurationSource interface in order to provide a CorsConfiguration for each request.
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cc = new CorsConfiguration();

        cc.setAllowedHeaders(Arrays.asList("Origin", "Accept", "X-Requested-With", "Content-Type", "Access-Control-Request-Method", "Access-Control-Request-Headers", "Authorization"));
        cc.setExposedHeaders(Arrays.asList("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"));

        cc.setAllowedOrigins(List.of("*"));

        cc.setAllowedMethods(Arrays.asList("GET", "POST", "OPTIONS", "PUT", "PATCH"));

        cc.addAllowedOriginPattern("*");


        cc.setMaxAge(Duration.ZERO);
        cc.setAllowCredentials(Boolean.TRUE);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cc);
        return source;
    }



}
