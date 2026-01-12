package AplikacjePrzeyslowe.dApp.service;

import AplikacjePrzeyslowe.dApp.dto.response.InterestResponse;
import AplikacjePrzeyslowe.dApp.entity.Interest;
import AplikacjePrzeyslowe.dApp.exception.ResourceNotFoundException;
import AplikacjePrzeyslowe.dApp.repository.InterestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service dla zarządzania zainteresowaniami.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterestService {

    private final InterestRepository interestRepository;
    private final ModelMapper modelMapper;

    // ========== READ OPERATIONS ==========

    /**
     * Znajduje wszystkie zainteresowania z paginacją.
     */
    @Transactional(readOnly = true)
    public Page<InterestResponse> findAll(Pageable pageable) {
        log.debug("Finding all interests - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<Interest> interests = interestRepository.findAll(pageable);

        return interests.map(interest -> modelMapper.map(interest, InterestResponse.class));
    }

    /**
     * Znajduje zainteresowanie po ID.
     */
    @Transactional(readOnly = true)
    public InterestResponse findById(Long id) {
        log.debug("Finding interest by id: {}", id);

        Interest interest = interestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Interest", id));

        return modelMapper.map(interest, InterestResponse.class);
    }

    /**
     * Znajduje zainteresowania po kategorii.
     */
    @Transactional(readOnly = true)
    public Page<InterestResponse> findByCategory(String category, Pageable pageable) {
        log.debug("Finding interests by category: {}", category);

        Page<Interest> interests = interestRepository.findByCategory(category, pageable);

        return interests.map(interest -> modelMapper.map(interest, InterestResponse.class));
    }

    /**
     * Wyszukuje zainteresowania po nazwie.
     */
    @Transactional(readOnly = true)
    public List<InterestResponse> searchByName(String searchText) {
        log.debug("Searching interests by name: {}", searchText);

        List<Interest> interests = interestRepository.searchByName(searchText);

        return interests.stream()
                .map(interest -> modelMapper.map(interest, InterestResponse.class))
                .collect(Collectors.toList());
    }

    /**
     * Znajduje wspólne zainteresowania między dwoma użytkownikami.
     */
    @Transactional(readOnly = true)
    public List<InterestResponse> findCommonInterests(Long userId1, Long userId2) {
        log.debug("Finding common interests between users {} and {}", userId1, userId2);

        List<Interest> commonInterests = interestRepository.findCommonInterests(userId1, userId2);

        log.info("Found {} common interests between users {} and {}",
                commonInterests.size(), userId1, userId2);

        return commonInterests.stream()
                .map(interest -> modelMapper.map(interest, InterestResponse.class))
                .collect(Collectors.toList());
    }

    /**
     * Liczy wspólne zainteresowania między dwoma użytkownikami.
     */
    @Transactional(readOnly = true)
    public long countCommonInterests(Long userId1, Long userId2) {
        log.debug("Counting common interests between users {} and {}", userId1, userId2);

        long count = interestRepository.countCommonInterests(userId1, userId2);

        log.debug("Common interests count: {}", count);

        return count;
    }

    /**
     * Znajduje najpopularniejsze zainteresowania.
     */
    @Transactional(readOnly = true)
    public Page<InterestResponse> findPopularInterests(Pageable pageable) {
        log.debug("Finding popular interests");

        Page<Interest> interests = interestRepository.findPopularInterests(pageable);

        return interests.map(interest -> modelMapper.map(interest, InterestResponse.class));
    }

    // ========== HELPER METHODS ==========

    /**
     * Pobiera Interest entity (do użytku wewnętrznego).
     */
    @Transactional(readOnly = true)
    public Interest getInterestEntity(Long id) {
        return interestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Interest", id));
    }
}

