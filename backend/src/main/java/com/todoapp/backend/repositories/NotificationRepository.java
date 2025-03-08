package com.todoapp.backend.repositories;

import com.todoapp.backend.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Метод для поиска уведомлений, которые ещё не отправлены и должны быть отправлены до указанного времени
    List<Notification> findByIsSentFalseAndSendAtBefore(LocalDateTime now);

    // Метод для поиска уведомлений по пользователю
    List<Notification> findByUser_Id(Long userId);

    // Метод для поиска уведомлений по статусу отправки
    List<Notification> findByIsSent(boolean isSent);

    List<Notification> findByIsSentFalseAndSendAtBetween(LocalDateTime start, LocalDateTime end);
}