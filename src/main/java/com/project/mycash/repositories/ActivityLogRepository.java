package com.project.mycash.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.mycash.models.ActivityLog;
import com.project.mycash.models.User;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findByUserOrderByCreatedAtDesc(User user);

    List<ActivityLog> findByUserAndActionContainingIgnoreCaseOrderByCreatedAtDesc(
            User user,
            String keyword);
}
