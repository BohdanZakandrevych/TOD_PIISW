package com.piisw.tod.repository;

import com.piisw.tod.model.ContactInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repozytorium dla encji ContactInfo.
 */
@Repository
public interface ContactInfoRepository extends JpaRepository<ContactInfo, Long> {

    /**
     * Wyszukiwanie danych kontaktowych po użytkowniku (user_id).
     */
    List<ContactInfo> findByUserId(Long userId);

    /**
     * Wyszukiwanie danych kontaktowych po typie dla konkretnego użytkownika.
     */
    List<ContactInfo> findByUserIdAndType(Long userId, String type);
}

