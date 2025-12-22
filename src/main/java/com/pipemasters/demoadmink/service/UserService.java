package com.pipemasters.demoadmink.service;

import com.pipemasters.demoadmink.dto.UserDto;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class UserService {

    private final Map<Long, UserDto> users = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final Counter userCreatedCounter;
    private final Counter userDeletedCounter;

    public UserService(MeterRegistry meterRegistry) {
        this.userCreatedCounter = Counter.builder("users.created")
                .description("Количество созданных пользователей")
                .register(meterRegistry);
        this.userDeletedCounter = Counter.builder("users.deleted")
                .description("Количество удаленных пользователей")
                .register(meterRegistry);

        // Инициализация тестовых данных
        createUser(UserDto.builder().name("John Doe").email("john@example.com").age(30).build());
        createUser(UserDto.builder().name("Jane Smith").email("jane@example.com").age(25).build());
    }

    public List<UserDto> getAllUsers() {
        log.info("Получение всех пользователей");
        return new ArrayList<>(users.values());
    }

    public Optional<UserDto> getUserById(Long id) {
        log.info("Получение пользователя с id: {}", id);
        return Optional.ofNullable(users.get(id));
    }

    public UserDto createUser(UserDto userDto) {
        Long id = idGenerator.getAndIncrement();
        userDto.setId(id);
        users.put(id, userDto);
        userCreatedCounter.increment();
        log.info("Создан пользователь: {}", userDto);
        return userDto;
    }

    public Optional<UserDto> updateUser(Long id, UserDto userDto) {
        log.info("Обновление пользователя с id: {}", id);
        if (users.containsKey(id)) {
            userDto.setId(id);
            users.put(id, userDto);
            return Optional.of(userDto);
        }
        return Optional.empty();
    }

    public boolean deleteUser(Long id) {
        log.info("Удаление пользователя с id: {}", id);
        UserDto removed = users.remove(id);
        if (removed != null) {
            userDeletedCounter.increment();
            return true;
        }
        return false;
    }
}
