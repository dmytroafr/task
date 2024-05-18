package com.clearsolutions.task.service;

import com.clearsolutions.task.exception.UserNotFoundException;
import com.clearsolutions.task.model.User;
import com.clearsolutions.task.dto.UserRequest;
import com.clearsolutions.task.exception.UserAlreadyExistsException;
import com.clearsolutions.task.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(UserRequest userRequest) {
        String newEmail = userRequest.getEmail().toLowerCase();
        if (userRepository.existsByEmail(newEmail)) {
            throw new UserAlreadyExistsException("Email address is already in use");
        }
        User newUser = mapUserRequestToNewUser(userRequest);
        return userRepository.save(newUser);
    }

    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()));
    }

    public void updateUserById(Long id, UserRequest userRequest) {

        User user = userRepository
                .findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));

        String newEmail = userRequest.getEmail().toLowerCase();
        if (userRepository.existsByEmail(newEmail)) {
            throw new UserAlreadyExistsException("Email address is already in use");
        }
        User updatedUser = mapUserRequestToUser(userRequest, user);
        userRepository.save(updatedUser);
    }

    private User mapUserRequestToNewUser(UserRequest userRequest) {
        User newUser = new User();
        return mapUserRequestToUser(userRequest, newUser);
    }

    private User mapUserRequestToUser(UserRequest userRequest, User user) {
//        if (userRequest.getEmail()!= null)
        user.setEmail(userRequest.getEmail());
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setBirthDate(userRequest.getBirthDate());

        String address = userRequest.getAddress() != null ? userRequest.getAddress() : "";
        user.setAddress(address);

        String phoneNumber = userRequest.getPhoneNumber() != null ? userRequest.getPhoneNumber() : "";
        user.setPhoneNumber(phoneNumber);
        return user;
    }

    public void deleteUserById(Long id) {
        User user = userRepository
                .findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));
        userRepository.delete(user);
    }

    public void patchUpdateUser(Long id, UserRequest userRequest) {

        User user = userRepository
                .findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));

        if (userRequest.getBirthDate() != null) {
            user.setBirthDate(userRequest.getBirthDate());
        }
        if (userRequest.getEmail() != null) {
            String newEmail = userRequest.getEmail().toLowerCase();
            if (newEmail.equals(user.getEmail()) || userRepository.existsByEmail(newEmail)) {
                throw new UserAlreadyExistsException("Email address is already in use");
            }
            user.setEmail(newEmail);
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
        userRepository.save(user);
    }

    public Page<User> getAllUsersWithin(LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("\"From\" date cannot be after \"To\" date");
        }
        return userRepository
                .findAllByBirthDateBetween(
                        fromDate, toDate, PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()));
    }
}
