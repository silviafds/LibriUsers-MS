package com.libriusers.demo.adapters.in.web.controller;

import com.libriusers.demo.adapters.in.web.dto.request.AuthenticationRequest;
import com.libriusers.demo.adapters.in.web.dto.request.UserRequest;
import com.libriusers.demo.adapters.in.web.dto.response.LogoutResponse;
import com.libriusers.demo.adapters.in.web.dto.response.RegisterResponse;
import com.libriusers.demo.application.service.TokenService;
import com.libriusers.demo.application.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private AuthenticationManager authenticationManager;

    private final UserService userService;

    public AuthController(TokenService tokenService, UserService userService) {
        this.tokenService = tokenService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid AuthenticationRequest data) {
        return userService.login(data);
    }

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<RegisterResponse> register(@RequestBody @Valid UserRequest data) {
        return userService.registerUser(data);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            // Obtenha o token do cabeçalho da autorização
            String token = extractTokenFromHeader(request);

            // Verifica se o token é válido
            if (token == null || tokenService.isTokenBlacklisted(token)) {
                LogoutResponse errorResponse = new LogoutResponse(
                        "401",
                        "Falha ao sair do sistema",
                        "Token inválido ou já invalidado."
                );
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            // Adicione o token à lista negra para invalidá-lo
            tokenService.addToBlacklist(token);

            // Retorna sucesso
            LogoutResponse successResponse = new LogoutResponse(
                    "200",
                    "Saindo do sistema",
                    "Usuário deslogado com sucesso");
            return ResponseEntity.ok(successResponse);

        } catch (Exception e) {
            // Captura exceções genéricas e retorna erro 500
            LogoutResponse errorResponse = new LogoutResponse(
                    "500",
                    "Erro interno no servidor",
                    "Um erro inesperado aconteceu no servidor: "
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    private String extractTokenFromHeader(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // O token JWT começa após "Bearer "
        }
        return null;
    }

    @PostMapping("/login2")
    public String login2() {
        return "funciona";
    }

}
