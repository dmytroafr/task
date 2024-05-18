package com.clearsolutions.task.repository;

import com.clearsolutions.task.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface UserRepository extends JpaRepository<User, Long> {
    Page<User> findAllByBirthDateBetween(LocalDate from, LocalDate to, Pageable pageable);
}
