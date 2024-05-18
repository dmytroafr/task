package com.clearsolutions.task.repository;

import com.clearsolutions.task.model.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class UserRepositoryTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    public void setUp() {
        IntStream.range(1, 10).mapToObj(i -> User.builder()
                        .email("email" + i + "@gmail.com")
                        .firstName("Afrosin")
                        .lastName("Dmytro")
                        .birthDate(LocalDate.parse("199" + i + "-05-25"))
                        .address("Kyiv")
                        .phoneNumber("+38095")
                        .build())
                .toList()
                .forEach(entityManager::persistAndFlush);
    }

    @Test
    public void whenFindAllByBirthDateBetween_ThenOne() {
        Page<User> allByBirthDateBetween = userRepository.findAllByBirthDateBetween(
                LocalDate.parse("1995-01-01"),
                LocalDate.parse("1995-12-31"),
                PageRequest.of(0, 5));

        assertNotNull(allByBirthDateBetween);
        assertEquals(1, allByBirthDateBetween.getTotalElements());
    }

    @Test
    public void whenFindAllByBirthDateBetween_ThenThree() {
        Page<User> allByBirthDateBetween = userRepository.findAllByBirthDateBetween(
                LocalDate.parse("1995-01-01"),
                LocalDate.parse("1997-12-31"),
                PageRequest.of(0,5));

        assertNotNull(allByBirthDateBetween);
        assertEquals(3, allByBirthDateBetween.getTotalElements());
    }
}
