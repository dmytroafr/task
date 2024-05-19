package com.clearsolutions.task.service;

import com.clearsolutions.task.dto.UserRequest;
import com.clearsolutions.task.exception.UserNotFoundException;
import com.clearsolutions.task.model.User;
import com.clearsolutions.task.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@Import(UserServiceTest.UserServiceTestConfiguration.class)
class UserServiceTest {

    @MockBean
    public UserRepository userRepository;

    @Autowired
    private UserService userService;

    @TestConfiguration
    static class UserServiceTestConfiguration {
        @Bean
        public UserService userService(UserRepository userRepository) {
            return new UserService(userRepository);
        }
    }

    private static final Random random = new Random();
    private static final List<User> usersList =
            IntStream.range(1, 51).mapToObj(i -> User.builder()
                            .id(Long.parseLong(String.valueOf(i)))
                            .email("user" + i + "@gmail.com")
                            .firstName("user" + i + "firstname")
                            .lastName("user" + i + "lastname")
                            .birthDate(LocalDate.parse("19" + random.nextInt(10) + "" + random.nextInt(10) + "-05-25"))
                            .address("City" + i)
                            .phoneNumber("+38095" + i)
                            .build())
                    .toList();

    private static Stream<Arguments> provideJsonData() throws IOException {
        List<String> jsons = Files.readAllLines(Path.of("src/test/resources/json_for_PATCH.txt"));
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return jsons.stream().map(s -> {
            try {
                UserRequest userRequest = mapper.readValue(s, UserRequest.class);
                return Arguments.of(userRequest);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    @DisplayName("Get all users")
    void whenGetAllUsers_thenReturnPageable() {
        PageRequest pageRequest = PageRequest.of(0, 20);
        int listSize = usersList.size();

        Page<User> userPage = new PageImpl<>(usersList, pageRequest, usersList.size());
        when(userRepository.findAll(any(PageRequest.class))).thenReturn(userPage);

        Page<User> allUsers = userService.getAllUsers(pageRequest);
        assertEquals(listSize, allUsers.getNumberOfElements());
        assertEquals(listSize, allUsers.getTotalElements());
        assertEquals(3, allUsers.getTotalPages());
    }

    @Test
    @DisplayName("Get all users within range")
    void givenCorrectDates_whenGetAllUsersWithin_thenReturnPageable() {
        LocalDate fromDate = LocalDate.parse("1990-01-01");
        LocalDate toDate = LocalDate.parse("1997-12-31");

        List<User> list = usersList.stream()
                .filter(user -> user.getBirthDate().isAfter(fromDate) &&
                        user.getBirthDate().isBefore(toDate))
                .toList();

        Page<User> userPage = new PageImpl<>(list, PageRequest.of(0, 20), list.size());
        when(userRepository.findAllByBirthDateBetween(any(LocalDate.class), any(LocalDate.class), any(PageRequest.class))).thenReturn(userPage);

        Page<User> allUsersWithin = userService.getAllUsersWithin(fromDate, toDate, PageRequest.of(0, 20));

        assertEquals(list.size(), allUsersWithin.getTotalElements());
        assertEquals(list.size(), allUsersWithin.getNumberOfElements());
    }

    @Test
    @DisplayName("Get All Users within wrong Range")
    void givenIncorrectDates_whenGetAllUsersWithin_thenReturnException() {
        LocalDate fromDate = LocalDate.parse("1990-01-01");
        LocalDate toDate = LocalDate.parse("1997-12-31");

        assertThrows(IllegalArgumentException.class,
                () -> userService.getAllUsersWithin(toDate, fromDate, PageRequest.of(0, 20)));
    }

    @Test
    @DisplayName("Create new user")
    void whenCreateUser_thenReturnUser() {
        UserRequest userRequest
                = UserRequest.builder()
                .email("created@gmail.com")
                .firstName("createdFirstName")
                .lastName("createdLastName")
                .birthDate(LocalDate.now().minusYears(random.nextLong(18, 99)))
                .address("CreatedCity")
                .phoneNumber("+3987435234234")
                .build();


        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        userService.createUser(userRequest);

        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        assertNotNull(capturedUser);
        assertEquals(userRequest.getEmail(), capturedUser.getEmail());
        assertEquals(userRequest.getFirstName(), capturedUser.getFirstName());
        assertEquals(userRequest.getLastName(), capturedUser.getLastName());
        assertEquals(userRequest.getBirthDate(), capturedUser.getBirthDate());
        assertEquals(userRequest.getAddress(), capturedUser.getAddress());
        assertEquals(userRequest.getPhoneNumber(), capturedUser.getPhoneNumber());
    }

    @Test
    @DisplayName("Update user with wrong id")
    void givenWrongId_whenUpdateUser_thenReturnException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.updateUser(random.nextLong(), any(UserRequest.class)));
    }

    @Test
    @DisplayName("Update user by id")
    void givenUserRequest_whenUpdateUser_thenReturn() {
        User userFromDb = usersList.get(random.nextInt(50));
        Optional<User> optionalUserFromDb = Optional.of(userFromDb);

        when(userRepository.findById(anyLong())).thenReturn(optionalUserFromDb);

        UserRequest userRequest = UserRequest.builder()
                .email("updated@gmail.com")
                .firstName("UpdatedFirstName")
                .lastName("UpdatedLastName")
                .birthDate(LocalDate.now().minusYears(random.nextLong(18, 99)))
                .build();

        userService.updateUser(userFromDb.getId(), userRequest);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        assertEquals(userFromDb.getId(), capturedUser.getId());
        assertEquals(userRequest.getEmail(), capturedUser.getEmail());
        assertEquals(userRequest.getFirstName(), capturedUser.getFirstName());
        assertEquals(userRequest.getLastName(), capturedUser.getLastName());
        assertEquals(userRequest.getBirthDate(), capturedUser.getBirthDate());
        assertNull(capturedUser.getAddress());
        assertNull(capturedUser.getPhoneNumber());
    }

    @Test
    @DisplayName("Update user by id with full request")
    void givenFullUserRequest_whenUpdateUser_thenReturn() {
        User userFromDb = usersList.get(random.nextInt(50));
        Optional<User> optionalUserFromDb = Optional.of(userFromDb);

        when(userRepository.findById(anyLong())).thenReturn(optionalUserFromDb);

        UserRequest userRequest = UserRequest.builder()
                .email("updated@gmail.com")
                .firstName("UpdatedFirstName")
                .lastName("UpdatedLastName")
                .birthDate(LocalDate.now().minusYears(random.nextLong(18, 99)))
                .address("UpdatedCity")
                .phoneNumber("+38093485345")
                .build();

        userService.updateUser(userFromDb.getId(), userRequest);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        assertEquals(userFromDb.getId(), capturedUser.getId());
        assertEquals(userRequest.getEmail(), capturedUser.getEmail());
        assertEquals(userRequest.getFirstName(), capturedUser.getFirstName());
        assertEquals(userRequest.getLastName(), capturedUser.getLastName());
        assertEquals(userRequest.getBirthDate(), capturedUser.getBirthDate());
        assertEquals(userRequest.getAddress(), capturedUser.getAddress());
        assertEquals(userRequest.getPhoneNumber(), capturedUser.getPhoneNumber());
    }


    @Test
    @DisplayName("Patch user by wrong id")
    void givenWrongId_whenPatchUpdateUser_thenReturnException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.patchUpdateUser(random.nextLong(), any(UserRequest.class)));
    }

    @ParameterizedTest
    @MethodSource("provideJsonData")
    @DisplayName("Patch user with fields combinations")
    void givenFields_whenPatchUser_thenReturnNoContent(UserRequest userRequest) {
        User userFromDb = usersList.get(random.nextInt(50));
        Optional<User> optionalUserFromDb = Optional.of(userFromDb);

        when(userRepository.findById(anyLong())).thenReturn(optionalUserFromDb);

        userService.patchUpdateUser(userFromDb.getId(), userRequest);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        if (userRequest.getEmail() != null) {
            assertEquals(userRequest.getEmail(), capturedUser.getEmail());
        }
        if (userRequest.getFirstName() != null) {
            assertEquals(userRequest.getFirstName(), capturedUser.getFirstName());
        }
        if (userRequest.getLastName() != null) {
            assertEquals(userRequest.getLastName(), capturedUser.getLastName());
        }
        if (userRequest.getBirthDate() != null) {
            assertEquals(userRequest.getBirthDate(), capturedUser.getBirthDate());
        }
        if (userRequest.getAddress() != null) {
            assertEquals(userRequest.getAddress(), capturedUser.getAddress());
        }
        if (userRequest.getPhoneNumber() != null) {
            assertEquals(userRequest.getPhoneNumber(), capturedUser.getPhoneNumber());
        }
    }

    @Test
    @DisplayName(value = "Delete user by id")
    void givenCorrectId_whenDeleteUserById_thenPerformDelete() {
        Optional<User> optionalUser = Optional.of(usersList.get(random.nextInt(50)));
        when(userRepository.findById(anyLong())).thenReturn(optionalUser);
        User user = optionalUser.get();
        doNothing().when(userRepository).delete(user);

        userService.deleteUserById(user.getId());

        verify(userRepository, times(1)).delete(user);
    }

    @Test
    @DisplayName(value = "Delete user by wrong id")
    void givenIncorrectId_whenDeleteUserById_thenThrowException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteUserById(anyLong()));
    }
}