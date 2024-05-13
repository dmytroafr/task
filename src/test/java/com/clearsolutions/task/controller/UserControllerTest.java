package com.clearsolutions.task.controller;

import com.clearsolutions.task.User;
import com.clearsolutions.task.dto.UserRequest;
import com.clearsolutions.task.exception.BusinessLogicException;
import com.clearsolutions.task.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserService userService;

    private static final Long ID = 1L;
    private User simpleUser;


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
    void whenGetAllUsers_thenReturnListOfUsers() throws Exception {
        Mockito.when(userService.getAllUsers()).thenReturn(List.of(simpleUser));

        mvc.perform(get("/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.size()").value(1)
                );
    }

    @Test
    void givenCorrectDateRange_whenGetWithinRange_thenReturnList() throws Exception {
        Mockito.when(userService.getAllUsersWithin(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(simpleUser));

        mvc.perform(get("/users/range")
                        .param("from","1993-01-01")
                        .param("to","1998-12-31"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(simpleUser.getId()))
                .andExpect(jsonPath("$[0].email").exists());
    }
    @Test
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
    void givenCorrectUserRequest_whenCreateUser_thenStatusOk() throws Exception {
        Mockito.when(userService.registerUser(any())).thenReturn(simpleUser);

        mvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email":"afrosin.dmytro@gmail.com","firstName":"Dmytro","lastName":"Afrosin","birthDate":"2004-05-01","address":"Kyiv","phoneNumber":"+380"}"""))
                .andExpectAll(
                        status().isCreated(),
                        header().exists(HttpHeaders.LOCATION)
                );

    }
    @ParameterizedTest
    @MethodSource("provideJsonData")
    void givenInCorrectUserRequest_whenCreateUser_thenStatusOk(String badJson) throws Exception {

        mvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(badJson))
                .andExpect(
                        status().isBadRequest());
    }

    @Test
    void givenCorrectIdAndCorrectJson_whenUpdateUser_thenReturnNoContent() throws Exception {
        String updatedUserJson = """
            {
                "email": "updated@gmail.com",
                "firstName": "updated",
                "lastName": "updated",
                "birthDate": "1989-05-01"
            }
            """;
        Mockito.doNothing().when(userService).updateUserById(eq(1L),any(UserRequest.class));
        mvc.perform(put("/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatedUserJson))
                .andExpect(
                        status().isNoContent());
    }

    @ParameterizedTest
    @MethodSource("provideJsonData")
    void givenInCorrectValues_whenUpdateUser_thenReturnBadRequest(String json) throws Exception {
        Mockito.doThrow(BusinessLogicException.class).when(userService).updateUserById(eq(999L),any());

        mvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(
                        status().isBadRequest());

        mvc.perform(put("/users/user"))
                .andExpect(
                        status().isBadRequest());

        mvc.perform(put("/users/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"email":"q@gmail.com","firstName":"q","lastName":"q","birthDate":"2003-05-01"}"""))
                .andExpect(
                        status().isBadRequest());
    }

    @Test
    void givenCorrectValues_whenPatch_thenReturnNoContent() throws Exception {
        Mockito.doNothing().when(userService).patchUpdateUser(eq(999L),any());
        mvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"email":"afrosin.dmytro@gmail.com"}"""))
                .andExpect(
                        status().isNoContent());
    }
    @Test
    void givenInCorrectValues_whenPatch_thenReturnBadRequest() throws Exception {
        Mockito.doThrow(BusinessLogicException.class).when(userService).patchUpdateUser(any(),any());
        mvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(
                        status().isBadRequest());
    }

    @Test
    void givenCorrectId_whenDelete_thenNoContent() throws Exception {
        Mockito.doNothing().when(userService).deleteUserById(1L);

        mvc.perform(delete("/users/{id}", 1L))
                .andExpect(
                        status().isNoContent());
    }
    @Test
    void whenDelete_thenBadRequest() throws Exception {
        Mockito.doThrow(BusinessLogicException.class).when(userService).deleteUserById(999L);
        mvc.perform(delete("/users/{id}", 999L))
                .andExpect(
                        status().isBadRequest());
    }
}