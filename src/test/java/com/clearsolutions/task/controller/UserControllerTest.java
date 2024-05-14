package com.clearsolutions.task.controller;

import com.clearsolutions.task.User;
import com.clearsolutions.task.dto.UserRequest;
import com.clearsolutions.task.exception.BusinessLogicException;
import com.clearsolutions.task.service.UserService;
import org.junit.jupiter.api.BeforeEach;
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

    private static final Long ID = 1L;
    private User simpleUser;
    private final String simpleUserJson = """
                                    {
                                    "email":"afrosin.dmytro@gmail.com",
                                    "firstName":"Dmytro",
                                    "lastName":"Afrosin",
                                    "birthDate":"2004-05-01",
                                    "address":"Kyiv",
                                    "phoneNumber":"+380"
                                    }""";


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
    }
    static Stream<Arguments> provideJsonData() throws IOException{
        List<String> jsons = Files.readAllLines(Path.of("src/test/resources/BadJson.txt"));
        return jsons.stream().map(Arguments::of);
    }

    @Test
    @DisplayName("GET /users - return page")
    void whenGetAllUsers_thenReturnListOfUsers() throws Exception {
        Page<User> userPage = new PageImpl<>(List.of(simpleUser));
        when(userService.getAllUsers(any())).thenReturn(userPage);

        mvc.perform(get("/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.content.size()").value(1)
                );
    }

    @Test
    @DisplayName("GET /users/range - with correct range - return page")
    void givenCorrectDateRange_whenGetWithinRange_thenReturnPage() throws Exception {
        Page<User> userPage = new PageImpl<>(List.of(simpleUser));
        when(userService.getAllUsersWithin(any(LocalDate.class), any(LocalDate.class),any(Pageable.class)))
                .thenReturn(userPage);

        mvc.perform(get("/users/range")
                        .param("from","1993-01-01")
                        .param("to","1998-12-31")
                        .param("page","0")
                        .param("size","5"))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.content[0].id").value(simpleUser.getId()),
                        jsonPath("$.content[0].email").exists()
                );
    }
    @Test
    @DisplayName("GET /users/range - with incorrect range - 400_BadRequest")
    void givenIncorrectDateRange_whenGetWithinRange_thenStatusBadRequest() throws Exception {
        mvc.perform(get("/users/range")
                        .param("from","1999-12-31")
                        .param("to","1998-12-31"))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/users/range")
                        .param("from","")
                        .param("to","1998-12-31"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/users/range")
                        .param("to","1998-12-31"))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/users/range")
                        .param("from","1998-12-31")
                        .param("to",""))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/users/range")
                        .param("from","1998-12-31"))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/users/range"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /users - with valid json - return 201_Created")
    void givenValidRequest_whenCreateUser_thenStatusOk() throws Exception {
        when(userService.registerUser(any(UserRequest.class))).thenReturn(simpleUser);

        mvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(simpleUserJson))
                .andExpectAll(
                        status().isCreated(),
                        header().exists(HttpHeaders.LOCATION)
                );
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
    @DisplayName("POST PUT PATCH /users/{id} - with invalid age - return 400_BedRequest")
    void givenInvalidAge_whenSendRequest_thenStatusBadRequest() throws Exception {
        when(userService.getValidAge()).thenReturn(18);
        mvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                    {
                                    "email":"afrosin.dmytro@gmail.com",
                                    "firstName":"Dmytro",
                                    "lastName":"Afrosin",
                                    "birthDate":"2008-05-01"
                                    }"""))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/users/{id}",1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                    "email":"afrosin.dmytro@gmail.com",
                                    "firstName":"Dmytro",
                                    "lastName":"Afrosin",
                                    "birthDate":"2008-05-01"
                                    }"""))
                .andExpect(status().isBadRequest());
        mvc.perform(patch("/users/{id}",1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                    "birthDate":"2008-05-01"
                                    }"""))
                .andExpect(status().isBadRequest());
        mvc.perform(patch("/users/{id}",1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PUT /users/{id} - with correct Id and userRequest - return 204_NoContent")
    void givenCorrectIdAndCorrectJson_whenUpdateUser_thenReturnNoContent() throws Exception {
        doNothing().when(userService).updateUserById(eq(1L),any(UserRequest.class));

        mvc.perform(put("/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(simpleUserJson))
                .andExpect(status().isNoContent());
    }

    @ParameterizedTest
    @MethodSource("provideJsonData")
    @DisplayName("PUT /users/ - with invalid id - return 400_BadRequest")
    void givenAllInvalidData_whenUpdateUser_thenReturnBadRequest(String json) throws Exception {
        doThrow(BusinessLogicException.class).when(userService).updateUserById(eq(999L),any());

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
        doNothing().when(userService).patchUpdateUser(any(),any());

        mvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(simpleUserJson))
                .andExpect(status().isNoContent());
    }
    @Test
    @DisplayName("PATCH /users/{id} - with incorrect data - return 400_BadRequest")
    void givenInCorrectValues_whenPatch_thenReturnBadRequest() throws Exception {
        doThrow(BusinessLogicException.class).when(userService).patchUpdateUser(any(),any());
        mvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /users/{id} - with correct id - return 204_NoContent")
    void givenCorrectId_whenDelete_thenNoContent() throws Exception {
        doNothing().when(userService).deleteUserById(1L);

        mvc.perform(delete("/users/{id}", 1L))
                .andExpect(status().isNoContent());
    }
    @Test
    @DisplayName("DELETE /users/{id} - with wrong id - return 400_Bed_Request ")
    void whenDelete_thenBadRequest() throws Exception {
        doThrow(BusinessLogicException.class).when(userService).deleteUserById(999L);
        mvc.perform(delete("/users/{id}", 999L))
                .andExpect(status().isBadRequest());
    }
}