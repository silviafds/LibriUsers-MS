package com.libriusers.demo.adapters.in.web.controller;

import com.libriusers.demo.adapters.in.web.dto.request.AuthenticationRequest;
import com.libriusers.demo.adapters.in.web.dto.request.UserRequest;
import com.libriusers.demo.adapters.in.web.dto.response.LogoutResponse;
import com.libriusers.demo.adapters.in.web.dto.response.RegisterResponse;
import com.libriusers.demo.application.service.AuthorizationService;
import com.libriusers.demo.application.service.TokenService;
import com.libriusers.demo.application.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
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

    @Operation(summary = "Authenticate user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Returns token."),
                    @ApiResponse(responseCode = "400", description = "Login failed."),
                    @ApiResponse(responseCode = "500", description = "Internal server error"),
            })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid AuthenticationRequest data) {
        return userService.login(data);
    }

    @Operation(summary = "Register new user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User registered in the system"),
                    @ApiResponse(responseCode = "400", description = "User registration failed"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            })
    @PostMapping("/register")
    @Transactional
    public ResponseEntity<RegisterResponse> register(@RequestBody @Valid UserRequest data) {
        return userService.registerUser(data);
    }

    @Operation(summary = "Log out of the system.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User logged out of the system"),
                    @ApiResponse(responseCode = "401", description = "Failed to log out of the system"),
                    @ApiResponse(responseCode = "500", description = "Internal server failure"),
            })
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @Parameter(description = "Bearer token for authentication",
                    required = true,
                    example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            @RequestHeader(value = "Authorization", required = false) String request) {
        try {
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

            LogoutResponse successResponse = new LogoutResponse(
                    "200",
                    "Saindo do sistema",
                    "Usuário deslogado com sucesso");
            return ResponseEntity.ok(successResponse);

        } catch (Exception e) {
            LogoutResponse errorResponse = new LogoutResponse(
                    "500",
                    "Erro interno no servidor",
                    "Um erro inesperado aconteceu no servidor: "
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Operation(summary = "Search user for id",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Search user in the system for id"
                    ),
                    @ApiResponse(responseCode = "400", description = "Search user failed"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            })
    @GetMapping("/search-user/{id}")
    public String searchUserForId(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String tokenAuth) {
        System.out.println("ID RECEBIDO PARA BUSCA: "+ id);
        return userService.searchUserPerId(id);
    }

    @Operation(summary = "Validate if token is not blacklisted",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Returns true if token is valid, false if blacklisted"),
                    @ApiResponse(responseCode = "400", description = "Invalid token format"),
                    @ApiResponse(responseCode = "500", description = "Internal server error"),
            })
    @GetMapping("/validate-token")
    public ResponseEntity<Boolean> validateToken(@RequestParam String token) {
        try {
            boolean isBlacklisted = tokenService.isTokenBlacklisted(token);

            if (isBlacklisted) {
                System.out.println("❌ Token is BLACKLISTED");
                return ResponseEntity.ok(false);
            }

            String email = tokenService.validateToken(token);
            boolean isValid = email != null && !email.isEmpty();

            System.out.println("Email from token: " + (email != null ? email : "null"));
            System.out.println("Is token valid? " + isValid);
            System.out.println("=== END VALIDATION ===");

            return ResponseEntity.ok(isValid);

        } catch (Exception e) {
            System.out.println("❌ ERROR validating token: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(false);
        }
    }

    private String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }

        return null;
    }

}
