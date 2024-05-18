package com.clearsolutions.task.controller;

import com.clearsolutions.task.model.User;
import com.clearsolutions.task.validation.PatchValidation;
import com.clearsolutions.task.dto.UserRequest;
import com.clearsolutions.task.service.UserService;
import com.clearsolutions.task.validation.PutValidation;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    public ResponseEntity<Page<User>> getAllUsers(Pageable pageable) {
        Page<User> allUsers = userService.getAllUsers(pageable);
        return ResponseEntity.ok(allUsers);
    }

    @GetMapping("/range")
    public ResponseEntity<Page<User>> getAllUsersInRange(
            @RequestParam(name = "from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @NotNull LocalDate fromDate,
            @RequestParam(name = "to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @NotNull LocalDate toDate,
            Pageable pageable) {
        Page<User> allUsers = userService.getAllUsersWithin(fromDate, toDate, pageable);
        return ResponseEntity.ok(allUsers);
    }

    @PostMapping
    public ResponseEntity<Void> createUser(@Validated(PutValidation.class) @RequestBody UserRequest userRequest,
                                           UriComponentsBuilder uriBuilder) {
        User user = userService.createUser(userRequest);
        URI location = uriBuilder
                .path("/users/{id}")
                .buildAndExpand(user.getId())
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateUser(@PathVariable Long id,
                                           @Validated(PutValidation.class) @RequestBody UserRequest userRequest) {
        userService.updateUserById(id, userRequest);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> patchUser(@PathVariable Long id,
                                          @Validated(PatchValidation.class) @RequestBody UserRequest userRequest) {
        userService.patchUpdateUser(id, userRequest);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }

}
