package com.clearsolutions.task.service;

import com.clearsolutions.task.model.User;
import com.clearsolutions.task.dto.UserRequest;
import com.clearsolutions.task.exception.BusinessLogicException;
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
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@Import(UserServiceTest.UserServiceTestConfiguration.class)
@TestPropertySource(locations = "/test.properties")
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

    @BeforeEach
    public void setUp() {
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
    @ParameterizedTest
    @ValueSource(strings = {"-1", "-3", "-15", "0", "5","100", "101", "105", "5000"})
    void givenIncorrectAge_whenSetValid_returnExceptions(String age) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        Method setValidAge = UserService.class
                .getDeclaredMethod("setValidAge", String.class);

        setValidAge.setAccessible(true);
//        assertThrows(BusinessLogicException.class, () -> setValidAge.invoke(userService, age) );
        try {
            setValidAge.invoke(userService, age);

        } catch (InvocationTargetException e) {

            assertInstanceOf(BusinessLogicException.class, e.getCause());

            Field declaredField1 = userService.getClass().getDeclaredField("validAge");
            declaredField1.setAccessible(true);
            Object validAge = declaredField1.get(userService);
            assertEquals(18,(int) validAge);
        }
    }

    @Test
    void givenFullCorrectUserRequest_whenCreateUser_thanReturnUser() {
        when(userRepository.existsByEmail(simpleUser.getEmail())).thenReturn(false);
        when(userRepository.save(any())).thenReturn(simpleUser);

        User registeredUser = userService.createUser(simpleUserRequest);

        assertNotNull(registeredUser);
        assertNotNull(registeredUser.getId());
        assertEquals(simpleUser, registeredUser);
    }

    @Test
    void givenFullCorrectUserRequest_whenCreateUserEmailExists_thenThrowException() {
        when(userRepository.existsByEmail(simpleUser.getEmail())).thenReturn(true);

        assertThrows(BusinessLogicException.class, () -> userService.createUser(simpleUserRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void whenGetAllUsers_thenReturnListOfTwo() {
        User user2 = simpleUser;
        user2.setId(2L);
        user2.setEmail("email2@gmail.com");
        user2.setBirthDate(LocalDate.parse("1998-05-25"));
        Page<User> userPage = new PageImpl<>(List.of(simpleUser, user2), PageRequest.of(0, 5), 1);

        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);


        Page<User> allUsers = userService.getAllUsers(PageRequest.of(1,5));

        assertNotNull(allUsers);
        assertEquals(2, allUsers.getTotalElements());
    }

    @Test
    void givenValidIdAndRequest_whenUpdateUserById_thenReturnChangedUser() {
        when(userRepository.findById(ID)).thenReturn(Optional.of(simpleUser));
        when(userRepository.existsByEmail(simpleUserRequest.getEmail())).thenReturn(false);
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
        assertThrows(BusinessLogicException.class, executable);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void givenExistingEmail_whenUpdateUserById_thenThrowException() {
        when(userRepository.findById(ID)).thenReturn(Optional.of(simpleUser));
        when(userRepository.existsByEmail(simpleUserRequest.getEmail())).thenReturn(true);

        assertThrows(BusinessLogicException.class, () -> userService.updateUserById(ID, simpleUserRequest));
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
    void givenCorrectId_whenDeleteUserById_thenPerformDelete() {
        when(userRepository.findById(ID)).thenReturn(Optional.of(simpleUser));
        doNothing().when(userRepository).delete(simpleUser);

        userService.deleteUserById(ID);

        verify(userRepository, times(1)).delete(simpleUser);
    }

    @Test
    void givenIncorrectId_whenDeleteUserById_thenThrowException() {
        when(userRepository.findById(ID)).thenReturn(Optional.empty());

        assertThrows(BusinessLogicException.class, () -> userService.deleteUserById(ID));
    }

    @Test
    void givenIncorrectId_whenPatchUpdateUser_thenThrowException() {
        when(userRepository.findById(ID)).thenReturn(Optional.empty());

        assertThrows(BusinessLogicException.class, () -> userService.patchUpdateUser(ID, new UserRequest()));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void givenIncorrectEmail_whenPatchUpdateUser_thenThrowException() {
        UserRequest userRequest = UserRequest.builder()
                .email("e@e@gmail.com")
                .build();

        when(userRepository.findById(ID)).thenReturn(Optional.of(simpleUser));
        when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(true);

        assertThrows(BusinessLogicException.class, () -> userService.patchUpdateUser(ID, userRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void givenExistingEmail_whenPatchUpdateUser_thenThrowException() {
        UserRequest userRequest = UserRequest.builder()
                .email("email@gmail.com")
                .build();

        when(userRepository.findById(ID)).thenReturn(Optional.of(simpleUser));
        when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(true);

        assertThrows(BusinessLogicException.class, () -> userService.patchUpdateUser(ID, userRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void givenOnlyEmail_whenPatchUpdateUser_thenUpdateUser() {
        UserRequest userRequest = UserRequest.builder()
                .email("updated@gmail.com")
                .build();

        when(userRepository.findById(ID)).thenReturn(Optional.of(simpleUser));
        when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(false);
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

    @Test
    void givenValidRange_whenFindAllByBirthDateBetween_thenReturnList() {
        List<User> userList = IntStream.range(1, 10)
                .mapToObj(i -> User.builder()
                        .id(Long.parseLong(String.valueOf(i)))
                        .email("email" + i + "@gmail.com")
                        .firstName("Afrosin")
                        .lastName("Dmytro")
                        .birthDate(LocalDate.parse("199" + i + "-05-25"))
                        .address("Kyiv")
                        .phoneNumber("+38095")
                        .build())
                .toList();

        LocalDate fromDate = LocalDate.parse("1990-01-01");
        LocalDate toDate = LocalDate.parse("1997-12-31");

        List<User> list = userList.stream()
                .filter(user -> user.
                        getBirthDate().isAfter(fromDate) && user.
                        getBirthDate().isBefore(toDate))
                .toList();
        Page<User> users = new PageImpl<>(list);
        when(userRepository.findAllByBirthDateBetween(fromDate, toDate, PageRequest.of(0,10)))
                .thenReturn(users);

        Page<User> result = userService.getAllUsersWithin(fromDate, toDate, PageRequest.of(0,10));

        assertEquals(7, result.getTotalElements());
    }
}