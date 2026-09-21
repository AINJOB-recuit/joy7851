package com.ainjob.repository;

import com.ainjob.domain.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StatusRepository extends JpaRepository<Status, Long> {
    Optional<Status> findByCode(String code);
    Optional<Status> findById(Long id);
}
