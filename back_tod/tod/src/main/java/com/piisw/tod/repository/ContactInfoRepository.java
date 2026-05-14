package com.piisw.tod.repository;

import com.piisw.tod.model.ContactInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactInfoRepository extends JpaRepository<ContactInfo, Long> {

    List<ContactInfo> findByUserId(Long userId);

    List<ContactInfo> findByUserIdAndType(Long userId, String type);
}
