package com.censodev.minidrive.http.filters;

import io.github.censodev.jwtprovider.CanAuth;
import io.github.censodev.jwtprovider.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class AuthFilter<T extends CanAuth> implements Filter {
    private final JwtProvider tokenProvider;
    private final Class<T> canAuthConcreteClass;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String header = ((HttpServletRequest) request).getHeader(HttpHeaders.AUTHORIZATION);

        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.replace("Bearer ", "");
        try {
            tokenProvider.verify(token);
            T canAuthConcrete = tokenProvider.getCredential(token, canAuthConcreteClass);
            List<SimpleGrantedAuthority> authorities = canAuthConcrete
                    .authorities()
                    .stream().map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
            Object principle = canAuthConcrete.subject();
            Authentication auth = new UsernamePasswordAuthenticationToken(principle, canAuthConcrete, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(request, response);
    }
}
