package com.clearsolutions.task.controller;

import com.clearsolutions.task.model.User;
import com.clearsolutions.task.dto.UserRequest;
import com.clearsolutions.task.exception.UserAlreadyExistsException;
import com.clearsolutions.task.service.UserService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserService userService;

    private static String simpleUserJson;
    private static List<User> usersList;

    static Stream<Arguments> provideJsonData() throws IOException {
        List<String> jsons = Files.readAllLines(Path.of("src/test/resources/invalid_jsons_for_POST.txt"));
        return jsons.stream().map(Arguments::of);
    }

    @BeforeAll
    static void setUp() {
        Random randomYear = new Random();
        usersList =
                IntStream.range(1, 51).mapToObj(i -> User.builder()
                                .id(Long.parseLong(String.valueOf(i)))
                                .email("user" + i + "@gmail.com")
                                .firstName("user" + i + "firstname")
                                .lastName("user" + i + "lastname")
                                .birthDate(LocalDate.parse("19" + randomYear.nextInt(10) + "" + randomYear.nextInt(10) + "-05-25"))
                                .address("City" + i)
                                .phoneNumber("+38095" + i)
                                .build())
                        .toList();
        simpleUserJson = """
                {
                "email":"user98@gmail.com",
                "firstName":"user98First",
                "lastName":"user98Last",
                "birthDate":"1974-05-01",
                "address":"City98",
                "phoneNumber":"+38098"
                }""";
    }

    @Test
    @DisplayName("GET /users - return page")
    void whenGetAllUsers_thenReturnListOfUsers() throws Exception {
        Page<User> userPage = new PageImpl<>(usersList, PageRequest.of(0, 20), usersList.size());
        when(userService.getAllUsers(PageRequest.of(0, 20))).thenReturn(userPage);
        mvc.perform(get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[4]").exists())
                .andExpect(jsonPath("$.pageable.pageSize").value(20))
                .andExpect(jsonPath("$.totalElements").value(50));
    }

    @Test
    @DisplayName("GET /users/range - with correct range - return page")
    void givenCorrectDateRange_whenGetWithinRange_thenReturnPage() throws Exception {
        LocalDate from = LocalDate.parse("1983-01-01");
        LocalDate to = LocalDate.parse("1998-12-31");
        List<User> inRange = usersList
                .stream()
                .filter(user -> user.getBirthDate().isAfter(from)
                        && user.getBirthDate().isBefore(to)).toList();
        Page<User> userPage = new PageImpl<>(inRange, PageRequest.of(0, 20), inRange.size());

        when(userService.getAllUsersWithin(eq(from), eq(to), any(Pageable.class)))
                .thenReturn(userPage);

        mvc.perform(get("/users/range")
                        .param("from", from.toString())
                        .param("to", to.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalElements").value(inRange.size()));
    }

    @Test
    @DisplayName("GET /users/range - with incorrect range - 400_BadRequest")
    void givenIncorrectDateRange_whenGetWithinRange_thenStatusBadRequest() throws Exception {
        // date From is Empty
        mvc.perform(get("/users/range")
                        .param("from", "")
                        .param("to", "1998-12-31"))
                .andExpect(status().isBadRequest());
        // date From is absent
        mvc.perform(get("/users/range")
                        .param("to", "1998-12-31"))
                .andExpect(status().isBadRequest());
        // date To is Empty
        mvc.perform(get("/users/range")
                        .param("from", "1998-12-31")
                        .param("to", ""))
                .andExpect(status().isBadRequest());
        // date To is absent
        mvc.perform(get("/users/range")
                        .param("from", "1998-12-31"))
                .andExpect(status().isBadRequest());
        // absent required params
        mvc.perform(get("/users/range"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /users - with valid json - return 201_Created")
    void givenValidRequest_whenCreateUser_thenStatusOk() throws Exception {

        when(userService.createUser(any(UserRequest.class))).thenReturn(usersList.get(1));

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(simpleUserJson))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(header().string("LOCATION", "http://localhost/users/2"));
    }

    @ParameterizedTest
    @MethodSource("provideJsonData")
    @DisplayName("POST /users - with invalid json/userRequest - return 400_BadRequest")
    void givenInvalidRequest_whenCreateUser_thenBedRequest(String invalidJson) throws Exception {
        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /users/{id} - with correct Id and userRequest - return 204_NoContent")
    void givenCorrectIdAndCorrectJson_whenUpdateUser_thenReturnNoContent() throws Exception {
        doNothing().when(userService).updateUser(anyLong(), any(UserRequest.class));
        mvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(simpleUserJson))
                .andExpect(status().isNoContent());
    }

    @ParameterizedTest
    @MethodSource("provideJsonData")
    @DisplayName("PUT /users/ - with invalid id - return 400_BadRequest")
    void givenAllInvalidData_whenUpdateUser_thenReturnBadRequest(String json) throws Exception {
        doThrow(UserAlreadyExistsException.class).when(userService).updateUser(anyLong(), any(UserRequest.class));

        // invalid json format and userRequest invalid
        mvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(
                        status().isBadRequest());

        // invalid method param
        mvc.perform(put("/users/user"))
                .andExpect(status().isBadRequest());

        // invalid id - exception from userService
        mvc.perform(put("/users/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(simpleUserJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /users/{id} - with correct data - return 204_NoContent")
    void givenCorrectValues_whenPatch_thenReturnNoContent() throws Exception {
        doNothing().when(userService).patchUpdateUser(anyLong(), any(UserRequest.class));

        mvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(simpleUserJson))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PATCH /users/{id} - with incorrect data - return 400_BadRequest")
    void givenInCorrectValues_whenPatch_thenReturnBadRequest() throws Exception {
        doThrow(UserAlreadyExistsException.class).when(userService).patchUpdateUser(anyLong(), any(UserRequest.class));
        mvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /users/{id} - with correct id - return 204_NoContent")
    void givenCorrectId_whenDelete_thenNoContent() throws Exception {
        doNothing().when(userService).deleteUserById(anyLong());

        mvc.perform(delete("/users/{id}", anyLong()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /users/{id} - with wrong id - return 400_Bed_Request ")
    void whenDelete_thenBadRequest() throws Exception {
        doThrow(UserAlreadyExistsException.class).when(userService).deleteUserById(anyLong());
        mvc.perform(delete("/users/{id}", anyLong()))
                .andExpect(status().isBadRequest());
    }
}