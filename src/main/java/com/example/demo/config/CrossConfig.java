package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CrossConfig {
    @Bean
    public WebMvcConfigurer webMvcConfigurer(){
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:3000")
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE")
                        .allowedHeaders("Authorization", "Content-Type", "Accept")
                        .allowCredentials(true);
            }
        };
    }
//                @Bean
//                CorsConfigurationSource corsConfigurationSource() {
//                    CorsConfiguration configuration = new CorsConfiguration();
//                    configuration.setAllowedOrigins(List.of("http://localhost:3000"));
//                    configuration.setAllowedMethods(Arrays.asList("GET","POST","PUT","DELETE"));
//                    configuration.setAllowedHeaders(List.of("Authorization"));
//                    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//                    source.registerCorsConfiguration("/**", configuration);
//                    return source;
//                }
}
