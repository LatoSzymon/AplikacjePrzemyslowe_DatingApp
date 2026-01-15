package AplikacjePrzemyslowe.DatApp.controller;

import AplikacjePrzemyslowe.DatApp.dto.request.LoginRequest;
import AplikacjePrzemyslowe.DatApp.dto.request.RegisterRequest;
import AplikacjePrzemyslowe.DatApp.dto.response.AuthResponse;
import AplikacjePrzemyslowe.DatApp.dto.response.UserResponse;
import AplikacjePrzemyslowe.DatApp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @Operation(summary = "Register new user")
    @PostMapping("/register")
    @PreAuthorize("permitAll()")
    public ResponseEntity<UserResponse> register(@Validated @RequestBody RegisterRequest request) {
        UserResponse created = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Login user (placeholder)")
    @PostMapping("/login")
    @PreAuthorize("permitAll()")
    public ResponseEntity<AuthResponse> login(@Validated @RequestBody LoginRequest request) {
        // TODO: Implement real auth once Security (Step 13) is ready
        AuthResponse response = AuthResponse.builder()
                .accessToken("dummy-token")
                .expiresIn(3600L)
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Logout user (placeholder)")
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logout() {
        // TODO: Invalidate token/session in Step 13
        return ResponseEntity.noContent().build();
    }
}
