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

    public void updateUserById(Long id, UserRequest userRequest) {
        User user = userRepository
                .findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));
        mapUserRequestToUser(userRequest, user);
        user.setAddress(userRequest.getAddress() != null ? userRequest.getAddress() : null);
        user.setPhoneNumber(userRequest.getPhoneNumber() != null ? userRequest.getPhoneNumber() : null);
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
        if (userRequest.getBirthDate() != null) {
            user.setBirthDate(userRequest.getBirthDate());
        }
        if (userRequest.getEmail() != null) {
            user.setEmail(userRequest.getEmail());
        }
        if (userRequest.getFirstName() != null) {
            user.setFirstName(userRequest.getFirstName());
        }
        if (userRequest.getLastName() != null) {
            user.setLastName(userRequest.getLastName());
        }
        if (userRequest.getAddress() != null) {
            user.setAddress(userRequest.getAddress());
        }
        if (userRequest.getPhoneNumber() != null) {
            user.setPhoneNumber(userRequest.getPhoneNumber());
        }
    }
}
