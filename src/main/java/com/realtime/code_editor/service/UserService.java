package com.realtime.code_editor.service;

import com.realtime.code_editor.model.User;
import com.realtime.code_editor.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    private static final String[] COLORS = {
            "#FF6B6B", "#4ECDC4", "#45B7D1", "#FFA07A",
            "#98D8C8", "#F7DC6F", "#BB8FCE", "#85C1E2",
            "#F8B739", "#6C5CE7", "#00B894", "#FD79A8"
    };

    public User createOrGetUser(String username) {
        Optional<User> existingUser = userRepository.findByUsername(username);

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setOnline(true);
            return userRepository.save(user);
        }

        // Create new user with random color
        User newUser = User.builder()
                .username(username)
                .color(getRandomColor())
                .online(true)
                .build();

        return userRepository.save(newUser);
    }

    public Optional<User> getUser(Long userId) {
        return userRepository.findById(userId);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> getAllOnlineUsers() {
        return userRepository.findAll().stream()
                .filter(User::isOnline)
                .toList();
    }

    public void setUserOnline(String username, boolean online) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setOnline(online);
            userRepository.save(user);
        });
    }

    private String getRandomColor() {
        return COLORS[new Random().nextInt(COLORS.length)];
    }
}