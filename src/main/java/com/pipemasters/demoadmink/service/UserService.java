package com.pipemasters.demoadmink.service;

import com.pipemasters.demoadmink.dto.UserDto;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
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
    private final Counter userUpdatedCounter;
    private final Counter userViewsCounter;
    private final Timer userOperationTimer;

    public UserService(MeterRegistry meterRegistry) {
        this.userCreatedCounter = Counter.builder("users_created_total")
                .description("Total number of users created")
                .register(meterRegistry);
        this.userDeletedCounter = Counter.builder("users_deleted_total")
                .description("Total number of users deleted")
                .register(meterRegistry);
        this.userUpdatedCounter = Counter.builder("users_updated_total")
                .description("Total number of users updated")
                .register(meterRegistry);
        this.userViewsCounter = Counter.builder("users_views_total")
                .description("Total number of user views")
                .register(meterRegistry);
        this.userOperationTimer = Timer.builder("users_operation_duration")
                .description("Duration of user operations")
                .register(meterRegistry);

        Gauge.builder("users_total", users, Map::size)
                .description("Current total number of users")
                .register(meterRegistry);

        Gauge.builder("users_average_age", this, UserService::getAverageAge)
                .description("Average age of all users")
                .register(meterRegistry);

        // Инициализация тестовых данных
        createUser(UserDto.builder().name("John Doe").email("john@example.com").age(30).build());
        createUser(UserDto.builder().name("Jane Smith").email("jane@example.com").age(25).build());
    }

    public List<UserDto> getAllUsers() {
        return userOperationTimer.record(() -> {
            log.info("Получение всех пользователей");
            userViewsCounter.increment(users.size());
            return new ArrayList<>(users.values());
        });
    }

    public Optional<UserDto> getUserById(Long id) {
        return userOperationTimer.record(() -> {
            log.info("Получение пользователя с id: {}", id);
            Optional<UserDto> user = Optional.ofNullable(users.get(id));
            if (user.isPresent()) {
                userViewsCounter.increment();
            }
            return user;
        });
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
        return userOperationTimer.record(() -> {
            log.info("Обновление пользователя с id: {}", id);
            if (users.containsKey(id)) {
                userDto.setId(id);
                users.put(id, userDto);
                userUpdatedCounter.increment();
                return Optional.of(userDto);
            }
            return Optional.empty();
        });
    }

    public boolean deleteUser(Long id) {
        return userOperationTimer.record(() -> {
            log.info("Удаление пользователя с id: {}", id);
            UserDto removed = users.remove(id);
            if (removed != null) {
                userDeletedCounter.increment();
                return true;
            }
            return false;
        });
    }

    private double getAverageAge() {
        if (users.isEmpty()) {
            return 0.0;
        }
        return users.values().stream()
                .mapToInt(UserDto::getAge)
                .average()
                .orElse(0.0);
    }
}
