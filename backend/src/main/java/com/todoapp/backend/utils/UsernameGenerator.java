package com.todoapp.backend.utils;

import java.util.Random;

public class UsernameGenerator {

    private static final String[] ADJECTIVES = {"Cool", "Happy", "Lucky", "Smart", "Funny", "Brave", "Wise", "Gentle", "Eager", "Jolly"};
    private static final String[] NOUNS = {"User", "Hero", "Star", "Tiger", "Panda", "Wolf", "Eagle", "Lion", "Bear", "Fox"};
    private static final Random RANDOM = new Random();

    public static String generateRandomUsername() {
        String adjective = ADJECTIVES[RANDOM.nextInt(ADJECTIVES.length)];
        String noun = NOUNS[RANDOM.nextInt(NOUNS.length)];
        int number = RANDOM.nextInt(1000); // Добавляем случайное число для уникальности
        return adjective + noun + number;
    }
}