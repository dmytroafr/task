package com.clearsolutions.task.repository;

import com.clearsolutions.task.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface UserRepository extends JpaRepository<User,Long> {

    boolean existsByEmail(String email);

    List<User> findAllByBirthDateBetween(LocalDate from, LocalDate to);
}
