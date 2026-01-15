package AplikacjePrzemyslowe.DatApp.controller;

import AplikacjePrzemyslowe.DatApp.dto.response.CandidateResponse;
import AplikacjePrzemyslowe.DatApp.service.MatchingEngineService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Kontroler dla kandydatów (matching algorithm).
 */
@RestController
@RequestMapping("/api/v1/candidates")
@RequiredArgsConstructor
public class CandidateController {

    private final MatchingEngineService matchingEngineService;

    @Operation(summary = "Get next candidate for user")
    @GetMapping("/{userId}/next")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CandidateResponse> getNextCandidate(@PathVariable Long userId) {
        CandidateResponse candidate = matchingEngineService.getNextCandidate(userId);

        if (candidate == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(candidate);
    }
}

