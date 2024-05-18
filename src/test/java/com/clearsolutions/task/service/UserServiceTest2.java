package com.clearsolutions.task.service;

import com.clearsolutions.task.exception.UserNotFoundException;
import com.clearsolutions.task.model.User;
import com.clearsolutions.task.dto.UserRequest;
import com.clearsolutions.task.exception.UserAlreadyExistsException;
import com.clearsolutions.task.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@Import(UserServiceTest.UserServiceTestConfiguration.class)
class UserServiceTest {

    @MockBean
    public UserRepository userRepository;

    @Autowired
    protected UserService userService;

    @TestConfiguration
    static class UserServiceTestConfiguration {
        @Bean
        public UserService userService(UserRepository userRepository) {
            return new UserService(userRepository);
        }
    }

    private static final Long ID = 1L;
    private User simpleUser;
    private UserRequest simpleUserRequest;
    private UserRequest updateUserRequest;
    private static final Random randomYear = new Random();
    private static List<User> usersList =
            IntStream.range(1, 51).mapToObj(i -> User.builder()
            .id(Long.parseLong(String.valueOf(i)))
            .email("user" + i + "@gmail.com")
                    .firstName("user"+i+"firstname")
                    .lastName("user"+i+"lastname")
                    .birthDate(LocalDate.parse("19" + randomYear.nextInt(10) + "" + randomYear.nextInt(10) + "-05-25"))
            .address("City" + i)
                    .phoneNumber("+38095" + i)
                    .build())
            .toList();

    @BeforeEach
    public void setUp() {
        Page<User> userPage = new PageImpl<>(usersList, PageRequest.of(0,20),usersList.size());
        when(userRepository.findAll()).thenReturn(usersList);

        simpleUser = User.builder()
                .id(ID)
                .email("email@gmail.com")
                .firstName("Afrosin")
                .lastName("Dmytro")
                .birthDate(LocalDate.parse("1999-05-25"))
                .address("Kyiv")
                .phoneNumber("+38095")
                .build();
        simpleUserRequest = UserRequest.builder()
                .email("email@gmail.com")
                .firstName("Afrosin")
                .lastName("Dmytro")
                .birthDate(LocalDate.parse("1999-05-25"))
                .address("Kyiv")
                .phoneNumber("+38095")
                .build();
        updateUserRequest = UserRequest.builder()
                .email("updated@gmail.com")
                .firstName("updated")
                .lastName("updated")
                .birthDate(LocalDate.parse("2000-05-25"))
                .address("updated")
                .phoneNumber("updated")
                .build();
    }

    @Test
    void givenFullCorrectUserRequest_whenCreateUser_thanReturnUser() {
        when(userRepository.save(any())).thenReturn(simpleUser);

        User registeredUser = userService.createUser(simpleUserRequest);

        assertNotNull(registeredUser);
        assertNotNull(registeredUser.getId());
        assertEquals(simpleUser, registeredUser);
    }

    @Test
    void givenFullCorrectUserRequest_whenCreateUserEmailExists_thenThrowException() {

        assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(simpleUserRequest));
        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    void givenValidIdAndRequest_whenUpdateUserById_thenReturnChangedUser() {
        when(userRepository.findById(ID)).thenReturn(Optional.of(simpleUser));
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        userService.updateUserById(ID, updateUserRequest);

        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertNotNull(capturedUser);
        assertNotNull(capturedUser.getId());
        assertEquals(updateUserRequest.getEmail(), capturedUser.getEmail());
        assertEquals(updateUserRequest.getFirstName(), capturedUser.getFirstName());
        assertEquals(updateUserRequest.getLastName(), capturedUser.getLastName());
        assertEquals(updateUserRequest.getBirthDate(), capturedUser.getBirthDate());
        assertEquals(updateUserRequest.getAddress(), capturedUser.getAddress());
        assertEquals(updateUserRequest.getPhoneNumber(), capturedUser.getPhoneNumber());
    }

    @Test
    public void givenIncorrectId_whenUpdateUserById_thenThrowException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        Executable executable = () -> userService.updateUserById(999L, new UserRequest());
        assertThrows(UserNotFoundException.class, executable);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void givenExistingEmail_whenUpdateUserById_thenThrowException() {
        when(userRepository.findById(ID)).thenReturn(Optional.of(simpleUser));

        assertThrows(UserAlreadyExistsException.class, () -> userService.updateUserById(ID, simpleUserRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void givenOptionalNullFields_whenUpdateUserById_thenReturnNotNullFields() {
        simpleUserRequest.setAddress(null);
        simpleUserRequest.setPhoneNumber(null);

        simpleUser.setAddress(null);
        simpleUser.setPhoneNumber(null);

        when(userRepository.findById(ID)).thenReturn(Optional.of(simpleUser));
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        userService.updateUserById(ID, simpleUserRequest);

        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertNotNull(capturedUser.getAddress());
        assertNotNull(capturedUser.getPhoneNumber());
    }


    @Test
    void givenIncorrectId_whenPatchUpdateUser_thenThrowException() {
        when(userRepository.findById(ID)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.patchUpdateUser(ID, new UserRequest()));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void givenIncorrectEmail_whenPatchUpdateUser_thenThrowException() {
        UserRequest userRequest = UserRequest.builder()
                .email("e@e@gmail.com")
                .build();

        when(userRepository.findById(ID)).thenReturn(Optional.of(simpleUser));

        assertThrows(UserAlreadyExistsException.class, () -> userService.patchUpdateUser(ID, userRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void givenExistingEmail_whenPatchUpdateUser_thenThrowException() {
        UserRequest userRequest = UserRequest.builder()
                .email("email@gmail.com")
                .build();

        when(userRepository.findById(ID)).thenReturn(Optional.of(simpleUser));

        assertThrows(UserAlreadyExistsException.class, () -> userService.patchUpdateUser(ID, userRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void givenOnlyEmail_whenPatchUpdateUser_thenUpdateUser() {
        UserRequest userRequest = UserRequest.builder()
                .email("updated@gmail.com")
                .build();

        when(userRepository.findById(ID)).thenReturn(Optional.of(simpleUser));
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        userService.patchUpdateUser(ID, userRequest);

        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertEquals(userRequest.getEmail(), capturedUser.getEmail());
    }

    @Test
    public void givenOnlyFirstName_whenPatchUpdateUser_thenUpdateUser() {
        UserRequest userRequest = UserRequest.builder()
                .firstName("updated")
                .build();

        when(userRepository.findById(ID)).thenReturn(Optional.of(simpleUser));
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        userService.patchUpdateUser(ID, userRequest);

        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertEquals(userRequest.getFirstName(), capturedUser.getFirstName());
    }

    @Test
    public void givenOnlyLastName_whenPatchUpdateUser_thenUpdateUser() {
        UserRequest userRequest = UserRequest.builder()
                .lastName("updated")
                .build();

        when(userRepository.findById(ID)).thenReturn(Optional.of(simpleUser));
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        userService.patchUpdateUser(ID, userRequest);

        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertEquals(userRequest.getLastName(), capturedUser.getLastName());
    }

    @Test
    public void givenOnlyBirthDate_whenPatchUpdateUser_thenUpdateUser() {
        UserRequest userRequest = UserRequest.builder()
                .birthDate(LocalDate.parse("1997-05-25"))
                .build();

        when(userRepository.findById(ID)).thenReturn(Optional.of(simpleUser));
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        userService.patchUpdateUser(ID, userRequest);

        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertEquals(userRequest.getBirthDate(), capturedUser.getBirthDate());
    }

    @Test
    public void givenOnlyAddress_whenPatchUpdateUser_thenUpdateUser() {
        UserRequest userRequest = UserRequest.builder()
                .address("updated")
                .build();

        when(userRepository.findById(ID)).thenReturn(Optional.of(simpleUser));
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        userService.patchUpdateUser(ID, userRequest);

        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertEquals(userRequest.getAddress(), capturedUser.getAddress());
    }

    @Test
    public void givenOnlyPhoneNumber_whenPatchUpdateUser_thenUpdateUser() {
        UserRequest userRequest = UserRequest.builder()
                .phoneNumber("updated")
                .build();

        when(userRepository.findById(ID)).thenReturn(Optional.of(simpleUser));
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        userService.patchUpdateUser(ID, userRequest);

        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertEquals(userRequest.getPhoneNumber(), capturedUser.getPhoneNumber());
    }

}