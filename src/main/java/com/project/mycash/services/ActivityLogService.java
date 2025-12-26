package com.project.mycash.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.project.mycash.models.ActivityLog;
import com.project.mycash.models.User;
import com.project.mycash.repositories.ActivityLogRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository repo;

    public List<ActivityLog> getAll(User user) {
        return repo.findByUserOrderByCreatedAtDesc(user);
    }

    public List<ActivityLog> search(User user, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAll(user);
        }
        return repo.findByUserAndActionContainingIgnoreCaseOrderByCreatedAtDesc(user, keyword);
    }

    public void log(User user, String action, String description) {
        ActivityLog log = new ActivityLog();
        log.setUser(user);
        log.setAction(action);
        log.setDescription(description);
        log.setCreatedAt(LocalDateTime.now());

        repo.save(log);
    }

}
