package AplikacjePrzemyslowe.DatApp.service;

import AplikacjePrzemyslowe.DatApp.dto.response.InterestResponse;
import AplikacjePrzemyslowe.DatApp.entity.Interest;
import AplikacjePrzemyslowe.DatApp.exception.ResourceNotFoundException;
import AplikacjePrzemyslowe.DatApp.repository.InterestRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InterestService unit tests")
class InterestServiceTests {

    @Mock private InterestRepository interestRepository;
    @Mock private ModelMapper modelMapper;

    @InjectMocks private InterestService interestService;

    @Test
    @DisplayName("findAll: maps page of interests")
    void findAll_ok() {
        Page<Interest> page = new PageImpl<>(List.of(
                Interest.builder().id(1L).name("Chess").category("games").build(),
                Interest.builder().id(2L).name("Running").category("sport").build()
        ), PageRequest.of(0, 10), 2);

        when(interestRepository.findAll(any(PageRequest.class))).thenReturn(page);
        when(modelMapper.map(any(Interest.class), eq(InterestResponse.class))).thenReturn(new InterestResponse());

        Page<InterestResponse> resp = interestService.findAll(PageRequest.of(0, 10));

        assertThat(resp.getTotalElements()).isEqualTo(2);
        verify(interestRepository).findAll(any(PageRequest.class));
    }

    @Test
    @DisplayName("findById: throws when missing")
    void findById_missing() {
        when(interestRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interestService.findById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("findById: maps when present")
    void findById_ok() {
        Interest i = Interest.builder().id(1L).name("Chess").category("games").build();
        when(interestRepository.findById(1L)).thenReturn(Optional.of(i));
        when(modelMapper.map(i, InterestResponse.class)).thenReturn(InterestResponse.builder().id(1L).build());

        InterestResponse resp = interestService.findById(1L);

        assertThat(resp.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findByCategory: maps page")
    void findByCategory_ok() {
        Page<Interest> page = new PageImpl<>(List.of(
                Interest.builder().id(1L).name("Chess").category("games").build()
        ), PageRequest.of(0, 10), 1);

        when(interestRepository.findByCategory(eq("games"), any())).thenReturn(page);
        when(modelMapper.map(any(Interest.class), eq(InterestResponse.class))).thenReturn(new InterestResponse());

        Page<InterestResponse> resp = interestService.findByCategory("games", PageRequest.of(0, 10));

        assertThat(resp.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("searchByName: maps list")
    void searchByName_ok() {
        when(interestRepository.searchByName("ch")).thenReturn(List.of(
                Interest.builder().id(1L).name("Chess").category("games").build()
        ));
        when(modelMapper.map(any(Interest.class), eq(InterestResponse.class))).thenReturn(new InterestResponse());

        List<InterestResponse> resp = interestService.searchByName("ch");

        assertThat(resp).hasSize(1);
    }

    @Test
    @DisplayName("findCommonInterests/countCommonInterests: delegates to repo")
    void commonInterests_ok() {
        when(interestRepository.findCommonInterests(1L, 2L)).thenReturn(List.of(
                Interest.builder().id(1L).name("Chess").category("games").build()
        ));
        when(modelMapper.map(any(Interest.class), eq(InterestResponse.class))).thenReturn(new InterestResponse());
        when(interestRepository.countCommonInterests(1L, 2L)).thenReturn(1L);

        assertThat(interestService.findCommonInterests(1L, 2L)).hasSize(1);
        assertThat(interestService.countCommonInterests(1L, 2L)).isEqualTo(1L);
    }

    @Test
    @DisplayName("findPopularInterests: maps page")
    void findPopularInterests_ok() {
        Page<Interest> page = new PageImpl<>(List.of(
                Interest.builder().id(1L).name("Chess").category("games").build()
        ), PageRequest.of(0, 10), 1);

        when(interestRepository.findPopularInterests(any())).thenReturn(page);
        when(modelMapper.map(any(Interest.class), eq(InterestResponse.class))).thenReturn(new InterestResponse());

        Page<InterestResponse> resp = interestService.findPopularInterests(PageRequest.of(0, 10));

        assertThat(resp.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("getInterestEntity: returns entity")
    void getInterestEntity_ok() {
        Interest i = Interest.builder().id(1L).name("Chess").category("games").build();
        when(interestRepository.findById(1L)).thenReturn(Optional.of(i));

        Interest entity = interestService.getInterestEntity(1L);

        assertThat(entity.getId()).isEqualTo(1L);
    }
}

