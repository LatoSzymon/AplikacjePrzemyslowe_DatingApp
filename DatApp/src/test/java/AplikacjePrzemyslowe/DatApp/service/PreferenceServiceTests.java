package AplikacjePrzemyslowe.DatApp.service;

import AplikacjePrzemyslowe.DatApp.dto.request.PreferenceRequest;
import AplikacjePrzemyslowe.DatApp.dto.response.PreferenceResponse;
import AplikacjePrzemyslowe.DatApp.entity.Gender;
import AplikacjePrzemyslowe.DatApp.entity.Preference;
import AplikacjePrzemyslowe.DatApp.entity.User;
import AplikacjePrzemyslowe.DatApp.exception.ResourceNotFoundException;
import AplikacjePrzemyslowe.DatApp.repository.PreferenceRepository;
import AplikacjePrzemyslowe.DatApp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PreferenceService unit tests")
class PreferenceServiceTests {

    @Mock private PreferenceRepository preferenceRepository;
    @Mock private UserRepository userRepository;
    @Mock private ModelMapper modelMapper;

    @InjectMocks private PreferenceService preferenceService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("u1").email("u1@x.pl").password("Password123").isActive(true).build();
    }

    @Test
    @DisplayName("getPreferences: throws when user missing")
    void getPreferences_userMissing() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> preferenceService.getPreferences(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getPreferences: returns defaults when no preferences")
    void getPreferences_defaults() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.empty());

        // modelMapper map() będzie wołany na obiekcie domyślnych preferencji
        when(modelMapper.map(any(Preference.class), eq(PreferenceResponse.class))).thenAnswer(inv -> {
            Preference p = inv.getArgument(0);
            PreferenceResponse r = new PreferenceResponse();
            r.setPreferredGender(p.getPreferredGender().name());
            r.setMinAge(p.getMinAge());
            r.setMaxAge(p.getMaxAge());
            return r;
        });

        PreferenceResponse resp = preferenceService.getPreferences(1L);

        assertThat(resp.getPreferredGender()).isEqualTo("OTHER");
        assertThat(resp.getMinAge()).isEqualTo(18);
        assertThat(resp.getMaxAge()).isEqualTo(99);
        assertThat(resp.getMaxDistance()).isEqualTo(50);
    }

    @Test
    @DisplayName("setPreferences: creates or updates preference")
    void setPreferences_ok() {
        PreferenceRequest req = PreferenceRequest.builder()
                .preferredGender("female")
                .minAge(20)
                .maxAge(30)
                .maxDistanceKm(25)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.empty());

        when(preferenceRepository.save(any(Preference.class))).thenAnswer(inv -> {
            Preference p = inv.getArgument(0);
            p.setId(10L);
            return p;
        });

        when(modelMapper.map(any(Preference.class), eq(PreferenceResponse.class))).thenReturn(new PreferenceResponse());

        PreferenceResponse resp = preferenceService.setPreferences(1L, req);

        assertThat(resp.getMaxDistance()).isEqualTo(25);
        verify(preferenceRepository).save(argThat(p ->
                p.getUser().getId().equals(1L)
                        && p.getPreferredGender() == Gender.FEMALE
                        && p.getMinAge() == 20
                        && p.getMaxAge() == 30
                        && p.getMaxDistanceKm() == 25
        ));
    }

    @Test
    @DisplayName("resetPreferences: saves defaults")
    void resetPreferences_ok() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.empty());

        when(preferenceRepository.save(any(Preference.class))).thenAnswer(inv -> inv.getArgument(0));
        when(modelMapper.map(any(Preference.class), eq(PreferenceResponse.class))).thenReturn(new PreferenceResponse());

        PreferenceResponse resp = preferenceService.resetPreferences(1L);

        assertThat(resp.getMaxDistance()).isEqualTo(50);
        verify(preferenceRepository).save(argThat(p -> p.getPreferredGender() == Gender.OTHER && p.getMinAge() == 18 && p.getMaxAge() == 99 && p.getMaxDistanceKm() == 50));
    }

    @Test
    @DisplayName("hasPreferences: returns true/false")
    void hasPreferences_ok() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.of(new Preference()));

        assertThat(preferenceService.hasPreferences(1L)).isTrue();

        when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.empty());
        assertThat(preferenceService.hasPreferences(1L)).isFalse();
    }

    @Test
    @DisplayName("deletePreferences: does nothing when missing")
    void deletePreferences_missing() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.empty());

        preferenceService.deletePreferences(1L);

        verify(preferenceRepository, never()).delete(any());
    }
}

