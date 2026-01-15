package AplikacjePrzemyslowe.DatApp.controller;

import AplikacjePrzemyslowe.DatApp.dto.request.SwipeRequest;
import AplikacjePrzemyslowe.DatApp.dto.response.SwipeResponse;
import AplikacjePrzemyslowe.DatApp.service.SwipeService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Kontroler dla swipe'ów (Like/Dislike).
 */
@RestController
@RequestMapping("/api/v1/swipes")
@RequiredArgsConstructor
public class SwipeController {

    private final SwipeService swipeService;

    @Operation(summary = "Create swipe (returns match if mutual like)")
    @PostMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SwipeResponse> createSwipe(@PathVariable Long userId,
                                                     @Validated @RequestBody SwipeRequest request) {
        SwipeResponse response = swipeService.recordSwipe(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

