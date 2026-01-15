package AplikacjePrzemyslowe.DatApp.controller;

import AplikacjePrzemyslowe.DatApp.dto.backup.ProfileBackupDto;
import AplikacjePrzemyslowe.DatApp.service.ProfileBackupService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Kontroler dla backupu profili (export/import JSON/XML).
 */
@RestController
@RequestMapping("/api/v1/backup")
@RequiredArgsConstructor
public class BackupController {

    private final ProfileBackupService profileBackupService;

    @Operation(summary = "Export profile to JSON or XML")
    @PostMapping("/{userId}/export")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> exportProfile(@PathVariable Long userId,
                                               @RequestParam(defaultValue = "json") String format) {
        byte[] data;
        String contentType;
        String filename;

        if ("xml".equalsIgnoreCase(format)) {
            data = profileBackupService.exportProfileToXml(userId);
            contentType = MediaType.APPLICATION_XML_VALUE;
            filename = "profile_backup_" + userId + ".xml";
        } else {
            data = profileBackupService.exportProfileToJson(userId);
            contentType = MediaType.APPLICATION_JSON_VALUE;
            filename = "profile_backup_" + userId + ".json";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentDispositionFormData("attachment", filename);

        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }

    @Operation(summary = "Import profile from JSON or XML file")
    @PostMapping(value = "/{userId}/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProfileBackupDto> importProfile(@PathVariable Long userId,
                                                          @RequestParam("file") MultipartFile file,
                                                          @RequestParam(defaultValue = "json") String format) {
        ProfileBackupDto imported;

        if ("xml".equalsIgnoreCase(format)) {
            imported = profileBackupService.importProfileFromXml(userId, file);
        } else {
            imported = profileBackupService.importProfileFromJson(userId, file);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(imported);
    }
}

