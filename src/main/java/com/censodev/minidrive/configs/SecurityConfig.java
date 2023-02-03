package com.censodev.minidrive.configs;

import com.censodev.minidrive.data.domains.User;
import com.censodev.minidrive.http.filters.AuthFilter;
import io.github.censodev.jwtprovider.JwtProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtProvider tokenProvider) throws Exception {
        AuthFilter<User> filter = new AuthFilter<>(tokenProvider, User.class);
        return http
                .csrf()
                .disable()
                .cors()
                .and()
                .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .antMatchers("/api/auth/login", "/api/auth/register").permitAll()
                        .antMatchers(HttpMethod.GET, "/api/drive/file/{\\w+}").permitAll()
                        .antMatchers(HttpMethod.GET, "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(withDefaults())
                .build();
    }

    @Bean
    public JwtProvider tokenProvider(@Value("${auth.secret}") String authSecret) {
        return JwtProvider.secret(authSecret).build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public WebMvcConfigurer cors() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE");
            }
        };
    }
}