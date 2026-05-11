package com.piisw.tod.repository;

import com.piisw.tod.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repozytorium dla encji User.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Wyszukiwanie użytkownika po emailu.
     */
    Optional<User> findByEmail(String email);

    /**
     * Sprawdzenie, czy użytkownik z danym emailem istnieje.
     */
    boolean existsByEmail(String email);
}

