package com.clearsolutions.task.service;

import com.clearsolutions.task.exception.UserNotFoundException;
import com.clearsolutions.task.model.User;
import com.clearsolutions.task.dto.UserRequest;
import com.clearsolutions.task.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()));
    }

    public Page<User> getAllUsersWithin(LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("\"From\" date cannot be after \"To\" date");
        }
        return userRepository.findAllByBirthDateBetween(fromDate, toDate, pageable);
    }

    public User createUser(UserRequest userRequest) {
        User newUser = mapUserRequestToNewUser(userRequest);
        return userRepository.save(newUser);
    }

    public void updateUser(Long id, UserRequest userRequest) {
        User user = userRepository
                .findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));

        mapUserRequestToUser(userRequest, user);
        if (Objects.isNull(userRequest.getAddress())) {
            user.setAddress(null);
        }
        if (Objects.isNull(userRequest.getPhoneNumber())) {
            user.setPhoneNumber(null);
        }

        userRepository.save(user);
    }

    public void patchUpdateUser(Long id, UserRequest userRequest) {
        User user = userRepository
                .findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));
        mapUserRequestToUser(userRequest, user);
        userRepository.save(user);
    }

    public void deleteUserById(Long id) {
        User user = userRepository
                .findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));
        userRepository.delete(user);
    }

    private User mapUserRequestToNewUser(UserRequest userRequest) {
        User newUser = new User();
        mapUserRequestToUser(userRequest, newUser);
        return newUser;
    }

    private void mapUserRequestToUser(UserRequest userRequest, User user) {
        Optional.ofNullable(userRequest.getEmail()).ifPresent(user::setEmail);
        Optional.ofNullable(userRequest.getFirstName()).ifPresent(user::setFirstName);
        Optional.ofNullable(userRequest.getLastName()).ifPresent(user::setLastName);
        Optional.ofNullable(userRequest.getBirthDate()).ifPresent(user::setBirthDate);
        Optional.ofNullable(userRequest.getAddress()).ifPresent(user::setAddress);
        Optional.ofNullable(userRequest.getPhoneNumber()).ifPresent(user::setPhoneNumber);
    }
}
