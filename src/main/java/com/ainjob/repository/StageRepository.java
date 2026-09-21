package com.ainjob.repository;

import com.ainjob.domain.Stage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StageRepository extends JpaRepository<Stage, Long> {
    Optional<Stage> findByIdAndCompanyId(Long id, Long companyId);
    Optional<Stage> findByCompanyIdAndName(Long companyId, String name);
}
