package com.clearsolutions.task.service;

import com.clearsolutions.task.User;
import com.clearsolutions.task.dto.UserRequest;
import com.clearsolutions.task.exception.BusinessLogicException;
import com.clearsolutions.task.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public int validAge;

    @Value("${request.age}")
    private void setValidAge(String age) {
        try {
            int ageInt = Integer.parseInt(age);
            if (ageInt > 0 && ageInt <= 99) {
                this.validAge = ageInt;
            }
        } catch (NumberFormatException e) {
            throw new BusinessLogicException("Invalid age set Up");
        }
    }

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    private void ageValidator(LocalDate birthDate) {
        if (LocalDate.now().minusYears(validAge).isBefore(birthDate)){
            throw new BusinessLogicException("user must be older than " + validAge + " years");
        }
    }

    public User registerUser(UserRequest userRequest) {

        ageValidator(userRequest.getBirthDate());
        String newEmail = userRequest.getEmail().toLowerCase();
        if (userRepository.existsByEmail(newEmail)) {
            throw new BusinessLogicException("Email address is already in use");
        }
        User newUser = new User();
        mapUserRequestToUser(userRequest, newUser);
        return userRepository.save(newUser);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void updateUserById(Long id, UserRequest userRequest) {

        User user = userRepository
                .findById(id)
                .orElseThrow(() -> new BusinessLogicException("user with id " + id + " not found"));

        ageValidator(userRequest.getBirthDate());

        String newEmail = userRequest.getEmail().toLowerCase();
        if (userRepository.existsByEmail(newEmail)) {
            throw new BusinessLogicException("Email address is already in use");
        }
        mapUserRequestToUser(userRequest, user);
        userRepository.save(user);
    }

    private static void mapUserRequestToUser(UserRequest userRequest, User user) {
        user.setEmail(userRequest.getEmail());
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setBirthDate(userRequest.getBirthDate());
        String address = userRequest.getAddress() != null ? userRequest.getAddress() : "";
        user.setAddress(address);
        String phoneNumber = userRequest.getPhoneNumber() != null ? userRequest.getPhoneNumber() : "";
        user.setPhoneNumber(phoneNumber);
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
            ageValidator(userRequest.getBirthDate());
            user.setBirthDate(userRequest.getBirthDate());
        }
        if(userRequest.getEmail() != null) {
            String newEmail = userRequest.getEmail().toLowerCase();
//            if (!Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$")
//                    .matcher(newEmail)
//                    .matches()){
//                throw new BusinessLogicException("email is not valid");
//            }

            if (newEmail.equals(user.getEmail()) || userRepository.existsByEmail(newEmail)) {
                throw new BusinessLogicException("Email address is already in use");
            }
            user.setEmail(newEmail);
        }

        if(userRequest.getFirstName() != null) {
            user.setFirstName(userRequest.getFirstName());
        }

        if(userRequest.getLastName() != null) {
            user.setLastName(userRequest.getLastName());
        }

        if(userRequest.getAddress() != null) {
            user.setAddress(userRequest.getAddress());
        }

        if(userRequest.getPhoneNumber() != null) {
            user.setPhoneNumber(userRequest.getPhoneNumber());
        }
        userRepository.save(user);
    }

    public List<User> getAllUsersWithin(LocalDate fromDate, LocalDate toDate) {
        return userRepository.findAllByBirthDateBetween(fromDate, toDate);
    }
}
