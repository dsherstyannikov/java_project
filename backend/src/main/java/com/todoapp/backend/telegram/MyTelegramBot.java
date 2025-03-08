package com.todoapp.backend.telegram;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.todoapp.backend.repositories.UserRepository;
import com.todoapp.backend.models.User;

@Component
public class MyTelegramBot extends TelegramLongPollingBot {

    @Autowired
    private UserRepository userRepository;

    private final String botUsername;
    private final String botToken;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public MyTelegramBot(String botUsername, String botToken) {
        this.botUsername = botUsername;
        this.botToken = botToken;
    }

    @Override
public void onUpdateReceived(Update update) {
    if (update.hasMessage()) {
        Message message = update.getMessage();
        String text = message.getText();
        Long chatId = message.getChatId();

        if (text.startsWith("/register")) {
            String[] parts = text.split(" ");
            if (parts.length == 3) {
                String username = parts[1];
                String password = parts[2];

                // Используем orElse(null) для извлечения значения из Optional
                User user = userRepository.findByUsername(username).orElse(null);
                if (user != null && passwordEncoder.matches(password, user.getPasswordHash())) {
                    user.setTelegramId(chatId);
                    userRepository.save(user);
                    sendMessage(chatId, "Вы успешно зарегистрированы в Telegram!");
                } else {
                    sendMessage(chatId, "Неверное имя пользователя или пароль.");
                }
            } else {
                sendMessage(chatId, "Используйте команду /register username password");
            }
        }
    }
}

    public void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
    
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}