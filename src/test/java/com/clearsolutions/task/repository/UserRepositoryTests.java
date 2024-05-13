package com.clearsolutions.task.repository;

import com.clearsolutions.task.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;
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
    public void givenExistentEmail_whenExistsByEmail_thanTrue() {
        boolean exists = userRepository.existsByEmail("email1@gmail.com");
        assertTrue(exists);
    }

    @Test
    public void givenNotExistentEmail_whenExistsByEmail_thanFalse() {
        boolean exists = userRepository.existsByEmail("unexistent.email@gmail.com");
        assertFalse(exists);
    }

    @Test
    public void givenCorrectRange_whenFindAllByBirthDateBetween_ThenOne() {
        List<User> allByBirthDateBetween = userRepository.findAllByBirthDateBetween(
                LocalDate.parse("1995-01-01"), LocalDate.parse("1995-12-31"));

        assertNotNull(allByBirthDateBetween);
        assertEquals(1, allByBirthDateBetween.size());
    }

    @Test
    public void givenCorrectRange_whenFindAllByBirthDateBetween_ThenThree() {
        List<User> allByBirthDateBetween = userRepository.findAllByBirthDateBetween(
                LocalDate.parse("1995-01-01"), LocalDate.parse("1997-12-31"));

        assertNotNull(allByBirthDateBetween);
        assertEquals(3, allByBirthDateBetween.size());
    }

    @Test
    public void givenIncorrectRange_whenFindAllByBirthDateBetween_ThenEmptyList() {
        List<User> allByBirthDateBetween = userRepository.findAllByBirthDateBetween(
                LocalDate.parse("1996-01-01"), LocalDate.parse("1993-12-31"));

        assertEquals(0, allByBirthDateBetween.size());
    }
}
