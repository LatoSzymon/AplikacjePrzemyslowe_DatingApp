package AplikacjePrzemyslowe.DatApp.controller;

import AplikacjePrzemyslowe.DatApp.dto.request.MessageRequest;
import AplikacjePrzemyslowe.DatApp.dto.response.MessageResponse;
import AplikacjePrzemyslowe.DatApp.dto.response.PageResponse;
import AplikacjePrzemyslowe.DatApp.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Kontroler dla wiadomości (messages).
 */
@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @Operation(summary = "Send message")
    @PostMapping("/{senderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> sendMessage(@PathVariable Long senderId,
                                                       @Validated @RequestBody MessageRequest request) {
        MessageResponse response = messageService.sendMessage(senderId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get conversation messages (paginated)")
    @GetMapping("/{userId}/{matchId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<MessageResponse>> getMessages(@PathVariable Long userId,
                                                                     @PathVariable Long matchId,
                                                                     Pageable pageable) {
        Page<MessageResponse> page = messageService.getConversation(userId, matchId, pageable);

        PageResponse<MessageResponse> response = PageResponse.<MessageResponse>builder()
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

    @Operation(summary = "Delete conversation")
    @DeleteMapping("/{userId}/{matchId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteConversation(@PathVariable Long userId,
                                                   @PathVariable Long matchId) {
        messageService.deleteConversation(userId, matchId);
        return ResponseEntity.noContent().build();
    }
}

