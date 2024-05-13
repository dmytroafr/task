package com.clearsolutions.task.controller;

import com.clearsolutions.task.User;
import com.clearsolutions.task.dto.UserRequest;
import com.clearsolutions.task.exception.BusinessLogicException;
import com.clearsolutions.task.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.List;

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

    @Test
    void whenGetAllUsers_thenReturnListOfUsers() throws Exception {
        Mockito.when(userService.getAllUsers()).thenReturn(List.of(simpleUser));

        mvc.perform(get("/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()").value(1));
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

        ResultActions response = mvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email":"afrosin.dmytro@gmail.com","firstName":"Dmytro","lastName":"Afrosin","birthDate":"2006-05-01","address":"Kyiv","phoneNumber":"+380"}"""));

        response.andExpect(status().isCreated())
                .andDo(print());
    }@Test
    void givenInCorrectUserRequest_whenCreateUser_thenStatusOk() throws Exception {

        mvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email":"afrosin.dm@ytro@gmail.com","firstName":"Dmytro","lastName":"Afrosin","birthDate":"2006-05-01","address":"Kyiv","phoneNumber":"+380"}"""))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"email":"","firstName":"Dmytro","lastName":"Afrosin","birthDate":"2006-05-01","address":"Kyiv","phoneNumber":"+380"}"""))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"firstName":"Dmytro","lastName":"Afrosin","birthDate":"2006-05-01","address":"Kyiv","phoneNumber":"+380"}"""))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"email":"afrosin.dmytro@gmail.com","firstName":"","lastName":"Afrosin","birthDate":"2006-05-01","address":"Kyiv","phoneNumber":"+380"}"""))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"email":"afrosin.dmytro@gmail.com","lastName":"Afrosin","birthDate":"2006-05-01","address":"Kyiv","phoneNumber":"+380"}"""))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"email":"afrosin.dmytro@gmail.com","firstName":"Dmytro","lastName":"","birthDate":"2006-05-01","address":"Kyiv","phoneNumber":"+380"}"""))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"email":"afrosin.dmytro@gmail.com","firstName":"Dmytro","birthDate":"2006-05-01","address":"Kyiv","phoneNumber":"+380"}"""))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"email":"afrosin.dmytro@gmail.com","firstName":"Dmytro","lastName":"Afrosin","birthDate":"2025-05-01","address":"Kyiv","phoneNumber":"+380"}"""))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"email":"afrosin.dmytro@gmail.com","firstName":"Dmytro","lastName":"Afrosin","birthDate":"2005-13-42","address":"Kyiv","phoneNumber":"+380"}"""))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"email":"afrosin.dmytro@gmail.com","firstName":"Dmytro","lastName":"Afrosin","birthDate":"","address":"Kyiv","phoneNumber":"+380"}"""))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"email":"afrosin.dmytro@gmail.com","firstName":"Dmytro","lastName":"Afrosin","address":"Kyiv","phoneNumber":"+380"}"""))
                .andExpect(status().isBadRequest());

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
                .andExpect(status().isNoContent());
    }
    @Test
    void givenInCorrectValues_whenUpdateUser_thenReturnBadRequest() throws Exception {
        String updatedUserJson = """
            {
                "email": "updated@gmail.com",
                "firstName": "updated"
                "lastName": "updated",
                "birthDate": "1989-05-01"
            }
            """;
        mvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedUserJson))
                .andExpect(status().isBadRequest());

        mvc.perform(put("/users/user"))
                .andExpect(status().isBadRequest());

        mvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"email":"afrosin.dm@ytro@gmail.com","firstName":"Dmytro","lastName":"Afrosin","birthDate":"2006-05-01","address":"Kyiv","phoneNumber":"+380"}"""))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"email":"","firstName":"Dmytro","lastName":"Afrosin","birthDate":"2006-05-01","address":"Kyiv","phoneNumber":"+380"}"""))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"firstName":"Dmytro","lastName":"Afrosin","birthDate":"2006-05-01","address":"Kyiv","phoneNumber":"+380"}"""))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"email":"afrosin.dmytro@gmail.com","firstName":"","lastName":"Afrosin","birthDate":"2006-05-01","address":"Kyiv","phoneNumber":"+380"}"""))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"email":"afrosin.dmytro@gmail.com","lastName":"Afrosin","birthDate":"2006-05-01","address":"Kyiv","phoneNumber":"+380"}"""))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"email":"afrosin.dmytro@gmail.com","firstName":"Dmytro","lastName":"","birthDate":"2006-05-01","address":"Kyiv","phoneNumber":"+380"}"""))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"email":"afrosin.dmytro@gmail.com","firstName":"Dmytro","birthDate":"2006-05-01","address":"Kyiv","phoneNumber":"+380"}"""))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"email":"afrosin.dmytro@gmail.com","firstName":"Dmytro","lastName":"Afrosin","birthDate":"2025-05-01","address":"Kyiv","phoneNumber":"+380"}"""))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"email":"afrosin.dmytro@gmail.com","firstName":"Dmytro","lastName":"Afrosin","birthDate":"2005-13-42","address":"Kyiv","phoneNumber":"+380"}"""))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"email":"afrosin.dmytro@gmail.com","firstName":"Dmytro","lastName":"Afrosin","birthDate":"","address":"Kyiv","phoneNumber":"+380"}"""))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"email":"afrosin.dmytro@gmail.com","firstName":"Dmytro","lastName":"Afrosin","address":"Kyiv","phoneNumber":"+380"}"""))
                .andExpect(status().isBadRequest());

        Mockito.doThrow(BusinessLogicException.class).when(userService).updateUserById(eq(999L),any());
        mvc.perform(put("/users/999")
                .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"email":"q@gmail.com","firstName":"q","lastName":"q","birthDate":"2003-05-01"}"""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenCorrectValues_whenPatch_thenReturnNoContent() throws Exception {
        Mockito.doNothing().when(userService).patchUpdateUser(eq(999L),any());
        mvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"email":"afrosin.dmytro@gmail.com"}"""))
                .andExpect(status().isNoContent());
    }
    @Test
    void givenInCorrectValues_whenPatch_thenReturnBadRequest() throws Exception {
        Mockito.doThrow(BusinessLogicException.class).when(userService).patchUpdateUser(any(),any());
        mvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenCorrectId_whenDelete_thenNoContent() throws Exception {
        Mockito.doNothing().when(userService).deleteUserById(1L);

        mvc.perform(delete("/users/{id}", 1L))
                .andExpect(status().isNoContent());
    }
    @Test
    void whenDelete_thenBadRequest() throws Exception {
        Mockito.doThrow(BusinessLogicException.class).when(userService).deleteUserById(999L);
        mvc.perform(delete("/users/{id}", 999L))
                .andExpect(status().isBadRequest());
    }
}