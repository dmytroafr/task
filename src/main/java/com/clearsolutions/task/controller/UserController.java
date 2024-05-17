package com.clearsolutions.task.controller;

import com.clearsolutions.task.User;
import com.clearsolutions.task.dto.PatchValidation;
import com.clearsolutions.task.dto.UserRequest;
import com.clearsolutions.task.exception.BusinessLogicException;
import com.clearsolutions.task.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.Optional;

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
    public ResponseEntity<Page<User>> getAllUsersInRange(@RequestParam(name = "from")
                                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                                         @RequestParam(name = "to")
                                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                                                         Pageable pageable) {
        if (fromDate.isAfter(toDate)) {
            throw new BusinessLogicException ("Invalid date range");
        }
        Page<User> allUsers = userService.getAllUsersWithin(fromDate, toDate, pageable);
        return ResponseEntity.ok(allUsers);
    }

    @PostMapping
    public ResponseEntity<Void> createUser(@Valid @RequestBody UserRequest userRequest, UriComponentsBuilder uriBuilder) {

        if (userRequest.getBirthDate().plusYears(userService.getValidAge()).isAfter(LocalDate.now())) {
            throw new BusinessLogicException ("Invalid birth date");
        }

        User user = userService.registerUser(userRequest);
        URI location = uriBuilder
                .path("/users/{id}")
                .buildAndExpand(user.getId())
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateUser(@PathVariable Long id,
                                           @Valid @RequestBody UserRequest userRequest) {

        if (userRequest.getBirthDate().plusYears(userService.getValidAge()).isAfter(LocalDate.now())) {
            throw new BusinessLogicException ("Invalid birth date");
        }
        userService.updateUserById(id, userRequest);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> patchUser(@PathVariable Long id,
                                         @Validated(PatchValidation.class) @RequestBody UserRequest userRequest){
        Optional<LocalDate> localDateOptional = Optional.ofNullable(userRequest.getBirthDate());
        if (localDateOptional.isPresent()
            && userRequest.getBirthDate().plusYears(userService.getValidAge()).isAfter(LocalDate.now())) {
            throw new BusinessLogicException ("Invalid birth date");
        }
        userService.patchUpdateUser(id, userRequest);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }

}
