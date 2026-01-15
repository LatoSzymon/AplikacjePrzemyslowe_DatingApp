package AplikacjePrzemyslowe.DatApp;

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

import java.time.LocalDate;
import java.util.Optional;

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
}

