package AplikacjePrzemyslowe.DatApp.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Photo Entity Tests")
class PhotoTests {

    private Photo photo;
    private Profile profile;

    @BeforeEach
    void setUp() {
        profile = Profile.builder().id(1L).build();
        photo = Photo.builder()
                .id(1L)
                .profile(profile)
                .photoUrl("https://example.com/photos/photo1.jpg")
                .isPrimary(false)
                .displayOrder(0)
                .build();
    }

    @Test
    @DisplayName("Builder powinien ustawić wszystkie pola")
    void builder_setsAllFields() {
        assertEquals(1L, photo.getId());
        assertEquals(profile, photo.getProfile());
        assertEquals("https://example.com/photos/photo1.jpg", photo.getPhotoUrl());
        assertFalse(photo.getIsPrimary());
        assertEquals(0, photo.getDisplayOrder());
        // uploadedAt jest ustawiany automatycznie przez @CreationTimestamp tylko podczas zapisu do bazy
    }

    @Test
    @DisplayName("Settery i gettery powinny działać")
    void settersGetters_work() {
        photo.setPhotoUrl("https://example.com/photos/photo2.jpg");
        photo.setDisplayOrder(2);
        photo.setIsPrimary(true);

        assertEquals("https://example.com/photos/photo2.jpg", photo.getPhotoUrl());
        assertEquals(2, photo.getDisplayOrder());
        assertTrue(photo.getIsPrimary());
    }

    @Test
    @DisplayName("setAsPrimary powinno ustawić isPrimary na true")
    void setAsPrimary_setsPrimaryTrue() {
        assertFalse(photo.getIsPrimary());

        photo.setAsPrimary();

        assertTrue(photo.getIsPrimary());
    }

    @Test
    @DisplayName("removeAsPrimary powinno ustawić isPrimary na false")
    void removeAsPrimary_setPrimaryFalse() {
        photo.setIsPrimary(true);
        assertTrue(photo.getIsPrimary());

        photo.removeAsPrimary();

        assertFalse(photo.getIsPrimary());
    }

    @Test
    @DisplayName("equals powinno porównywać id i photoUrl")
    void equals_comparesIdAndPhotoUrl() {
        Photo photo1 = Photo.builder().id(1L).photoUrl("https://example.com/1.jpg").build();
        Photo photo2 = Photo.builder().id(1L).photoUrl("https://example.com/1.jpg").build();
        Photo photo3 = Photo.builder().id(2L).photoUrl("https://example.com/1.jpg").build();
        Photo photo4 = Photo.builder().id(1L).photoUrl("https://example.com/2.jpg").build();

        assertEquals(photo1, photo2);
        assertNotEquals(photo1, photo3);
        assertNotEquals(photo1, photo4);
    }

    @Test
    @DisplayName("hashCode powinno być spójne z equals")
    void hashCode_consistentWithEquals() {
        Photo photo1 = Photo.builder().id(1L).photoUrl("https://example.com/1.jpg").build();
        Photo photo2 = Photo.builder().id(1L).photoUrl("https://example.com/1.jpg").build();

        assertEquals(photo1.hashCode(), photo2.hashCode());
    }

    @Test
    @DisplayName("toString powinno zawierać kluczowe pola")
    void toString_containsKeyFields() {
        String str = photo.toString();
        assertTrue(str.contains("id=1"));
        assertTrue(str.contains("profileId=1"));
        assertTrue(str.contains("https://example.com/photos/photo1.jpg"));
        assertTrue(str.contains("isPrimary=false"));
        assertTrue(str.contains("displayOrder=0"));
    }
}

