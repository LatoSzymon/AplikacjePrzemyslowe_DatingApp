package AplikacjePrzemyslowe.DatApp.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Profile Entity Tests")
class ProfileTests {

    private Profile profile;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("alice").build();
        profile = Profile.builder()
                .id(1L)
                .user(user)
                .bio("Kocham podróżować i fotografować")
                .heightCm(170)
                .occupation("Inżynier")
                .education("Politechnika")
                .latitude(52.2297)
                .longitude(21.0122)
                .build();
    }

    @Test
    @DisplayName("Builder powinien ustawić wszystkie pola")
    void builder_setsAllFields() {
        assertEquals(1L, profile.getId());
        assertEquals(user, profile.getUser());
        assertEquals("Kocham podróżować i fotografować", profile.getBio());
        assertEquals(170, profile.getHeightCm());
        assertEquals("Inżynier", profile.getOccupation());
        assertEquals("Politechnika", profile.getEducation());
        assertEquals(52.2297, profile.getLatitude());
        assertEquals(21.0122, profile.getLongitude());
        assertNotNull(profile.getPhotos());
        assertNotNull(profile.getInterests());
        assertTrue(profile.getPhotos().isEmpty());
        assertTrue(profile.getInterests().isEmpty());
    }

    @Test
    @DisplayName("Settery i gettery powinny działać")
    void settersGetters_work() {
        profile.setBio("Nowe bio");
        profile.setHeightCm(180);
        profile.setOccupation("Lekarz");

        assertEquals("Nowe bio", profile.getBio());
        assertEquals(180, profile.getHeightCm());
        assertEquals("Lekarz", profile.getOccupation());
    }

    @Test
    @DisplayName("addPhoto powinno dodać zdjęcie do profilu")
    void addPhoto_addsPhotoToProfile() {
        Photo photo = Photo.builder().id(1L).photoUrl("https://example.com/1.jpg").build();
        profile.addPhoto(photo);

        assertEquals(1, profile.getPhotos().size());
        assertEquals(profile, photo.getProfile());
    }

    @Test
    @DisplayName("removePhoto powinno usunąć zdjęcie z profilu")
    void removePhoto_removesPhotoFromProfile() {
        Photo photo = Photo.builder().id(1L).photoUrl("https://example.com/1.jpg").build();
        profile.addPhoto(photo);
        assertEquals(1, profile.getPhotos().size());

        profile.removePhoto(photo);
        assertEquals(0, profile.getPhotos().size());
    }

    @Test
    @DisplayName("addInterest powinno dodać zainteresowanie do profilu")
    void addInterest_addsInterestToProfile() {
        Interest interest = Interest.builder().id(1L).name("Gry wideo").build();
        profile.addInterest(interest);

        assertEquals(1, profile.getInterests().size());
        assertTrue(profile.getInterests().contains(interest));
    }

    @Test
    @DisplayName("removeInterest powinno usunąć zainteresowanie z profilu")
    void removeInterest_removesInterestFromProfile() {
        Interest interest = Interest.builder().id(1L).name("Gry wideo").build();
        profile.addInterest(interest);
        assertEquals(1, profile.getInterests().size());

        profile.removeInterest(interest);
        assertEquals(0, profile.getInterests().size());
    }

    @Test
    @DisplayName("getMainPhoto powinno zwrócić primarne zdjęcie lub pierwsze")
    void getMainPhoto_returnsMainOrFirst() {
        Photo photo1 = Photo.builder().id(1L).photoUrl("https://example.com/1.jpg").isPrimary(false).build();
        Photo photo2 = Photo.builder().id(2L).photoUrl("https://example.com/2.jpg").isPrimary(true).build();
        profile.addPhoto(photo1);
        profile.addPhoto(photo2);

        Photo mainPhoto = profile.getMainPhoto();
        assertTrue(mainPhoto.getIsPrimary());
        assertEquals(photo2, mainPhoto);

        // Test gdy brak primary - powinno zwrócić pierwsze
        Photo photo3 = Photo.builder().id(3L).photoUrl("https://example.com/3.jpg").isPrimary(false).build();
        Profile profile2 = Profile.builder().user(user).build();
        profile2.addPhoto(photo3);
        assertNotNull(profile2.getMainPhoto());
    }

    @Test
    @DisplayName("isComplete powinno sprawdzić czy profil ma bio i przynajmniej jedno zdjęcie")
    void isComplete_checksCompleteness() {
        // Bez bio i zdjęć
        Profile incomplete1 = Profile.builder().user(user).build();
        assertFalse(incomplete1.isComplete());

        // Z bio ale bez zdjęć
        Profile incomplete2 = Profile.builder().user(user).bio("Bio").build();
        assertFalse(incomplete2.isComplete());

        // Z zdjęciami ale bez bio
        Profile incomplete3 = Profile.builder().user(user).build();
        Photo photo = Photo.builder().id(1L).photoUrl("https://example.com/1.jpg").build();
        incomplete3.addPhoto(photo);
        assertFalse(incomplete3.isComplete());

        // Z bio i zdjęciami
        Profile complete = Profile.builder().user(user).bio("Bio").build();
        Photo photo2 = Photo.builder().id(2L).photoUrl("https://example.com/2.jpg").build();
        complete.addPhoto(photo2);
        assertTrue(complete.isComplete());
    }

    @Test
    @DisplayName("equals powinno porównywać tylko id")
    void equals_comparesId() {
        Profile prof1 = Profile.builder().id(1L).build();
        Profile prof2 = Profile.builder().id(1L).build();
        Profile prof3 = Profile.builder().id(2L).build();

        assertEquals(prof1, prof2);
        assertNotEquals(prof1, prof3);
    }

    @Test
    @DisplayName("hashCode powinno być spójne z equals")
    void hashCode_consistentWithEquals() {
        Profile prof1 = Profile.builder().id(1L).build();
        Profile prof2 = Profile.builder().id(1L).build();

        assertEquals(prof1.hashCode(), prof2.hashCode());
    }

    @Test
    @DisplayName("toString powinno zawierać kluczowe pola")
    void toString_containsKeyFields() {
        String str = profile.toString();
        assertTrue(str.contains("id=1"));
        assertTrue(str.contains("userId=1"));
        assertTrue(str.contains("photosCount=0"));
        assertTrue(str.contains("interestsCount=0"));
    }
}

