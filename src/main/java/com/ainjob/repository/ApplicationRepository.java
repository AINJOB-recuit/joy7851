package com.ainjob.repository;

import com.ainjob.domain.ApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<ApplicationEntity, Long> {
    Optional<ApplicationEntity> findByIdAndCompanyId(Long id, Long companyId);
}
