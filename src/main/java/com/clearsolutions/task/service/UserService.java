package com.clearsolutions.task.service;

import com.clearsolutions.task.model.User;
import com.clearsolutions.task.dto.UserRequest;
import com.clearsolutions.task.exception.BusinessLogicException;
import com.clearsolutions.task.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class UserService {

    private final UserRepository userRepository;
//    private int validAge;
//
//    @Value("${request.age}")
//    private void setValidAge(String age) throws Exception {
//        int ageInt = Optional.of(Integer.parseInt(age))
//                .orElseThrow(() -> new BusinessLogicException("Invalid age set Up"));
//        if (ageInt > 0 && ageInt <= 99) {
//            this.validAge = ageInt;
//        } else {
//            this.validAge = 18;
//            throw new BusinessLogicException("Age has to be >0 <=99, valid age default is 18");
//        }
//    }
//
//    public int getValidAge() {
//        return validAge;
//    }

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public User createUser(UserRequest userRequest) {
        String newEmail = userRequest.getEmail().toLowerCase();

        if (userRepository.existsByEmail(newEmail)) {
            throw new BusinessLogicException("Email address is already in use");
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
                .orElseThrow(() -> new BusinessLogicException("user with id " + id + " not found"));

        String newEmail = userRequest.getEmail().toLowerCase();
        if (userRepository.existsByEmail(newEmail)) {
            throw new BusinessLogicException("Email address is already in use");
        }
        User updatedUser = mapUserRequestToUser(userRequest, user);
        userRepository.save(updatedUser);
    }

    private User mapUserRequestToNewUser(UserRequest userRequest) {
        User newUser = new User();
        return mapUserRequestToUser(userRequest, newUser);
    }

    private User mapUserRequestToUser(UserRequest userRequest, User user) {
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
                .orElseThrow(() -> new BusinessLogicException("user with id " + id + " not found"));
        userRepository.delete(user);
    }

    public void patchUpdateUser(Long id, UserRequest userRequest) {

        User user = userRepository
                .findById(id)
                .orElseThrow(() -> new BusinessLogicException("user with id " + id + " not found"));

        if (userRequest.getBirthDate() != null) {
            user.setBirthDate(userRequest.getBirthDate());
        }
        if (userRequest.getEmail() != null) {
            String newEmail = userRequest.getEmail().toLowerCase();
            if (newEmail.equals(user.getEmail()) || userRepository.existsByEmail(newEmail)) {
                throw new BusinessLogicException("Email address is already in use");
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
        return userRepository
                .findAllByBirthDateBetween(
                        fromDate,
                        toDate,
                        PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()));
    }
}
