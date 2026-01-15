package AplikacjePrzemyslowe.DatApp.controller;

import AplikacjePrzemyslowe.DatApp.dto.request.PreferenceRequest;
import AplikacjePrzemyslowe.DatApp.dto.response.PreferenceResponse;
import AplikacjePrzemyslowe.DatApp.service.PreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/preferences")
@RequiredArgsConstructor
public class PreferenceController {

    private final PreferenceService preferenceService;

    @Operation(summary = "Get preferences for user")
    @GetMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PreferenceResponse> getPreferences(@PathVariable Long userId) {
        return ResponseEntity.ok(preferenceService.getPreferences(userId));
    }

    @Operation(summary = "Update preferences for user")
    @PutMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PreferenceResponse> updatePreferences(@PathVariable Long userId,
                                                                @Validated @RequestBody PreferenceRequest request) {
        return ResponseEntity.ok(preferenceService.setPreferences(userId, request));
    }
}

