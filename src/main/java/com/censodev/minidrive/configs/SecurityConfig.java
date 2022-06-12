package com.censodev.minidrive.configs;

import censodev.lib.auth.utils.jwt.JwtAuthenticationFilter;
import censodev.lib.auth.utils.jwt.TokenProvider;
import com.censodev.minidrive.data.domains.User;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .cors()
                .and()
                .addFilterBefore(new JwtAuthenticationFilter<>(tokenProvider(), User.class), UsernamePasswordAuthenticationFilter.class)
                .authorizeRequests()
                .antMatchers("/api/auth/login", "/api/auth/register").permitAll()
                .antMatchers(HttpMethod.GET, "/api/drive/file/{\\w+}").permitAll()
                .anyRequest().authenticated();
    }

    @Bean
    public TokenProvider tokenProvider() {
        return TokenProvider.builder()
                .header("Authorization")
                .prefix("Bearer ")
                .secret("qwertyuiopasdfghjklzxcvbnm1!2@3#4$5%6^7&8*9(0)-_=+")
                .expiration(86_400_000)
                .build();
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