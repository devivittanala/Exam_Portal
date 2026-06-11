package com.qbank.service;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NotificationService {

    private final Map<Long, List<Map<String, String>>> userAlerts = new ConcurrentHashMap<>();

    public synchronized void addNotification(Long studentId, String type, String title, String detail) {
        List<Map<String, String>> alerts = userAlerts.computeIfAbsent(studentId, k -> new ArrayList<>());
        Map<String, String> alert = new HashMap<>();
        alert.put("type", type); // BADGE or SOLVE
        alert.put("title", title);
        alert.put("detail", detail);
        alerts.add(alert);
    }

    public synchronized List<Map<String, String>> fetchAndClearNotifications(Long studentId) {
        List<Map<String, String>> alerts = userAlerts.get(studentId);
        if (alerts == null || alerts.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, String>> unread = new ArrayList<>(alerts);
        alerts.clear();
        return unread;
    }
}