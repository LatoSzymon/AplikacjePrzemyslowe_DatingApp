package AplikacjePrzemyslowe.DatApp.service;

import AplikacjePrzemyslowe.DatApp.dto.request.RegisterRequest;
import AplikacjePrzemyslowe.DatApp.dto.response.UserResponse;
import AplikacjePrzemyslowe.DatApp.entity.Gender;
import AplikacjePrzemyslowe.DatApp.entity.User;
import AplikacjePrzemyslowe.DatApp.exception.ResourceAlreadyExistsException;
import AplikacjePrzemyslowe.DatApp.exception.ResourceNotFoundException;
import AplikacjePrzemyslowe.DatApp.exception.ValidationException;
import AplikacjePrzemyslowe.DatApp.repository.UserRepository;
import AplikacjePrzemyslowe.DatApp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests with Mockito")
class UserServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UserService userService;

    private RegisterRequest registerRequest;
    private User testUser;

    @BeforeEach
    void setup() {
        registerRequest = RegisterRequest.builder()
                .username("johndoe")
                .email("john@example.com")
                .password("password123")
                .gender("MALE")
                .birthDate(LocalDate.of(1995, 5, 15))
                .city("Warsaw")
                .build();

        testUser = User.builder()
                .id(1L)
                .username("johndoe")
                .email("john@example.com")
                .password("hashedPassword")
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(1995, 5, 15))
                .city("Warsaw")
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("1. Register new user successfully")
    void testRegisterNewUserSuccess() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("johndoe")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(modelMapper.map(testUser, UserResponse.class)).thenReturn(UserResponse.builder()
                .id(1L)
                .username("johndoe")
                .email("john@example.com")
                .build());

        UserResponse response = userService.register(registerRequest);

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("johndoe");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("2. Reject registration - email already exists")
    void testRegisterEmailAlreadyExists() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(registerRequest))
                .isInstanceOf(ResourceAlreadyExistsException.class);
    }

    @Test
    @DisplayName("3. Reject registration - username already exists")
    void testRegisterUsernameAlreadyExists() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("johndoe")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(registerRequest))
                .isInstanceOf(ResourceAlreadyExistsException.class);
    }

    @Test
    @DisplayName("4. Reject registration - user too young (under 18)")
    void testRegisterUserTooYoung() {
        registerRequest.setBirthDate(LocalDate.now().minusYears(17));
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("johndoe")).thenReturn(false);

        assertThatThrownBy(() -> userService.register(registerRequest))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("5. Find user by ID")
    void testFindUserById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(modelMapper.map(testUser, UserResponse.class)).thenReturn(UserResponse.builder()
                .id(1L)
                .username("johndoe")
                .build());

        UserResponse response = userService.findById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("johndoe");
    }

    @Test
    @DisplayName("6. Find user by ID - not found")
    void testFindUserByIdNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("7. Find user by email")
    void testFindUserByEmail() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(modelMapper.map(testUser, UserResponse.class)).thenReturn(UserResponse.builder()
                .id(1L)
                .email("john@example.com")
                .build());

        UserResponse response = userService.findByEmail("john@example.com");

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    @DisplayName("8. Find user by username")
    void testFindUserByUsername() {
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));
        when(modelMapper.map(testUser, UserResponse.class)).thenReturn(UserResponse.builder()
                .id(1L)
                .username("johndoe")
                .build());

        UserResponse response = userService.findByUsername("johndoe");

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("johndoe");
    }

    @Test
    @DisplayName("9. Update password - success")
    void testUpdatePasswordSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword123")).thenReturn("newHashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        assertThatNoException().isThrownBy(() -> 
            userService.updatePassword(1L, "password123", "newPassword123"));

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("10. Update password - incorrect current password")
    void testUpdatePasswordIncorrect() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

        assertThatThrownBy(() -> 
            userService.updatePassword(1L, "wrongPassword", "newPassword123"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("11. Update password - new password same as old")
    void testUpdatePasswordSameAsOld() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true); // current ok
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true); // new same as old

        assertThatThrownBy(() -> userService.updatePassword(1L, "password123", "password123"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("12. Update password - user not found")
    void testUpdatePasswordUserNotFound() {
        when(userRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updatePassword(404L, "x", "y"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("13. Update city - success")
    void testUpdateCitySuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(modelMapper.map(testUser, UserResponse.class)).thenReturn(UserResponse.builder()
                .id(1L)
                .username("johndoe")
                .email("john@example.com")
                .build());

        UserResponse response = userService.updateCity(1L, "Krakow");

        assertThat(testUser.getCity()).isEqualTo("Krakow");
        assertThat(response).isNotNull();
        assertThat(response.getAge()).isEqualTo(testUser.getAge());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("14. Update city - user not found")
    void testUpdateCityNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateCity(2L, "Gdansk"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("15. Deactivate account - success")
    void testDeactivateAccountSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        userService.deactivateAccount(1L);

        assertThat(testUser.getIsActive()).isFalse();
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("16. Deactivate account - user not found")
    void testDeactivateAccountNotFound() {
        when(userRepository.findById(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deactivateAccount(3L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("17. Reactivate account - success")
    void testReactivateAccountSuccess() {
        testUser.setIsActive(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        userService.reactivateAccount(1L);

        assertThat(testUser.getIsActive()).isTrue();
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("18. Delete account - success")
    void testDeleteAccountSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        userService.deleteAccount(1L);

        verify(userRepository).delete(testUser);
    }

    @Test
    @DisplayName("19. Delete account - user not found")
    void testDeleteAccountNotFound() {
        when(userRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteAccount(5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("20. Exists & count - simple delegates")
    void testExistsAndCountDelegates() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);
        when(userRepository.existsByUsername("johndoe")).thenReturn(false);
        when(userRepository.countActiveUsers()).thenReturn(3L);

        assertThat(userService.existsById(1L)).isTrue();
        assertThat(userService.existsByEmail("john@example.com")).isTrue();
        assertThat(userService.existsByUsername("johndoe")).isFalse();
        assertThat(userService.countActiveUsers()).isEqualTo(3L);
    }

    @Test
    @DisplayName("21. getUserEntity - found")
    void testGetUserEntityFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        User entity = userService.getUserEntity(1L);

        assertThat(entity).isSameAs(testUser);
    }

    @Test
    @DisplayName("22. getUserEntity - not found")
    void testGetUserEntityNotFound() {
        when(userRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserEntity(9L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("23. deleteUserById - not found")
    void testDeleteUserByIdNotFound() {
        when(userRepository.existsById(7L)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUserById(7L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("24. deleteUserById - success")
    void testDeleteUserByIdSuccess() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUserById(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    @DisplayName("25. Register - invalid gender string throws IllegalArgumentException")
    void testRegisterInvalidGender() {
        registerRequest.setGender("UNKNOWN");
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("johndoe")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedX"); // wywoła się przed Gender.valueOf

        assertThatThrownBy(() -> userService.register(registerRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("26. findAll - returns mapped page")
    void testFindAllPaged() {
        User u1 = User.builder()
                .id(11L).username("u1").email("u1@x.com").password("p")
                .gender(Gender.MALE).birthDate(LocalDate.of(1990, 1, 1)).city("W")
                .isActive(true).build();
        User u2 = User.builder()
                .id(12L).username("u2").email("u2@x.com").password("p")
                .gender(Gender.FEMALE).birthDate(LocalDate.of(1992, 2, 2)).city("W")
                .isActive(false).build();

        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(u1, u2), pageable, 2));
        when(modelMapper.map(any(User.class), eq(UserResponse.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0, User.class);
            return UserResponse.builder().id(u.getId()).username(u.getUsername()).email(u.getEmail()).build();
        });

        Page<UserResponse> page = userService.findAll(pageable);

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting(UserResponse::getUsername).containsExactly("u1", "u2");
        assertThat(page.getContent().get(0).getAge()).isEqualTo(u1.getAge());
        assertThat(page.getContent().get(1).getAge()).isEqualTo(u2.getAge());
    }

    @Test
    @DisplayName("27. findActiveUsers - returns only active mapped page")
    void testFindActiveUsersPaged() {
        User u1 = User.builder()
                .id(21L).username("a1").email("a1@x.com").password("p")
                .gender(Gender.MALE).birthDate(LocalDate.of(1991, 3, 3)).city("W")
                .isActive(true).build();

        Pageable pageable = PageRequest.of(0, 5);
        when(userRepository.findByIsActiveTrue(pageable)).thenReturn(new PageImpl<>(List.of(u1), pageable, 1));
        when(modelMapper.map(any(User.class), eq(UserResponse.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0, User.class);
            return UserResponse.builder().id(u.getId()).username(u.getUsername()).email(u.getEmail()).build();
        });

        Page<UserResponse> page = userService.findActiveUsers(pageable);

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getUsername()).isEqualTo("a1");
        assertThat(page.getContent().get(0).getAge()).isEqualTo(u1.getAge());
    }
}

