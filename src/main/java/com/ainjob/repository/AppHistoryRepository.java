package com.ainjob.repository;

import com.ainjob.domain.AppHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppHistoryRepository extends JpaRepository<AppHistory, Long> {
}
