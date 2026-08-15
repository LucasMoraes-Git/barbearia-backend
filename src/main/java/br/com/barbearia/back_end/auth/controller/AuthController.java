package br.com.barbearia.back_end.auth.controller;

import br.com.barbearia.back_end.auth.dto.LoginRequest;
import br.com.barbearia.back_end.auth.dto.LoginResponse;
import br.com.barbearia.back_end.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService service;


    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request)
    {
        LoginResponse response = service.login(request);

        return ResponseEntity.ok(response);
    }
}
