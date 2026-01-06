package com.libriusers.demo.infraestructure.security;

import com.libriusers.demo.application.ports.out.repository.UserRepositoryImpl;
import com.libriusers.demo.application.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    @Autowired
    TokenService tokenService;

    @Autowired
    UserRepositoryImpl userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println("Request URI: " + request.getRequestURI());
        System.out.println("Method: " + request.getMethod());

        var token = this.recoverToken(request);
        System.out.println("token doFilterInternal: " + token);

        // Se houver token, valide-o
        if (token != null && !token.isEmpty()) {

            // Verifique se o token está na lista negra
            if (tokenService.isTokenBlacklisted(token)) {
                System.out.println("Token está na lista negra");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or blacklisted token");
                return;
            }

            var email = tokenService.validateToken(token);
            System.out.println("login retornado da validação de token: " + email);

            // Se o email não for vazio (token válido)
            if (email != null && !email.isEmpty()) {
                UserDetails user = userRepository.findByEmail(email);

                if (user != null) {
                    var authentication = new UsernamePasswordAuthenticationToken(
                            user, null, user.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    System.out.println("Usuário não encontrado para o email: " + email);
                }
            } else {
                System.out.println("Token inválido ou expirado");
                // Token inválido, mas permita que a requisição continue (para endpoints públicos)
                // Ou retorne erro 401 se quiser bloquear
                // response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                // return;
            }
        }

        // Continue com a cadeia de filtros
        filterChain.doFilter(request, response);
    }

    private String recoverToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.replace("Bearer ", "");
    }

}
