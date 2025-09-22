package com.buk.tasker;

import com.buk.tasker.model.User;
import com.buk.tasker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class TaskManagerApplication {
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Bean
	CommandLineRunner init(UserRepository userRepository) {
		return args -> {
			System.out.println("🔍 Инициализация: проверка пользователя test");
			if (userRepository.findByUsername("testuser").isEmpty()) {
				User test = new User();
				test.setUsername("testuser");
				test.setPassword(passwordEncoder.encode("1234"));
				userRepository.save(test);
				System.out.println("✅ test-пользователь создан");
			} else {
				System.out.println("ℹ️ Пользователь test уже существует");
			}
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(TaskManagerApplication.class, args);
	}


}