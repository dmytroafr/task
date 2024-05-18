package com.clearsolutions.task.service;

import com.clearsolutions.task.dto.UserRequest;
import com.clearsolutions.task.exception.UserNotFoundException;
import com.clearsolutions.task.model.User;
import com.clearsolutions.task.repository.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@Import(UserService2Test.UserServiceTestConfiguration.class)
class UserService2Test {

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

    private static List<User> usersList;

    @BeforeAll
    static void setUp(){
        Random randomYear = new Random();
        usersList =
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
    }

    @Test
    void whenGetAllUsers_thenReturnPageable() {
        PageRequest pageRequest = PageRequest.of(0, 20);
        int listSize = usersList.size();

        Page<User> userPage = new PageImpl<>(usersList,pageRequest,usersList.size());
        when(userRepository.findAll(any(PageRequest.class))).thenReturn(userPage);

        Page<User> allUsers = userService.getAllUsers(pageRequest);

        assertEquals(pageRequest.getPageSize(),listSize);
        assertEquals(listSize,allUsers.getNumberOfElements());
        assertEquals(listSize,allUsers.getTotalElements());
        assertEquals(3,allUsers.getTotalPages());
    }

    @Test
    void givenCorrectDates_whenGetAllUsersWithin_thenReturnPageable() {
        LocalDate fromDate = LocalDate.parse("1990-01-01");
        LocalDate toDate = LocalDate.parse("1997-12-31");

        List<User> list = usersList.stream()
                .filter(user -> user.getBirthDate().isAfter(fromDate) &&
                        user.getBirthDate().isBefore(toDate))
                .toList();

        Page<User> userPage = new PageImpl<>(list,PageRequest.of(0,20),list.size());
        when(userRepository.findAllByBirthDateBetween(any(LocalDate.class),any(LocalDate.class),any(PageRequest.class))).thenReturn(userPage);

        Page<User> allUsersWithin = userService.getAllUsersWithin(fromDate, toDate, PageRequest.of(0,20));

        assertEquals(list.size(),allUsersWithin.getTotalElements());
        assertEquals(list.size(),allUsersWithin.getNumberOfElements());
    }

    @Test
    void givenIncorrectDates_whenGetAllUsersWithin_thenReturnException() {
        LocalDate fromDate = LocalDate.parse("1990-01-01");
        LocalDate toDate = LocalDate.parse("1997-12-31");

        assertThrows(IllegalArgumentException.class,
                ()->userService.getAllUsersWithin(toDate,fromDate,PageRequest.of(0,20)));
    }
    @Test
    void createUser() {
        User userFromList = usersList.get(7);
        UserRequest build = UserRequest.builder()
                .email(userFromList.getEmail())
                .firstName(userFromList.getFirstName())
                .lastName(userFromList.getLastName())
                .birthDate(userFromList.getBirthDate())
                .address(userFromList.getAddress())
                .phoneNumber(userFromList.getPhoneNumber())
                .build();
        when(userRepository.save(any(User.class))).thenReturn(userFromList);

        User user = userService.createUser(build);

        assertNotNull(user);
        assertNotNull(user.getId());
        assertEquals(userFromList.getId(), user.getId());
        assertEquals(userFromList.getEmail(), user.getEmail());
        assertEquals(userFromList.getFirstName(), user.getFirstName());
        assertEquals(userFromList.getLastName(), user.getLastName());
        assertEquals(userFromList.getBirthDate(), user.getBirthDate());
        assertEquals(userFromList.getAddress(), user.getAddress());
        assertEquals(userFromList.getPhoneNumber(),user.getPhoneNumber());
    }

    @Test
    void updateUserById() {
    }

    @Test
    void givenWrongId_whenPatchUpdateUser_thenReturnException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                ()->userService.patchUpdateUser(87L,any(UserRequest.class)));
    }
//    @Test
//    void givenWrongId_whenPatchUpdateUser_thenReturnException() {
//
//    }

    @Test
    @DisplayName(value = "Delete by existing id")
    void givenCorrectId_whenDeleteUserById_thenPerformDelete() {
        Optional<User> optionalUser = Optional.of(usersList.get(0));
        when(userRepository.findById(anyLong())).thenReturn(optionalUser);
        User user = optionalUser.get();
        doNothing().when(userRepository).delete(user);

        userService.deleteUserById(user.getId());

        verify(userRepository, times(1)).delete(user);
    }

    @Test
    @DisplayName(value = "Delete by wrong id")
    void givenIncorrectId_whenDeleteUserById_thenThrowException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteUserById(anyLong()));
    }
}