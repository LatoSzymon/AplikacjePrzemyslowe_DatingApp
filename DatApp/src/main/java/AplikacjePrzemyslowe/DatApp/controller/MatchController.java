package AplikacjePrzemyslowe.DatApp.controller;

import AplikacjePrzemyslowe.DatApp.dto.response.MatchResponse;
import AplikacjePrzemyslowe.DatApp.dto.response.PageResponse;
import AplikacjePrzemyslowe.DatApp.service.MatchService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Kontroler dla dopasowań (matches).
 */
@RestController
@RequestMapping("/api/v1/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @Operation(summary = "Get matches for user (paginated)")
    @GetMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<MatchResponse>> getMatches(@PathVariable Long userId,
                                                                  Pageable pageable) {
        Page<MatchResponse> page = matchService.getMatches(userId, pageable);

        PageResponse<MatchResponse> response = PageResponse.<MatchResponse>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Unmatch (delete match)")
    @DeleteMapping("/{matchId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> unmatch(@PathVariable Long matchId,
                                       @RequestParam Long userId) {
        matchService.unmatch(matchId, userId);
        return ResponseEntity.noContent().build();
    }
}

