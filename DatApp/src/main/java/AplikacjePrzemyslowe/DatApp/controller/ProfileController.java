package AplikacjePrzemyslowe.DatApp.controller;

import AplikacjePrzemyslowe.DatApp.dto.request.PhotoUploadRequest;
import AplikacjePrzemyslowe.DatApp.dto.request.UpdateProfileRequest;
import AplikacjePrzemyslowe.DatApp.dto.response.PhotoResponse;
import AplikacjePrzemyslowe.DatApp.dto.response.ProfileResponse;
import AplikacjePrzemyslowe.DatApp.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @Operation(summary = "Get profile by user id")
    @GetMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProfileResponse> getProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(profileService.getProfileByUserId(userId));
    }

    @Operation(summary = "Update profile")
    @PutMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProfileResponse> updateProfile(@PathVariable Long userId,
                                                         @Validated @RequestBody UpdateProfileRequest request) {
        ProfileResponse updated = profileService.updateProfile(userId, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Upload photo to profile")
    @PostMapping(value = "/{userId}/photos", consumes = "multipart/form-data")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PhotoResponse> uploadPhoto(@PathVariable Long userId,
                                                     @Validated PhotoUploadRequest request) {
        MultipartFile file = request.getPhoto();
        PhotoResponse saved = profileService.addPhoto(userId, file != null ? file.getOriginalFilename() : null,
                request.getIsPrimary(), request.getDisplayOrder());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}

