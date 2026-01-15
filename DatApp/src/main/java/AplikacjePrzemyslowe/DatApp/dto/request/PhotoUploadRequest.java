package AplikacjePrzemyslowe.DatApp.dto.request;

import jakarta.validation.constraints.AssertTrue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * DTO dla upload'u zdjęcia do profilu.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoUploadRequest {

    private MultipartFile photo;

    private Boolean isPrimary;

    private Integer displayOrder;

    /**
     * Walidacja pliku (sprawdzana w service layer)
     */
    @AssertTrue(message = "Plik zdjęcia jest wymagany")
    public boolean isPhotoValid() {
        return photo != null && !photo.isEmpty();
    }

    @AssertTrue(message = "Plik musi być obrazem (jpg, jpeg, png)")
    public boolean isImageFile() {
        if (photo == null) {
            return true; // Inna walidacja to obsłuży
        }
        String contentType = photo.getContentType();
        return contentType != null &&
               (contentType.equals("image/jpeg") ||
                contentType.equals("image/jpg") ||
                contentType.equals("image/png"));
    }

    @AssertTrue(message = "Rozmiar pliku nie może przekraczać 10MB")
    public boolean isFileSizeValid() {
        if (photo == null) {
            return true;
        }
        return photo.getSize() <= 10 * 1024 * 1024; // 10 MB
    }
}

