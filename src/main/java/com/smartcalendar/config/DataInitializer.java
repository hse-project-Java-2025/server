package com.smartcalendar.config;

import com.smartcalendar.model.User;
import com.smartcalendar.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initUsers(UserRepository userRepository) {
        return args -> {
            if (userRepository.count() == 0) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@example.com");
                admin.setPassword("encoded_password"); // сюда поставь реальный зашифрованный пароль
                userRepository.save(admin);

                User user = new User();
                user.setUsername("user");
                user.setEmail("user@example.com");
                user.setPassword("encoded_password");
                userRepository.save(user);
            }
        };
    }
}