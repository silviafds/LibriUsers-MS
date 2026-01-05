package com.libriusers.demo.application.service;

import com.libriusers.demo.adapters.in.web.dto.request.AuthenticationRequest;
import com.libriusers.demo.adapters.in.web.dto.request.LoginResponse;
import com.libriusers.demo.adapters.in.web.dto.request.UserRequest;
import com.libriusers.demo.adapters.in.web.dto.response.LoginResponses;
import com.libriusers.demo.adapters.in.web.dto.response.RegisterResponse;
import com.libriusers.demo.application.ports.out.repository.UserRepositoryImpl;
import com.libriusers.demo.domain.enums.CodeMessageHttp;
import com.libriusers.demo.domain.enums.Role;
import com.libriusers.demo.domain.model.User;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepositoryImpl userRepositoryImpl;

    private final AuthenticationManager authenticationManager;

    private final TokenService tokenService;

    public UserService(UserRepositoryImpl userRepositoryImpl, AuthenticationManager authenticationManager, TokenService tokenService) {
        this.userRepositoryImpl = userRepositoryImpl;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    public ResponseEntity<?> login(AuthenticationRequest data) {
        try {
            var usernamePassword = new UsernamePasswordAuthenticationToken(data.login(), data.password());
            var auth = this.authenticationManager.authenticate(usernamePassword);

            var token = tokenService.generateToken((User) auth.getPrincipal());


            // Retorna o token
            return ResponseEntity.ok(new LoginResponse(token));

        } catch (BadCredentialsException | InternalAuthenticationServiceException e) {
            LoginResponses errorResponse = new LoginResponses(
                    CodeMessageHttp.QUATROCENTOS.getCodeError(),
                    "Login e/ou senha inválido.",
                    "Corrija o login"
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);

        } catch (Exception e) {
            LoginResponses errorResponse = new LoginResponses(
                    CodeMessageHttp.QUINHENTOS.getCodeError(),
                    "Erro interno no servidor",
                    "Um erro inesperado aconteceu no servidor: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Transactional
    public ResponseEntity<RegisterResponse> registerUser(UserRequest data) {
        try {
            Role roleConvertido = Role.fromString(data.getRole());

            if (this.userRepositoryImpl.findByEmail(data.getEmail()) != null) {
                RegisterResponse registerResponse = new RegisterResponse(
                        CodeMessageHttp.QUATROCENTOS.getCodeError(),
                        "Falha no cadastro do usuário",
                        "Login já existe no sistema.");

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(registerResponse);
            }

            String encryptedPassword = new BCryptPasswordEncoder().encode(data.getPassword());

            User newUser = new User(data.getName(), data.getEmail(), encryptedPassword, roleConvertido);

            this.userRepositoryImpl.save(newUser);

            RegisterResponse registerResponse = new RegisterResponse(
                    CodeMessageHttp.DUZENTOS.getCodeError(),
                    "Usuário cadastrado no sistema",
                    "Registro realizado com sucesso");
            return ResponseEntity.ok().body(registerResponse);
        } catch (Exception e) {
            RegisterResponse registerResponse = new RegisterResponse(
                    CodeMessageHttp.QUINHENTOS.getCodeError(),
                    "Erro interno no servidor",
                    "Um erro inesperado aconteceu no servidor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(registerResponse);
        }
    }

}
