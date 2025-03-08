package com.todoapp.backend.services;

import com.todoapp.backend.models.Notification;
import com.todoapp.backend.models.User;
import com.todoapp.backend.repositories.NotificationRepository;
import com.todoapp.backend.repositories.UserRepository;
import com.todoapp.backend.telegram.MyTelegramBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private MyTelegramBot telegramBot;

    @Autowired
    private UserRepository userRepository;

    // Метод для проверки и отправки уведомлений раз в час
    @Scheduled(fixedRate = 3600 * 10) // 1 час (в миллисекундах)
    public void checkAndSendNotifications() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourLater = now.plusHours(1);

        // Находим уведомления, которые нужно отправить в ближайший час
        List<Notification> notifications = notificationRepository.findByIsSentFalseAndSendAtBetween(now, oneHourLater);

        for (Notification notification : notifications) {
            if (notification.getUser().getTelegramId() != null) {
                // Отправляем уведомление через Telegram бота
                telegramBot.sendMessage(notification.getUser().getTelegramId(), notification.getMessage());
                notification.setSent(true); // Помечаем уведомление как отправленное
                notificationRepository.save(notification); // Сохраняем изменения в базе данных
            }
        }
    }

    // Метод для создания уведомления за час до окончания задачи
    public void createNotificationForTask(User user, String message, LocalDateTime dueDate) {
        if (user.getTelegramId() != null && dueDate != null) {
            Notification notification = new Notification();
            notification.setUser(user);
            notification.setMessage(message);
            notification.setSendAt(dueDate.minusHours(1)); // Уведомление за час до окончания
            notificationRepository.save(notification);
        }
    }
}